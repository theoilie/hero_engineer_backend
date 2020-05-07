package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a question that was asked in a graded quiz.
 */
public class GradedQuizQuestion {

    @Id @Getter @Setter
    public String id;
    @Getter @Setter
    public String question;
    @Getter @Setter
    public int points;
    @Getter @Setter
    public String studentAnswerId; // ID of the QuizAnswer that the student chose
    @Getter @Setter
    public List<QuizAnswer> answerOptions;

    public GradedQuizQuestion() {}

    public GradedQuizQuestion(String question, int points, String studentAnswerId, List<QuizAnswer> answerOptions) {
        this.question = question;
        this.points = points;
        this.studentAnswerId = studentAnswerId;
        this.answerOptions = answerOptions;
    }

    public GradedQuizQuestion(String id, String question, int points, String studentAnswerId, List<QuizAnswer> answerOptions) {
        this.id = id;
        this.question = question;
        this.points = points;
        this.studentAnswerId = studentAnswerId;
        this.answerOptions = answerOptions;
    }
}
