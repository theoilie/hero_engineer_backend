package com.heroengineer.hero_engineer_backend.user;

import lombok.Getter;
import lombok.Setter;

/**
 * MongoDB representation of a the colors of each body part
 */
@Getter @Setter
public class AvatarDataColors {

    public int backs;
    public int beard;
    public int chinshadow;
    public int clothes;
    public int ears;
    public int eyebrows;
    public int eyesback;
    public int eyesfront;
    public int eyesiris;
    public int facehighlight;
    public int faceshape;
    public int glasses;
    public int hair;
    public int humanbody;
    public int mouth;
    public int mustache;
    public int nose;

    public AvatarDataColors() {}

    public AvatarDataColors(int backs, int beard, int chinshadow, int clothes, int ears, int eyebrows, int eyesback, int eyesfront, int eyesiris, int facehighlight, int faceshape, int glasses, int hair, int humanbody, int mouth, int mustache, int nose) {
        this.backs = backs;
        this.beard = beard;
        this.chinshadow = chinshadow;
        this.clothes = clothes;
        this.ears = ears;
        this.eyebrows = eyebrows;
        this.eyesback = eyesback;
        this.eyesfront = eyesfront;
        this.eyesiris = eyesiris;
        this.facehighlight = facehighlight;
        this.faceshape = faceshape;
        this.glasses = glasses;
        this.hair = hair;
        this.humanbody = humanbody;
        this.mouth = mouth;
        this.mustache = mustache;
        this.nose = nose;
    }
}
