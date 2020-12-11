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
    public AvatarData avatarData;
    public AvatarDataColors avatarDataColors;
    public List<String> avatarUnlockedBodyZoneShapes;
    public int xp;
    public List<XpEntry> xpEntries;
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
                AvatarData avatarData,
                AvatarDataColors avatarDataColors,
                List<String> avatarUnlockedBodyZoneShapes,
                int xp,
                List<XpEntry> xpEntries,
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
                avatarData,
                avatarDataColors,
                avatarUnlockedBodyZoneShapes,
                xp,
                xpEntries,
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
                AvatarData avatarData,
                AvatarDataColors avatarDataColors,
                List<String> avatarUnlockedBodyZoneShapes,
                int xp,
                List<XpEntry> xpEntries,
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
        this.avatarData = avatarData;
        this.avatarDataColors = avatarDataColors;
        this.avatarUnlockedBodyZoneShapes = avatarUnlockedBodyZoneShapes;
        this.xp = xp;
        this.xpEntries = xpEntries;
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

    public void addXp(int changeAmount, String changeReason) {
        this.setXp(this.getXp() + changeAmount);
        if (this.getXpEntries() == null) this.xpEntries = new ArrayList<>();
        this.getXpEntries().add(new XpEntry(changeAmount, changeReason));
    }

}
