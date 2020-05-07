package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;

import java.util.List;

/**
 * A student's quiz that they are submitting for grading.
 */
public class GradeQuizRequest {

    @Getter
    public String questId;
    @Getter
    public String quizId;
    @Getter
    public List<Answer> answers;

    public static class Answer {

        @Getter
        public String questionId;
        @Getter
        public String answerId;

    }

}
