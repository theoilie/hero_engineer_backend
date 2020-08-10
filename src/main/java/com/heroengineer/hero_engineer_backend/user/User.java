package com.heroengineer.hero_engineer_backend.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heroengineer.hero_engineer_backend.assignment.GradedShortAnswerAssignment;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@NoArgsConstructor
@ToString
@Data
public class User implements UserDetails {

    @Id @JsonIgnore
    public String id;
    // TODO: Improve the regular expression
    @Email(regexp="[^@]+@usc.edu", message="USC email is not valid")
    public String email;
    public String username;
    public String password;
    public String heroId;
    public String avatarSVG;
    public AvatarDataMale avatarDataMale;
    public AvatarDataFemale avatarDataFemale;
    public AvatarDataColors avatarDataColors;
    public int xp;
    public int points;
    public List<Quest> quests;
    public List<GradedShortAnswerAssignment> gradedShortAnswerAssignments;
    public String grandChallengeCategory;
    public String grandChallengeCode;
    public String idea1;
    public String idea2;
    public String idea3;
    public boolean isProf;
    public boolean resetPasswordOnLogin;
    public String role;

    private Collection<? extends GrantedAuthority> authorities;

    public User(String email,
                String username,
                String password,
                String heroId,
                String avatarSVG,
                AvatarDataMale avatarDataMale,
                AvatarDataFemale avatarDataFemale,
                AvatarDataColors avatarDataColors,
                int xp,
                int points,
                List<Quest> quests,
                List<GradedShortAnswerAssignment> gradedShortAnswerAssignments,
                String grandChallengeCategory,
                String grandChallengeCode,
                String idea1,
                String idea2,
                String idea3,
                boolean isProf,
                boolean resetPasswordOnLogin) {
        this(
                email,
                username,
                password,
                heroId,
                avatarSVG,
                avatarDataMale,
                avatarDataFemale,
                avatarDataColors,
                xp,
                points,
                quests,
                gradedShortAnswerAssignments,
                grandChallengeCategory,
                grandChallengeCode,
                idea1,
                idea2,
                idea3,
                isProf,
                resetPasswordOnLogin,
                "student"
        );
    }

    public User(String email,
                String username,
                String password,
                String heroId,
                String avatarSVG,
                AvatarDataMale avatarDataMale,
                AvatarDataFemale avatarDataFemale,
                AvatarDataColors avatarDataColors,
                int xp,
                int points,
                List<Quest> quests,
                List<GradedShortAnswerAssignment> gradedShortAnswerAssignments,
                String grandChallengeCategory,
                String grandChallengeCode,
                String idea1,
                String idea2,
                String idea3,
                boolean isProf,
                boolean resetPasswordOnLogin,
                String role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.heroId = heroId;
        this.avatarSVG = avatarSVG;
        this.avatarDataMale = avatarDataMale;
        this.avatarDataFemale = avatarDataFemale;
        this.avatarDataColors = avatarDataColors;
        this.xp = xp;
        this.points = points;
        this.quests = quests;
        this.gradedShortAnswerAssignments = gradedShortAnswerAssignments;
        this.grandChallengeCategory = grandChallengeCategory;
        this.grandChallengeCode = grandChallengeCode;
        this.idea1 = idea1;
        this.idea2 = idea2;
        this.idea3 = idea3;
        this.isProf = isProf;
        this.resetPasswordOnLogin = resetPasswordOnLogin;

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

}
