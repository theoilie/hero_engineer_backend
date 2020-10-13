package com.heroengineer.hero_engineer_backend.assignment;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.section.Section;
import com.heroengineer.hero_engineer_backend.section.SectionRepository;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.codecs.ObjectIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for in-class, short-answer assignments.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/ShortAnswerAssignment")
public class ShortAnswerAssignmentController {

    private final ShortAnswerAssignmentRepository repo;
    private final UserRepository userRepo;
    private final SectionRepository sectionRepo;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public ShortAnswerAssignmentController(ShortAnswerAssignmentRepository repo,
                                           UserRepository userRepo,
                                           SectionRepository sectionRepo,
                                           UserService userService,
                                           JwtTokenUtil jwtTokenUtil) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.sectionRepo = sectionRepo;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * @return the ShortAnswerAssignment that is currently active for the class section of the student requesting it
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveShortAnswerAssignment(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }
        for (Section section : sectionRepo.findAll()) {
            if (section.getEmails().contains(email.toLowerCase())) {
                for (ShortAnswerAssignment shortAnswerAssignment : repo.findAll()) {
                    if (shortAnswerAssignment.getSectionIdsAvailableFor().contains(section.getId())) {
                        shortAnswerAssignment.setSectionIdsAvailableFor(Collections.singletonList(section.getId()));
                        return ResponseEntity.ok(shortAnswerAssignment);
                    }
                }
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("{\"error\": \"NO_ACTIVE_ASSIGNMENT\"}");
            }
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("{\"error\": \"NOT_IN_CLASS_SECTION\"}");
    }

    @GetMapping("/assignments")
    public List<ShortAnswerAssignment> getAll() {
        return repo.findAll();
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveAssignment(HttpServletRequest request, @Valid @RequestBody ShortAnswerAssignment assignment) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        // Avoid NPEs
        if (assignment.getSectionIdsAvailableFor() == null) assignment.setSectionIdsAvailableFor(new ArrayList<>());
        if (assignment.getSectionIdsGradesAvailableFor() == null) assignment.setSectionIdsGradesAvailableFor(new ArrayList<>());
        if (assignment.getQuestions() == null) assignment.setQuestions(new ArrayList<>());
        for (ShortAnswerQuestion question : assignment.getQuestions()) {
            if (question.getId() == null) question.setId(new ObjectIdGenerator().generate().toString());
        }

        // Update graded assignments on user objects in Mongo to make sure they're in sync with the saved data
        for (User user : userRepo.findAll()) {
            if (user == null || user.getGradedShortAnswerAssignments() == null) continue;

            boolean changed = false;
            for (GradedShortAnswerAssignment gradedAssignment : user.getGradedShortAnswerAssignments()) {
                if (gradedAssignment.getId().equals(assignment.getId())) {
                    for (Section section : sectionRepo.findAll()) {
                        if (section.getEmails().contains(user.getEmail().toLowerCase())) {
                            boolean available = assignment.getSectionIdsGradesAvailableFor().contains(section.getId());
                            if (gradedAssignment.isAvailable() != available) {
                                gradedAssignment.setAvailable(available);
                                changed = true;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            if (changed) userRepo.save(user);
        }

        repo.save(assignment);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeAssignment(HttpServletRequest request, @PathVariable String id) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        repo.deleteById(id);

        // Update graded assignments on user objects in Mongo to make sure they're in sync with the saved data
        for (User user : userRepo.findAll()) {
            if (user == null || user.getGradedShortAnswerAssignments() == null) continue;

            boolean changed = false;
            List<GradedShortAnswerAssignment> gradedAssignments = user.getGradedShortAnswerAssignments()
                    .stream()
                    .filter(gradedAssignment -> !gradedAssignment.getId().equals(id))
                    .collect(Collectors.toList());
            if (gradedAssignments.size() < user.getGradedShortAnswerAssignments().size()) {
                user.setGradedShortAnswerAssignments(gradedAssignments);
                userRepo.save(user);
            }
        }

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/saveGraded")
    public ResponseEntity<String> saveGradedAssignment(HttpServletRequest request, @RequestBody GradedShortAnswerAssignmentRequest gradedAssignmentRequest) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        GradedShortAnswerAssignment gradedAssignment = gradedAssignmentRequest.getGradedAssignment();
        boolean isProf = userService.isProf(request);

        // Find a matching ungraded assignment template and verify that the data the student is sending matches
        if (!isProf) {
            boolean matchedAssignment = false;
            for (ShortAnswerAssignment assignment : repo.findAll()) {
                if (assignment.getId().equals(gradedAssignment.getId())) {
                    matchedAssignment = true;
                    gradedAssignment.setMaxXp(assignment.getMaxXp());
                    gradedAssignment.setXpAwarded(0);
                    gradedAssignment.setAvailable(false);
                    for (Section section : sectionRepo.findAll()) {
                        if (section.getEmails().contains(email.toLowerCase()) && assignment.getSectionIdsGradesAvailableFor().contains(section.getId())) {
                            gradedAssignment.setAvailable(true);
                            break;
                        }
                    }
                    break;
                }
            }
            if (!matchedAssignment) {
                return ResponseEntity.badRequest().body("{\"error\": \"INVALID_ASSIGNMENT\"}");
            }
        }

        for (User user : userRepo.findAll()) {
            if (user == null || !user.getEmail().equalsIgnoreCase(isProf ? gradedAssignmentRequest.getEmail() : email)) continue;
            if (user.getGradedShortAnswerAssignments() == null) user.setGradedShortAnswerAssignments(new ArrayList<>());

            // Remove the old assignment object from the student's data before adding the updated assignment object
            boolean firstTimeGrading = true;
            List<GradedShortAnswerAssignment> updatedGradedAssignments = new ArrayList<>(user.getGradedShortAnswerAssignments());
            for (GradedShortAnswerAssignment otherGradedAssignment : user.getGradedShortAnswerAssignments()) {
                if (otherGradedAssignment.getId().equals(gradedAssignment.getId())) {
                    if (isProf) {
                        // Grade the assignment if the professor sent this request
                        firstTimeGrading = false;
                        updatedGradedAssignments.remove(otherGradedAssignment);
                        int changeAmount = gradedAssignment.getXpAwarded() - otherGradedAssignment.getXpAwarded();
                        if (changeAmount != 0) {
                            user.addXp(changeAmount, "Updated grade for in-class assignment: " + gradedAssignment.getName());
                        }
                        break;
                    } else {
                        // Don't allow a student to submit their assignment more than once
                        return ResponseEntity.badRequest().body("{\"error\": \"ASSIGNMENT_ALREADY_GRADED\"}");
                    }
                }
            }

            updatedGradedAssignments.add(gradedAssignment);
            user.setGradedShortAnswerAssignments(updatedGradedAssignments);
            if (firstTimeGrading) {
                user.addXp(gradedAssignment.getXpAwarded(), "Completed in-class assignment: " + gradedAssignment.getName());
            }
            userRepo.save(user);
            break;
        }

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @Data
    @RequiredArgsConstructor
    static class GradedShortAnswerAssignmentRequest {
        final GradedShortAnswerAssignment gradedAssignment;
        final String email;
    }

}
