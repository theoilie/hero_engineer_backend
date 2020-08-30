package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quiz.QuizService;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import com.heroengineer.hero_engineer_backend.util.UtilService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for quests.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/quest")
public class QuestController {

    private final QuestRepository questRepo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final QuestService questService;
    private final QuizService quizService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UtilService util;

    @Autowired
    public QuestController(QuestRepository questRepo,
                           UserRepository userRepo,
                           UserService userService,
                           QuestService questService,
                           QuizService quizService,
                           JwtTokenUtil jwtTokenUtil,
                           UtilService util) {
        this.questRepo = questRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.questService = questService;
        this.quizService = quizService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.util = util;
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
    public ResponseEntity<String> generateCodeForStudent(HttpServletRequest request, @Valid @RequestBody GenerateCodeRequest body) {
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

        userQuest.setCode(util.generateCode());
        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PutMapping("/generateUniversalCode")
    public ResponseEntity<String> generateCodeForQuest(HttpServletRequest request, @Valid @RequestBody GenerateUniversalCodeRequest body) {
        System.out.println("reached generateUniversalCode");
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        Quest quest = questRepo.findById(body.getQuestId()).orElse(null);
        if (quest == null) {
            System.out.println("quest is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No quest with the given ID could be found.\"}");
        }

        quest.setUniversalCode(util.generateCode());
        questRepo.save(quest);
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT_NOT_ACCEPTED\"}");
        }

        User user = userRepo.findByEmailIgnoreCase(email);
        Quest userQuest = user.getQuests().stream()
                .filter(q -> q.getId().equals(code.getQuestId())).findFirst().orElse(null);
        Quest globalQuest = questRepo.findById(code.getQuestId()).orElse(null);
        if (userQuest == null || globalQuest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"NO_QUEST_FOUND\"}");
        }
        String individualCode = (userQuest.getCode() == null || userQuest.getCode().isEmpty()) ? "" : userQuest.getCode();
        String universalCode = (globalQuest.getUniversalCode() == null || globalQuest.getUniversalCode().isEmpty()) ? "" : globalQuest.getUniversalCode();
        if ((!globalQuest.isCompleteWithCode() && !globalQuest.isCompleteWithQuizzesAndCode()) ||
                (individualCode.isEmpty() && universalCode.isEmpty())) {
            return ResponseEntity.ok().body("{\"error\": \"NO_CODE_AVAILABLE\"}");
        }
        if (!individualCode.equals(code.getCode()) && !universalCode.equals(code.getCode())) {
            return ResponseEntity.ok().body("{\"error\": \"INVALID_CODE\"}");
        }
        if (userQuest.isCodeEnteredSuccessfully()) {
            return ResponseEntity.ok().body("{\"error\": \"CODE_ALREADY_ENTERED\"}");
        }
        if (userQuest.isComplete()) {
            return ResponseEntity.ok().body("{\"error\": \"QUEST_ALREADY_COMPLETED\"}");
        }

        if ((!globalQuest.isCompleteWithQuizzesAndCode() && globalQuest.isCompleteWithCode())
                || userQuest.getIncompleteQuizIds().isEmpty()) {
            userQuest.setComplete(true);
            if (userQuest.getIncompleteQuizIds().isEmpty() && !userQuest.getCompletedQuizzes().isEmpty()) {
                quizService.awardXP(user, userQuest);
            } else {
                user.addXp(globalQuest.getAutomaticXpReward(), "Completed quest (with no quizzes): " + globalQuest.getName());
            }
        }

        userQuest.setCodeEnteredSuccessfully(true);
        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GenerateCodeRequest {

        public String userEmail;
        public String questId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GenerateUniversalCodeRequest {

        public String questId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EnterCodeRequest {

        public String questId;
        public String code;

    }

}
