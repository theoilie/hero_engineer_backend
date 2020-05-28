package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a quest.
 */
public class Quest {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public String description;
    @Getter @Setter
    public int automaticXpReward; // XP to give the player upon completion of this quest
    @Getter @Setter
    public boolean main; // Main quest or side quest
    @Getter @Setter
    public boolean complete; // Whether or not the user completed the quest
    @Getter @Setter
    public boolean completeWithQuizzes; // Whether or not this quest is automatically completed by taking a quiz
    @Getter @Setter
    public boolean completeWithCode; // Whether or not this quest is automatically completed by entering a code
    @Getter @Setter
    public boolean completeWithQuizzesAndCode; // Whether or not this quest is automatically completed by both taking a quiz AND entering a code
    @Getter @Setter
    public String code; // The code that the student can enter to complete this quest
    @Getter @Setter
    public String universalCode; // The code that any student can enter to complete this quest
    @Getter @Setter
    public List<String> incompleteQuizIds; // IDs of quizzes that must be completed to finish this quest
    @Getter @Setter
    public List<GradedQuiz> completedQuizzes; // Quizzes that the user already completed for this quest
    @Getter @Setter
    public List<String> requiredQuestIds; // Quests that must be completed prior to starting this quest

    public Quest() {}

    public Quest(String name,
                 String description,
                 int automaticXpReward,
                 boolean main,
                 boolean complete,
                 boolean completeWithQuizzes,
                 boolean completeWithCode,
                 boolean completeWithQuizzesAndCode,
                 String code,
                 String universalCode,
                 List<String> incompleteQuizIds,
                 List<GradedQuiz> completedQuizzes,
                 List<String> requiredQuestIds) {
        this.name = name;
        this.description = description;
        this.automaticXpReward = automaticXpReward;
        this.main = main;
        this.complete = complete;
        this.completeWithQuizzes = completeWithQuizzes;
        this.completeWithCode = completeWithCode;
        this.completeWithQuizzesAndCode = completeWithQuizzesAndCode;
        this.code = code;
        this.universalCode = universalCode;
        this.incompleteQuizIds = incompleteQuizIds;
        this.completedQuizzes = completedQuizzes;
        this.requiredQuestIds = requiredQuestIds;
    }

    public Quest(String id,
                 String name,
                 String description,
                 int automaticXpReward,
                 boolean main,
                 boolean complete,
                 boolean completeWithQuizzes,
                 boolean completeWithCode,
                 boolean completeWithQuizzesAndCode,
                 String code,
                 String universalCode,
                 List<String> incompleteQuizIds,
                 List<GradedQuiz> completedQuizzes,
                 List<String> requiredQuestIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.automaticXpReward = automaticXpReward;
        this.main = main;
        this.complete = complete;
        this.completeWithQuizzes = completeWithQuizzes;
        this.completeWithCode = completeWithCode;
        this.completeWithQuizzesAndCode = completeWithQuizzesAndCode;
        this.code = code;
        this.universalCode = universalCode;
        this.incompleteQuizIds = incompleteQuizIds;
        this.completedQuizzes = completedQuizzes;
        this.requiredQuestIds = requiredQuestIds;
    }

    // Copy constructor
    public Quest(Quest quest) {
        this.id = quest.id;
        this.name = quest.name;
        this.description = quest.getDescription();
        this.automaticXpReward = quest.getAutomaticXpReward();
        this.main = quest.isMain();
        this.complete = quest.isComplete();
        this.completeWithQuizzes = quest.isCompleteWithQuizzes();
        this.completeWithCode = quest.isCompleteWithCode();
        this.completeWithQuizzesAndCode = quest.isCompleteWithQuizzesAndCode();
        this.code = quest.getCode();
        this.universalCode = quest.getUniversalCode();
        this.incompleteQuizIds = quest.getIncompleteQuizIds();
        this.completedQuizzes = quest.getCompletedQuizzes();
        this.requiredQuestIds = quest.getRequiredQuestIds();
    }

}
