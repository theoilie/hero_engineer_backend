package com.heroengineer.hero_engineer_backend.hero;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * MongoDB representation of a Hero Engineer.
 */
public class Hero {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public String desc;

    public Hero() {}

    public Hero(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public Hero(String id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("Hero[id=%s, name=%s, desc=%s]", id, name, desc);
    }

}
