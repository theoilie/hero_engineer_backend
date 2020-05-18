package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quiz.QuizService;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

// TODO: Add origins value so that only the web server (and not students) can use REST

/**
 * REST controller for quests.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/quest")
public class QuestController {

    private final QuestRepository questRepo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final QuestService questService;
    private final QuizService quizService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public QuestController(QuestRepository questRepo,
                           UserRepository userRepo,
                           UserService userService,
                           QuestService questService,
                           QuizService quizService,
                           JwtTokenUtil jwtTokenUtil) {
        this.questRepo = questRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.questService = questService;
        this.quizService = quizService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/quests")
    public ResponseEntity<?> getAllQuests(HttpServletRequest request) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        return ResponseEntity.ok().body(questRepo.findAll());
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveQuest(HttpServletRequest request, @Valid @RequestBody Quest quest) {
        if (userService.isProf(request)) {
            questService.saveQuest(quest);
            return ResponseEntity.ok().body("{\"error\": \"\"}");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }
    }

    @PutMapping("/generateCode")
    public ResponseEntity<String> saveQuestForUser(HttpServletRequest request, @Valid @RequestBody GenerateCodeRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(body.getUserEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No user could be found with the given email.\"}");
        }
        Quest userQuest = user.getQuests().stream().filter(q -> q.getId().equals(body.getQuestId())).findFirst().orElse(null);
        if (userQuest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"User does not have a quest with the given ID.\"}");
        }

        userQuest.setCode(generateCode());
        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeQuest(HttpServletRequest request, @PathVariable String id) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        Optional<Quest> quest = questRepo.findById(id);
        if (quest.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Could not find a quest with that ID\"}");
        }

        // Delete the quest from each user's data
        userRepo.findAll().forEach(user -> {
            if (user.getQuests() == null) return;
            user.setQuests(user.getQuests().stream()
                    .filter(userQuest -> !userQuest.id.equals(id)).collect(Collectors.toList()));
            user.getQuests().forEach(userQuest ->
                userQuest.setRequiredQuestIds(userQuest.getRequiredQuestIds().stream()
                        .filter(requiredQuestId -> !requiredQuestId.equals(id)).collect(Collectors.toList()))
            );
            userRepo.save(user);
        });

        // Delete the quest from other global quests that depend on completing the now-deleted quest first
        questRepo.findAll().forEach(globalQuest -> {
            if (globalQuest.getRequiredQuestIds() == null) return;
                globalQuest.setRequiredQuestIds(globalQuest.getRequiredQuestIds().stream()
                        .filter(requiredQuestId -> !requiredQuestId.equals(id)).collect(Collectors.toList()));
                questRepo.save(globalQuest);
        });

        // Delete any quizzes that were created for this quest
        if (quest.get().getIncompleteQuizIds() != null)
            quest.get().getIncompleteQuizIds().forEach(quizService::removeQuiz);
        if (quest.get().getCompletedQuizzes() != null)
            quest.get().getCompletedQuizzes().forEach(completedQuiz -> quizService.removeQuiz(completedQuiz.getId()));

        questRepo.deleteById(id);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/enterCode")
    public ResponseEntity<String> enterCode(HttpServletRequest request, @RequestBody EnterCodeRequest code) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        Quest quest = user.getQuests().stream()
                .filter(q -> q.getId().equals(code.getQuestId())).findFirst().orElse(null);
        if (quest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No user quest matches the ID given\"}");
        }
        if ((!quest.isCompleteWithCode() && !quest.isCompleteWithQuizzesAndCode()) || quest.getCode() == null || quest.getCode().isEmpty()) {
            return ResponseEntity.ok().body("{\"error\": \"No code to complete this quest is available at this time.\"}");
        }
        if (!quest.getCode().equals(code.getCode())) {
            return ResponseEntity.ok().body("{\"error\": \"Invalid code.\"}");
        }

        if ((!quest.isCompleteWithQuizzesAndCode() && quest.isCompleteWithCode())
                || quest.getIncompleteQuizIds().isEmpty()) {
            quest.setComplete(true);
            if (quest.getIncompleteQuizIds().isEmpty() && !quest.getCompletedQuizzes().isEmpty()) {
                quizService.awardXP(user, quest);
            } else {
                user.setXp(user.getXp() + quest.getAutomaticXpReward());
            }
            userRepo.save(user);
        }

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    private static String generateCode() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 16;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static class GenerateCodeRequest {

        @Getter @Setter
        public String userEmail;
        @Getter @Setter
        public String questId;

        public GenerateCodeRequest() {}

        public GenerateCodeRequest(String userEmail, String questId) {
            this.userEmail = userEmail;
            this.questId = questId;
        }

    }

    private static class EnterCodeRequest {

        @Getter @Setter
        public String questId;
        @Getter @Setter
        public String code;

        public EnterCodeRequest() {}

        public EnterCodeRequest(String questId, String code) {
            this.questId = questId;
            this.code = code;
        }

    }

}
