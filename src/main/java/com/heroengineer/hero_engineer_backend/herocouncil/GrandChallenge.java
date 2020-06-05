package com.heroengineer.hero_engineer_backend.herocouncil;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * MongoDB representation of a grand challenge.
 * Note: These are actually categories of grand challenges.
 */
public class GrandChallenge {

    @Id @Getter
    public String id;
    @Getter @Setter
    public String code;
    @Getter @Setter
    public String grandChallenge;

    public GrandChallenge() {}

    public GrandChallenge(String code, String grandChallenge) {
        this.code = code;
        this.grandChallenge = grandChallenge;
    }

    public GrandChallenge(String id, String code, String grandChallenge) {
        this.id = id;
        this.code = code;
        this.grandChallenge = grandChallenge;
    }

    @Override
    public String toString() {
        return String.format("GrandChallenge[id=%s, code=%s, grandChallenge=%s]", id, code, grandChallenge);
    }

}
