package com.heroengineer.hero_engineer_backend.quiz;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import org.bson.codecs.ObjectIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * REST controller for quizzes.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizRepository quizRepo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final QuizService quizService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public QuizController(QuizRepository quizRepo,
                          UserRepository userRepo,
                          UserService userService,
                          QuizService quizService,
                          JwtTokenUtil jwtTokenUtil) {
        this.quizRepo = quizRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.quizService = quizService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/quizzes")
    public ResponseEntity<?> getAllQuizzes(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        if (userService.isProf(request)) {
            // Professor needs to see all quiz questions and answers
            return ResponseEntity.ok().body(quizRepo.findAll());
        } else {
            // Students only get to see the number of questions needed for each quiz, and no answers
            List<Quiz> quizzes = new ArrayList<>();
            quizRepo.findAll().forEach(quiz -> {
                // Don't select questions from the question bank if the quiz is locked
                if (quiz.isLocked()) {
                    quiz.setQuestionBank(new ArrayList<>());
                    quizzes.add(quiz);
                    return;
                }

                // Randomly select questions from the question bank. Redact the answer
                List<QuizQuestion> questionBank = new ArrayList<>();
                Set<Integer> indexes = new HashSet<>();
                for (int i = 0; i < quiz.numQuestions; i++) {
                    int randomIndex;
                    do {
                        randomIndex = ThreadLocalRandom.current().nextInt(0, quiz.getQuestionBank().size());
                    } while (indexes.contains(randomIndex));
                    indexes.add(randomIndex);
                    QuizQuestion question = quiz.getQuestionBank().get(randomIndex);
                    question.getAnswerOptions().forEach(answer -> answer.setCorrect(false));
                    questionBank.add(question);
                }
                quiz.setQuestionBank(questionBank);
                quizzes.add(quiz);
            });
            return ResponseEntity.ok().body(quizzes);
        }
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveQuiz(HttpServletRequest request, @Valid @RequestBody Quiz quiz) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        // Make sure each question and answer have IDs
        if (quiz.getQuestionBank() == null) quiz.setQuestionBank(new ArrayList<>());
        for (QuizQuestion question : quiz.getQuestionBank()) {
            if (question.getId() == null) question.setId(new ObjectIdGenerator().generate().toString());
            if (question.getAnswerOptions() == null) question.setAnswerOptions(new ArrayList<>());
            for (QuizAnswer answer : question.getAnswerOptions()) {
                if (answer.getId() == null) answer.setId(new ObjectIdGenerator().generate().toString());
            }
        }

        quizService.updateViewable(quiz);

        quizRepo.save(quiz);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeQuiz(HttpServletRequest request, @PathVariable String id) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        if (quizRepo.findById(id).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Could not find a quiz with that ID\"}");
        }

        quizService.removeQuiz(id);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping("/grade")
    public ResponseEntity<String> gradeQuiz(HttpServletRequest request, @RequestBody GradeQuizRequest quiz) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }
        User user = userRepo.findByEmailIgnoreCase(email);
        Quest quest = user.getQuests().stream()
                .filter(q -> q.getId().equals(quiz.getQuestId())).findFirst().orElse(null);
        if (quest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No user quest matches the ID given\"}");
        }
        if (!quest.getIncompleteQuizIds().contains(quiz.getQuizId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"User has no quizzes available to grade with the ID given\"}");
        }
        Quiz globalQuiz = quizRepo.findById(quiz.getQuizId()).orElse(null);
        if (globalQuiz == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No global quiz matches the quiz ID given\"}");
        }

        // TODO: This system assumes the student is sending all answers, which is not the case if they use curl/postman/not the website
        List<GradedQuizQuestion> gradedQuestions = new ArrayList<>();
        int points = 0, maxPoints = 0;
        for (GradeQuizRequest.Answer questionAnswer : quiz.getAnswers()) {
            QuizQuestion question = globalQuiz.getQuestionBank().stream()
                    .filter(q -> q.getId().equals(questionAnswer.getQuestionId())).findFirst().orElse(null);
            if (question == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Question with ID " + questionAnswer.getQuestionId() + " does not exist in the global quiz's question bank\"}");
            }
            QuizAnswer answer = question.getAnswerOptions().stream()
                    .filter(a -> a.getId().equals(questionAnswer.getAnswerId())).findFirst().orElse(null);
            if (answer == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Answer with ID " + questionAnswer.getAnswerId() + " does not exist in the global quiz's question bank\"}");
            }
            maxPoints += question.getPoints();
            if (answer.isCorrect()) points += question.getPoints();
            GradedQuizQuestion gradedQuestion = new GradedQuizQuestion(
                    question.getId(),
                    question.getQuestion(),
                    question.getPoints(),
                    answer.getId(),
                    question.getAnswerOptions()
            );
            gradedQuestions.add(gradedQuestion);
        }

        double percentCorrect = points / (maxPoints * 1D);
        GradedQuiz gradedQuiz = new GradedQuiz(
                globalQuiz.getId(),
                globalQuiz.getName(),
                percentCorrect,
                globalQuiz.isViewable(),
                gradedQuestions
        );
        quest.getCompletedQuizzes().add(gradedQuiz);

        quest.getIncompleteQuizIds().remove(globalQuiz.getId());
        if (quest.getIncompleteQuizIds().isEmpty()) {
            // Award XP if this was the last quiz the user needed to complete for this quest
            quizService.awardXP(user, quest);

            // Complete the quest if this was the last quiz and completeWithQuizzes is true
            if (quest.isCompleteWithQuizzes() && !quest.isCompleteWithQuizzesAndCode()) {
                quest.setComplete(true);
            }
        }

        userRepo.save(user);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

}
