package com.heroengineer.hero_engineer_backend.herocouncil;

import com.heroengineer.hero_engineer_backend.hero.Hero;
import com.heroengineer.hero_engineer_backend.hero.HeroController;
import com.heroengineer.hero_engineer_backend.hero.HeroRepository;
import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quest.QuestController;
import com.heroengineer.hero_engineer_backend.section.Section;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import com.heroengineer.hero_engineer_backend.util.UtilService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for hero councils and grand challenges.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/herocouncil")
public class HeroCouncilController {

    private final HeroCouncilRepository repo;
    private final UserRepository userRepo;
    private final GrandChallengeRepository grandChallengeRepo;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UtilService util;

    @Autowired
    public HeroCouncilController(HeroCouncilRepository repo,
                                 UserRepository userRepo,
                                 GrandChallengeRepository grandChallengeRepo,
                                 UserService userService,
                                 JwtTokenUtil jwtTokenUtil,
                                 UtilService util) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.grandChallengeRepo = grandChallengeRepo;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.util = util;
    }

    @GetMapping("/herocouncils")
    public List<HeroCouncil> getAll() {
        return repo.findAll();
    }

    @GetMapping("/herocouncil")
    public ResponseEntity<?> get(HttpServletRequest request) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }
        for (HeroCouncil heroCouncil : repo.findAll()) {
            if (heroCouncil.getEmails().contains(email)) {
                return ResponseEntity.ok().body(heroCouncil);
            }
        }
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/grandChallenges")
    public List<GrandChallenge> getAllGrandChallenges(HttpServletRequest request) {
        if (userService.isProf(request)) {
            return grandChallengeRepo.findAll();
        } else {
            return grandChallengeRepo.findAll().stream().peek(challenge -> challenge.setCode("")).collect(Collectors.toList());
        }
    }

    @PutMapping("/saveGrandChallenge")
    public ResponseEntity<String> saveGrandChallenge(HttpServletRequest request, @Valid @RequestBody GrandChallenge grandChallenge) {
        if (userService.isProf(request)) {
            grandChallengeRepo.save(grandChallenge);
            return ResponseEntity.ok().body("{\"error\": \"\"}");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }
    }

    @PostMapping("/enterCode")
    public ResponseEntity<String> enterCode(HttpServletRequest request, @RequestBody EnterCodeRequest body) {
        String email = jwtTokenUtil.getUsernameFromRequest(request);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"JWT not accepted\"}");
        }

        Optional<GrandChallenge> grandChallenge = grandChallengeRepo.findByCode(body.getCode());
        if (grandChallenge.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid code\"}");
        }

        // Save the user's selected grand challenge category
        User user = userRepo.findByEmailIgnoreCase(email);
        user.setGrandChallengeCategory(grandChallenge.get().getGrandChallenge());
        user.setGrandChallengeCode(grandChallenge.get().getCode());
        userRepo.save(user);

        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PutMapping("/generateCode")
    public ResponseEntity<String> generateCodeForGrandChallenge(HttpServletRequest request, @Valid @RequestBody GenerateCodeRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        GrandChallenge grandChallenge = grandChallengeRepo.findById(body.getGrandChallengeId()).orElse(null);
        if (grandChallenge == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"No grand challenge could be found with the given ID.\"}");
        }

        grandChallenge.setCode(util.generateCode());
        grandChallengeRepo.save(grandChallenge);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    private static class EnterCodeRequest {

        @Getter @Setter
        public String code;

        public EnterCodeRequest() {}

        public EnterCodeRequest(String code) {
            this.code = code;
        }

    }

    private static class GenerateCodeRequest {

        @Getter @Setter
        public String grandChallengeId;

        public GenerateCodeRequest() {}

        public GenerateCodeRequest(String grandChallengeId) {
            this.grandChallengeId = grandChallengeId;
        }

    }

}
