package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * MongoDB representation of an answer for a question that can be asked in a quiz.
 */
public class QuizAnswer {

    @Id @Getter @Setter
    public String id;
    @Getter @Setter
    public String answer;
    @Getter @Setter
    public boolean correct;

    public QuizAnswer() {}

    public QuizAnswer(String answer, boolean correct) {
        this.answer = answer;
        this.correct = correct;
    }

    public QuizAnswer(String id, String answer, boolean correct) {
        this.id = id;
        this.answer = answer;
        this.correct = correct;
    }

}
