package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a question that can be asked in a quiz.
 */
public class QuizQuestion {

    @Id @Getter @Setter
    public String id;
    @Getter @Setter
    public String question;
    @Getter @Setter
    public int points;
    @Getter @Setter
    public List<QuizAnswer> answerOptions;

    public QuizQuestion() {}

    public QuizQuestion(String question, int points, List<QuizAnswer> answerOptions) {
        this.points = points;
        this.question = question;
        this.answerOptions = answerOptions;
    }

    public QuizQuestion(String id, String question, int points, List<QuizAnswer> answerOptions) {
        this.id = id;
        this.points = points;
        this.question = question;
        this.answerOptions = answerOptions;
    }
}
