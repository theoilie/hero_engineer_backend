package com.heroengineer.hero_engineer_backend.quiz;

import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quest.QuestRepository;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuestRepository questRepo;
    private final QuizRepository quizRepo;
    private final UserRepository userRepo;

    @Autowired
    public QuizService(QuestRepository questRepo,
                       QuizRepository quizRepo,
                       UserRepository userRepo) {
        this.questRepo = questRepo;
        this.quizRepo = quizRepo;
        this.userRepo = userRepo;
    }

    public void removeQuiz(String id) {
        Optional<Quiz> quizOptional = quizRepo.findById(id);
        if (quizOptional.isEmpty()) return;

        // Delete the quiz from each user's quests
        userRepo.findAll().forEach(user -> {
            user.getQuests().forEach(userQuest -> {
                userQuest.setIncompleteQuizIds(userQuest.getIncompleteQuizIds().stream()
                        .filter(incompleteQuiz -> !incompleteQuiz.equals(id)).collect(Collectors.toList()));
                userQuest.setCompletedQuizzes(userQuest.getCompletedQuizzes().stream()
                        .filter(completeQuiz -> !completeQuiz.getId().equals(id)).collect(Collectors.toList()));
            });
            userRepo.save(user);
        });

        // Delete the quiz from global quests
        questRepo.findAll().forEach(globalQuest -> {
            globalQuest.setIncompleteQuizIds(globalQuest.getIncompleteQuizIds().stream()
                    .filter(incompleteQuiz -> !incompleteQuiz.equals(id)).collect(Collectors.toList()));
            globalQuest.setCompletedQuizzes(globalQuest.getCompletedQuizzes().stream()
                    .filter(completeQuiz -> !completeQuiz.getId().equals(id)).collect(Collectors.toList()));
            questRepo.save(globalQuest);
        });

        quizRepo.deleteById(id);
    }

    // Tell each student's quiz if it's viewable, as dictated by the professor
    public void updateViewable(Quiz quiz) {
        for (User user : userRepo.findAll()) {
            boolean changed = false;
            for (Quest quest : user.getQuests()) {
                for (GradedQuiz gradedQuiz : quest.getCompletedQuizzes()) {
                    if (gradedQuiz.getId().equals(quiz.getId())) {
                        if (gradedQuiz.isViewable() != quiz.isViewable()) {
                            gradedQuiz.setViewable(quiz.isViewable());
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (changed) userRepo.save(user);
        }
    }

    public void awardXP(User user, Quest quest) {
        double totalPercentCorrect = 0;
        for (GradedQuiz otherGradedQuiz : quest.getCompletedQuizzes()) {
            totalPercentCorrect += otherGradedQuiz.getGradePercent();
        }
        double avgPercentCorrect = totalPercentCorrect / quest.getCompletedQuizzes().size();
        user.setXp(user.getXp() + (int) (avgPercentCorrect * quest.getAutomaticXpReward()));
    }

}
