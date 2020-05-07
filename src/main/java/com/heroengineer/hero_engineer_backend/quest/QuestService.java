package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class QuestService {

    private final QuestRepository questRepo;
    private final UserRepository userRepo;

    @Autowired
    public QuestService(QuestRepository questRepo, UserRepository userRepo) {
        this.questRepo = questRepo;
        this.userRepo = userRepo;
    }

    public void saveQuest(Quest quest) {
        if (quest.getIncompleteQuizIds() == null) quest.setIncompleteQuizIds(new ArrayList<>());
        if (quest.getCompletedQuizzes() == null) quest.setCompletedQuizzes(new ArrayList<>());
        if (quest.getRequiredQuestIds() == null) quest.setRequiredQuestIds(new ArrayList<>());
        questRepo.save(quest);

        // Add/update the quest to all users
        userRepo.findAll().forEach(user -> {
            if (user.getQuests() == null) user.setQuests(new ArrayList<>());
            user.getQuests().stream().filter(userQuest -> userQuest.getId().equals(quest.getId())).forEach(
                    userQuest -> {
                        userQuest.setName(quest.getName());
                        userQuest.setDescription(quest.getDescription());
                        userQuest.setAutomaticXpReward(quest.getAutomaticXpReward());
                        userQuest.setMain(quest.isMain());
                        userQuest.setCompleteWithQuizzes(quest.isCompleteWithQuizzes());
                        userQuest.setRequiredQuestIds(quest.getRequiredQuestIds());
                        for (String quizId : quest.getIncompleteQuizIds()) {
                            if (!userQuest.getIncompleteQuizIds().contains(quizId)
                                    && userQuest.getCompletedQuizzes().stream().noneMatch(q -> q.getId().equals(quizId))) {
                                userQuest.getIncompleteQuizIds().add(quizId);
                            }
                        }
                    }
            );
            if (user.getQuests().stream().noneMatch(userQuest -> userQuest.getId().equals(quest.getId()))) {
                user.getQuests().add(new Quest(quest));
            }
            userRepo.save(user);
        });
    }

}
