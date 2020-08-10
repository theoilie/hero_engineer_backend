package com.heroengineer.hero_engineer_backend.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * A question that's part of a {@link ShortAnswerAssignment}.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShortAnswerQuestion {

    @Id
    public String id;
    public String question;

}
