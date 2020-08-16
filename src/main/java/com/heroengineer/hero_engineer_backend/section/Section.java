package com.heroengineer.hero_engineer_backend.section;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a Section (aka class).
 */
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    @Id
    public String id;
    public String name;
    public List<String> emails; // Emails of students in this class/section

    public Section(String name, List<String> emails) {
        this.name = name;
        this.emails = emails;
    }

}
