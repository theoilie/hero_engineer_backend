package com.heroengineer.hero_engineer_backend.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * A question that's part of a {@link GradedShortAnswerAssignment} -- a student has already submitted an assignment with this question.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GradedShortAnswerQuestion {

    @Id
    public String id;
    public String question;
    public String answer;

}
