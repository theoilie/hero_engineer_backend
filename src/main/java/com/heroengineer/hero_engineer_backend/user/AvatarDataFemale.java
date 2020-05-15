package com.heroengineer.hero_engineer_backend.user;

import lombok.Getter;
import lombok.Setter;

/**
 * MongoDB representation of a female avatar
 */
@Getter @Setter
public class AvatarDataFemale {

    public int backs;
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
    public int nose;

    public AvatarDataFemale() {}

    public AvatarDataFemale(int backs, int chinshadow, int clothes, int ears, int eyebrows, int eyesback, int eyesfront, int eyesiris, int facehighlight, int faceshape, int glasses, int hair, int humanbody, int mouth, int nose) {
        this.backs = backs;
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
        this.nose = nose;
    }
}
