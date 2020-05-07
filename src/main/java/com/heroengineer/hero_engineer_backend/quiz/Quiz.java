package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a quiz.
 */
public class Quiz {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public boolean locked; // Students can't take the quiz until the professor unlocks it
    @Getter @Setter
    public boolean viewable; // Whether or not students who have taken this quiz can view the correct answers
    @Getter @Setter
    public int numQuestions; // The number of questions (from the question bank) that will be asked on this quiz
    @Getter @Setter
    public List<QuizQuestion> questionBank; // List of questions that can be asked

    public Quiz() {}

    public Quiz(String name, boolean locked, boolean viewable, int numQuestions, List<QuizQuestion> questionBank) {
        this.name = name;
        this.locked = locked;
        this.viewable = viewable;
        this.numQuestions = numQuestions;
        this.questionBank = questionBank;
    }

    public Quiz(String id, String name, boolean locked, boolean viewable, int numQuestions, List<QuizQuestion> questionBank) {
        this.id = id;
        this.name = name;
        this.locked = locked;
        this.viewable = viewable;
        this.numQuestions = numQuestions;
        this.questionBank = questionBank;
    }

}
