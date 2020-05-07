package com.heroengineer.hero_engineer_backend.quiz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a quiz that a student has already taken.
 */
public class GradedQuiz {

    @Id @Getter
    public String id; // Must match the ID of a regular Quiz to sync up when the professor allows viewing
    @Getter @Setter
    public String name;
    @Getter @Setter
    public double gradePercent;
    @Getter @Setter
    public boolean viewable; // Whether or not the professor is allowing students to view this quiz yet
    @Getter @Setter
    public List<GradedQuizQuestion> questions;

    public GradedQuiz(String id,
                      String name,
                      double gradePercent,
                      boolean viewable,
                      List<GradedQuizQuestion> questions) {
        this.id = id;
        this.name = name;
        this.gradePercent = gradePercent;
        this.viewable = viewable;
        this.questions = questions;
    }

}
