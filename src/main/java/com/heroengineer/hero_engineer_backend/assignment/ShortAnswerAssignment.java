package com.heroengineer.hero_engineer_backend.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of an in-class writing prompt based assignment where the Professor
 * assigns a grade and feedback, rather than the program auto-grading each answer objectively.
 * <p></p>
 * This is the template version. See {@link GradedShortAnswerAssignment} for the version that a student already took.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShortAnswerAssignment {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public List<ShortAnswerQuestion> questions;
    @Getter @Setter
    public List<String> sectionIdsAvailableFor; // List of ids of class sections for which students can view questions and submit answers
    @Getter @Setter
    public List<String> sectionIdsGradesAvailableFor; // List of ids of class sections for which students can view this assignment if it's graded
    @Getter @Setter
    public int maxXp;

}
