package com.heroengineer.hero_engineer_backend.herocouncil;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a Hero Council.
 */
public class HeroCouncil {

    @Id @Getter @Setter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public List<String> emails; // Emails of students in this Hero Council
    @Getter @Setter
    public boolean approved; // Whether or not the professor has approved this Hero Council
    @Getter @Setter
    public String declarationFileName;

    public HeroCouncil() {}

    public HeroCouncil(String name, List<String> emails, boolean approved, String declarationFileName) {
        this.name = name;
        this.emails = emails;
        this.approved = approved;
        this.declarationFileName = declarationFileName;
    }

    public HeroCouncil(String id, String name, List<String> emails, boolean approved, String declarationFileName) {
        this.id = id;
        this.name = name;
        this.emails = emails;
        this.approved = approved;
        this.declarationFileName = declarationFileName;
    }

    @Override
    public String toString() {
        return String.format("HeroCouncil[id=%s, name=%s, emails=%s, approved=%s, declarationFileName=%s]",
                id, name, String.join(",", emails), approved, declarationFileName);
    }

}
