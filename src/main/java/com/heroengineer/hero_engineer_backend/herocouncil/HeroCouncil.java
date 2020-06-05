package com.heroengineer.hero_engineer_backend.herocouncil;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a Hero Council.
 */
public class HeroCouncil {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public List<String> emails; // Emails of students in this Hero Council

    public HeroCouncil() {}

    public HeroCouncil(String name, List<String> emails) {
        this.name = name;
        this.emails = emails;
    }

    public HeroCouncil(String id, String name, List<String> emails) {
        this.id = id;
        this.name = name;
        this.emails = emails;
    }

    @Override
    public String toString() {
        return String.format("HeroCouncil[id=%s, name=%s, emails=%s]", id, name, String.join(",", emails));
    }

}
