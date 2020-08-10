package com.heroengineer.hero_engineer_backend.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of an in-class writing prompt based assignment where the Professor
 * assigns a grade and feedback, rather than the program auto-grading each answer objectively.
 * <p></p>
 * This is the graded version. See {@link ShortAnswerAssignment} for the template version.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GradedShortAnswerAssignment {

    @Id
    public String id;
    public String name;
    public List<GradedShortAnswerQuestion> gradedQuestions;
    public boolean available; // Whether or not students can view the Professor's feedback for them
    public boolean graded;
    public int xpAwarded;
    public int maxXp;
    public String feedback; // The Professor's feedback to the student

}
