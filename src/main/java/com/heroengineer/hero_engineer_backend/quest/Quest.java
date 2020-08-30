package com.heroengineer.hero_engineer_backend.quest;

import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a quest.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quest {

    @Id
    public String id;
    public String name;
    public String description;
    public int automaticXpReward; // XP to give the player upon completion of this quest
    public boolean main; // Main quest or side quest
    public boolean available; // Whether or not students should be able to view and start this quest
    public boolean complete; // Whether or not the user completed the quest
    public boolean completeWithQuizzes; // Whether or not this quest is automatically completed by taking a quiz
    public boolean completeWithCode; // Whether or not this quest is automatically completed by entering a code
    public boolean completeWithQuizzesAndCode; // Whether or not this quest is automatically completed by both taking a quiz AND entering a code
    public boolean codeEnteredSuccessfully; // Whether or not the student has previously entered the correct code for this quest
    public String code; // The code that the student can enter to complete this quest
    public String universalCode; // The code that any student can enter to complete this quest
    public List<String> incompleteQuizIds; // IDs of quizzes that must be completed to finish this quest
    public List<GradedQuiz> completedQuizzes; // Quizzes that the user already completed for this quest
    public List<String> requiredQuestIds; // Quests that must be completed prior to starting this quest

    // Copy constructor
    public Quest(Quest quest) {
        this.id = quest.id;
        this.name = quest.name;
        this.description = quest.getDescription();
        this.automaticXpReward = quest.getAutomaticXpReward();
        this.main = quest.isMain();
        this.available = quest.isAvailable();
        this.complete = quest.isComplete();
        this.completeWithQuizzes = quest.isCompleteWithQuizzes();
        this.completeWithCode = quest.isCompleteWithCode();
        this.completeWithQuizzesAndCode = quest.isCompleteWithQuizzesAndCode();
        this.codeEnteredSuccessfully = quest.isCodeEnteredSuccessfully();
        this.code = quest.getCode();
        this.universalCode = quest.getUniversalCode();
        this.incompleteQuizIds = quest.getIncompleteQuizIds();
        this.completedQuizzes = quest.getCompletedQuizzes();
        this.requiredQuestIds = quest.getRequiredQuestIds();
    }

}
