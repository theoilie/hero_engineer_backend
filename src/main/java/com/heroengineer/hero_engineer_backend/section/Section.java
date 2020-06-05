package com.heroengineer.hero_engineer_backend.section;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a Section (aka class).
 */
public class Section {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public List<String> emails; // Emails of students in this class/section

    public Section() {}

    public Section(String name, List<String> emails) {
        this.name = name;
        this.emails = emails;
    }

    public Section(String id, String name, List<String> emails) {
        this.id = id;
        this.name = name;
        this.emails = emails;
    }

    @Override
    public String toString() {
        return String.format("Section[id=%s, name=%s]", id, name, String.join(",", emails));
    }

}
