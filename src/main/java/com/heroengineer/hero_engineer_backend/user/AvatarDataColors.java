package com.heroengineer.hero_engineer_backend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MongoDB representation of a the HEX color of each body zone
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarDataColors {

    public String backs;
    public String skin;
    public String mouth;
    public String clothes;
    public String eyebrows;
    public String eyesback;
    public String eyesfront;
    public String eyesiris;
    public String glasses;
    public String hair;
    public String mustache;

}
