package com.heroengineer.hero_engineer_backend.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.Email;
import java.util.Collection;
import java.util.List;

/**
 * MongoDB representation of a whitelist of emails allowed to sign up.
 */
public class UserWhitelist {
    @Id @Getter @JsonIgnore
    public String id;
    // TODO: Improve the regular expression
    @Getter @Setter
    @Email(regexp="[^@]+@usc.edu", message="USC email is not valid")
    public List<String> emails;

    private Collection<? extends GrantedAuthority> authorities;

    public UserWhitelist() {}

    public UserWhitelist(List<String> emails) {
        this.emails = emails;
    }

    public UserWhitelist(String id, List<String> emails) {
        this.id = id;
        this.emails = emails;
    }

}
