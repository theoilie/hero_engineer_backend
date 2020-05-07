package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import com.heroengineer.hero_engineer_backend.quiz.QuizService;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
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
}
