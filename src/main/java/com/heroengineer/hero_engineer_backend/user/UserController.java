package com.heroengineer.hero_engineer_backend.user;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quest.QuestRepository;
import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

// TODO: Add origins value so that only the web server (and not students) can use REST

/**
 * REST controller for heroes.
 */
@CrossOrigin
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
        user.setAvatarDataMale(body.getAvatarDataMale());
        user.setAvatarDataFemale(body.getAvatarDataFemale());
        user.setAvatarDataColors(body.getAvatarDataColors());
        userRepo.save(user);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    private static class ChangeWhitelistRequest {

        @Getter @Setter
        public String email;

        public ChangeWhitelistRequest() {}

        public ChangeWhitelistRequest(String email) {
            this.email = email;
        }

    }

    @Getter @Setter
    private static class UpdateAvatarRequest {

        public String avatarSVG;
        public AvatarDataMale avatarDataMale;
        public AvatarDataFemale avatarDataFemale;
        public AvatarDataColors avatarDataColors;

        public UpdateAvatarRequest() {}

        public UpdateAvatarRequest(String avatarSVG, AvatarDataMale avatarDataMale, AvatarDataFemale avatarDataFemale, AvatarDataColors avatarDataColors) {
            this.avatarSVG = avatarSVG;
            this.avatarDataMale = avatarDataMale;
            this.avatarDataFemale = avatarDataFemale;
            this.avatarDataColors = avatarDataColors;
        }

    }

}
