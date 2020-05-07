package com.heroengineer.hero_engineer_backend.hero;

import com.heroengineer.hero_engineer_backend.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

// TODO: Add origins value so that only the web server (and not students) can use REST

/**
 * REST controller for heroes.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/hero")
public class HeroController {

    private final HeroRepository repo;
    private final UserService userService;

    @Autowired
    public HeroController(HeroRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    @GetMapping("/heroes")
    public List<Hero> getAll() {
        return repo.findAll();
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveHero(HttpServletRequest request, @Valid @RequestBody Hero hero) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        repo.save(hero);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeHero(HttpServletRequest request, @PathVariable String id) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        repo.deleteById(id);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }
}
