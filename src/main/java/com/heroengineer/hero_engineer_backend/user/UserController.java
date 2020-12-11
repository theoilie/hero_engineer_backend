package com.heroengineer.hero_engineer_backend.user;

import com.heroengineer.hero_engineer_backend.assignment.GradedShortAnswerAssignment;
import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quest.QuestRepository;
import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for heroes.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepo;
    private final UserWhitelistRepository userWhitelistRepo;
    private final QuestRepository questRepo;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserRepository userRepo,
                          UserWhitelistRepository userWhitelistRepo,
                          QuestRepository questRepo,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtTokenUtil jwtTokenUtil) {
        this.userRepo = userRepo;
        this.userWhitelistRepo = userWhitelistRepo;
        this.questRepo = questRepo;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);

        user.getQuests().forEach(quest -> {
            // Only show the student quiz answers that the professor has marked as viewable
            quest.setCompletedQuizzes(quest.getCompletedQuizzes().stream()
                    .filter(GradedQuiz::isViewable)
                    .collect(Collectors.toList()));
            // Don't show the user the code to complete their quest
            quest.setCode("");
        });

        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/professorAvatar")
    public ResponseEntity<String> getProfessorAvatar(HttpServletRequest request) {
        return ResponseEntity.ok().body(userRepo.findByEmailIgnoreCase("admin@usc.edu").getAvatarSVG());
    }

    @GetMapping("/allUsers")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        return ResponseEntity.ok().body(userRepo.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<String> addUser(@Valid @RequestBody User user) {
        if (userRepo.findByEmailIgnoreCase(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Email in use\"}");
        }

        if (userRepo.findByUsernameIgnoreCase(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Username in use\"}");
        }

        UserWhitelist whitelist = userWhitelistRepo
                .findById("default")
                .orElse(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));
        if (!whitelist.getEmails().contains(user.getEmail().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"The email address you gave is not on the registrar for this semester. Please try a different email or contact Professor Ramsey.\"}");
        }

        // Copy all global quests into the user's local list
        ArrayList<Quest> quests = new ArrayList<>();
        questRepo.findAll().forEach(quest -> quests.add(new Quest(quest)));
        user.setQuests(quests);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setXp(0);
        user.setPoints(0);
        user.setProf(false);
        userRepo.insert(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(HttpServletRequest request, @Valid @RequestBody ResetPasswordRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(body.getEmail());
        user.setResetPasswordOnLogin(body.isResetPassword());
        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/setPassword")
    public ResponseEntity<String> setPassword(HttpServletRequest request, @Valid @RequestBody SetPasswordRequest body) {
        User user = userRepo.findByEmailIgnoreCase(body.getEmail());
        if (user.isResetPasswordOnLogin()) {
            user.setResetPasswordOnLogin(false);
            user.setPassword(new BCryptPasswordEncoder().encode(body.getPassword()));
            userRepo.save(user);
        }
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/addToWhitelist")
    public ResponseEntity<String> addUserToWhitelist(HttpServletRequest request, @Valid @RequestBody ChangeWhitelistRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        UserWhitelist whitelist = userWhitelistRepo
                .findById("default")
                .orElse(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));
        whitelist.getEmails().add(body.getEmail().toLowerCase());
        userWhitelistRepo.save(whitelist);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/removeFromWhitelist")
    public ResponseEntity<String> removeUserFromWhitelist(HttpServletRequest request, @Valid @RequestBody ChangeWhitelistRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        UserWhitelist whitelist = userWhitelistRepo
                .findById("default")
                .orElse(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));
        whitelist.getEmails().remove(body.getEmail().toLowerCase());
        userWhitelistRepo.save(whitelist);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @GetMapping("/getWhitelist")
    public ResponseEntity<?> addUserToWhitelist(HttpServletRequest request) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        UserWhitelist whitelist = userWhitelistRepo
                .findById("default")
                .orElse(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));

        return ResponseEntity.ok().body(whitelist);
    }

    @PostMapping("/updateAvatar")
    public ResponseEntity<String> updateAvatar(HttpServletRequest request, @RequestBody UpdateAvatarRequest body) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        user.setAvatarSVG(body.getAvatarSVG());
        user.setAvatarData(body.getAvatarData());
        user.setAvatarDataColors(body.getAvatarDataColors());
        userRepo.save(user);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/setIdeas")
    public ResponseEntity<String> setIdeas(HttpServletRequest request, @RequestBody SetIdeasRequest body) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        user.setIdea1(body.getIdea1());
        user.setIdea2(body.getIdea2());
        user.setIdea3(body.getIdea3());
        userRepo.save(user);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/addXP")
    public ResponseEntity<String> addXP(HttpServletRequest request, @Valid @RequestBody AddXPRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(body.getEmail());
        user.addXp(body.getXp(), body.getReason());

        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @GetMapping("/XPBreakdown/{email}")
    public ResponseEntity<?> getXPBreakdown(HttpServletRequest request, @PathVariable String email) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"No user was found for the email provided.\"}");
        }
        if (user.getQuests() == null) user.setQuests(new ArrayList<>());
        if (user.getGradedShortAnswerAssignments() == null) user.setGradedShortAnswerAssignments(new ArrayList<>());

        int totalXP = 0;
        Map<String, Integer> breakdown = new HashMap<>();
        for (Quest quest : user.getQuests()) {
            if (quest.isComplete()) {
                int xpGained = 0;
                if (quest.getIncompleteQuizIds().isEmpty() && !quest.getCompletedQuizzes().isEmpty()) {
                    double totalPercentCorrect = 0;
                    for (GradedQuiz otherGradedQuiz : quest.getCompletedQuizzes()) {
                        totalPercentCorrect += otherGradedQuiz.getGradePercent();
                    }
                    double avgPercentCorrect = totalPercentCorrect / quest.getCompletedQuizzes().size();
                    xpGained = (int) (avgPercentCorrect * quest.getAutomaticXpReward());
                } else {
                   xpGained = quest.getAutomaticXpReward();
                }

                if (xpGained != 0) {
                    breakdown.put(quest.getName(), xpGained);
                    totalXP += xpGained;
                }
            }
        }
        for (GradedShortAnswerAssignment gradedAssignment : user.getGradedShortAnswerAssignments()) {
            if (gradedAssignment.getXpAwarded() != 0) {
                breakdown.put("[In-Class Assignment] " + gradedAssignment.getName(), gradedAssignment.getXpAwarded());
                totalXP += gradedAssignment.getXpAwarded();
            }
        }

        breakdown.put("Total", totalXP);

        return ResponseEntity.ok().body(breakdown);
    }

    @GetMapping("/XPHistory/{email}")
    public ResponseEntity<?> getXPHistory(HttpServletRequest request, @PathVariable String email) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"No user was found for the email provided.\"}");
        }

        if (user.getXpEntries() == null) user.setXpEntries(new ArrayList<>());
        int total = 0;
        for (XpEntry entry : user.getXpEntries()) {
            total += entry.getXpChangeAmount();
        }
        user.getXpEntries().add(new XpEntry(total, "Total"));

        return ResponseEntity.ok().body(user.getXpEntries());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ChangeWhitelistRequest {

        public String email;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UpdateAvatarRequest {

        public String avatarSVG;
        public AvatarData avatarData;
        public AvatarDataColors avatarDataColors;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SetIdeasRequest {

        public String idea1;
        public String idea2;
        public String idea3;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ResetPasswordRequest {

        public String email;
        public boolean resetPassword;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SetPasswordRequest {

        public String email;
        public String password;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AddXPRequest {

        public String email;
        public int xp;
        public String reason;

    }

}
