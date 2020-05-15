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
import java.util.List;
import java.util.Map;
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
    private final QuestRepository questRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserRepository userRepo,
                          QuestRepository questRepo,
                          PasswordEncoder passwordEncoder,
                          JwtTokenUtil jwtTokenUtil) {
        this.userRepo = userRepo;
        this.questRepo = questRepo;
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

        // Only show the student quiz answers that the professor has marked as viewable
        user.getQuests().forEach(quest ->
                quest.setCompletedQuizzes(quest.getCompletedQuizzes().stream()
                        .filter(GradedQuiz::isViewable)
                        .collect(Collectors.toList()))
        );

        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/create")
    public ResponseEntity<String> addUser(@Valid @RequestBody User user) {
        if (userRepo.findByEmailIgnoreCase(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Email in use\"}");
        }

        if (userRepo.findByUsernameIgnoreCase(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Username in use\"}");
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
