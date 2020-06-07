package com.heroengineer.hero_engineer_backend.section;

import com.heroengineer.hero_engineer_backend.HeroEngineerBackendApplication;
import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for class sections.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/section")
public class SectionController {

    private final SectionRepository repo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public SectionController(SectionRepository repo,
                             UserService userService,
                             UserRepository userRepo,
                             JwtTokenUtil jwtTokenUtil) {
        this.repo = repo;
        this.userService = userService;
        this.userRepo = userRepo;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/sections")
    public List<Section> getAll() {
        return repo.findAll();
    }

    @GetMapping("/section")
    public ResponseEntity<?> get(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }
        for (Section section : repo.findAll()) {
            if (section.getEmails().contains(email)) {
                return ResponseEntity.ok().body(section);
            }
        }
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/classmates")
    public ResponseEntity<?> getClassmates(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }
        for (Section section : repo.findAll()) {
            if (section.getEmails().contains(email.toLowerCase())) {
                List<User> classmates = new ArrayList<>();
                for (String classmateEmail : section.getEmails()) {
                    if (classmateEmail.equals(email)) continue;
                    User classmate = userRepo.findByEmailIgnoreCase(classmateEmail);
                    classmate.setQuests(new ArrayList<>());
                    classmate.setPassword("");
                    classmates.add(classmate);
                }
                return ResponseEntity.ok().body(classmates);
            }
        }
        return ResponseEntity.ok().body(null);
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveSection(HttpServletRequest request, @Valid @RequestBody Section section) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        List<String> emails = new ArrayList<>();
        for (String email : section.getEmails()) {
            emails.add(email.toLowerCase());
        }
        section.setEmails(emails);

        repo.save(section);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeSection(HttpServletRequest request, @PathVariable String id) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        repo.deleteById(id);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }
}
