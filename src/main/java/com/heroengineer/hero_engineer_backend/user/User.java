package com.heroengineer.hero_engineer_backend.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * MongoDB representation of a student.
 */
public class User implements UserDetails {
    @Id @Getter @JsonIgnore
    public String id;
    // TODO: Improve the regular expression
    @Getter @Setter
    @Email(regexp="[^@]+@usc.edu", message="USC email is not valid")
    public String email;
    @Getter @Setter
    public String username;
    @Getter @Setter
    public String password;
    @Getter @Setter
    public String heroId;
    @Getter @Setter
    public int xp;
    @Getter @Setter
    public int points;
    @Getter @Setter
    public List<Quest> quests;
    @Getter @Setter
    public boolean isProf;
    @Getter @Setter
    public String role;

    private Collection<? extends GrantedAuthority> authorities;

    public User() {}

    public User(String email,
                String username,
                String password,
                String heroId,
                int xp,
                int points,
                List<Quest> quests,
                boolean isProf) {
        this(email, username, password, heroId, xp, points, quests, isProf, "student");
    }

    public User(String email,
                String username,
                String password,
                String heroId,
                int xp,
                int points,
                List<Quest> quests,
                boolean isProf,
                String role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.heroId = heroId;
        this.xp = xp;
        this.points = points;
        this.quests = quests;
        this.isProf = isProf;

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        this.authorities = authorities;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, email=%s, username=%s, heroId=%s, isProf=%b]", id, email, username, heroId, isProf);
    }
}
