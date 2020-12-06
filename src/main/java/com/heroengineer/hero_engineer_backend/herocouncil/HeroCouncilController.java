package com.heroengineer.hero_engineer_backend.herocouncil;

import com.heroengineer.hero_engineer_backend.jwt.JwtTokenUtil;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import com.heroengineer.hero_engineer_backend.util.UtilService;
import com.heroengineer.hero_engineer_backend.util.file.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final FileStorageService fileStorageService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UtilService util;

    @Autowired
    public HeroCouncilController(HeroCouncilRepository repo,
                                 UserRepository userRepo,
                                 GrandChallengeRepository grandChallengeRepo,
                                 UserService userService,
                                 FileStorageService fileStorageService,
                                 JwtTokenUtil jwtTokenUtil,
                                 UtilService util) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.grandChallengeRepo = grandChallengeRepo;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
            if (heroCouncil.getEmails().stream().anyMatch(email1 -> email1.equalsIgnoreCase(email))) {
                return ResponseEntity.ok().body(heroCouncil);
            }
        }
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(HttpServletRequest request, @RequestBody @Valid HeroCouncil council) {
        String userEmail = jwtTokenUtil.getUsernameFromRequest(request);

        if (council.getAnnouncements() == null) council.setAnnouncements(new ArrayList<>());

        if (userService.isProf(request)) {
            if (council.getId() != null && !council.getId().isBlank()) {
                // Save pre-existing council
                repo.save(council);
            } else {
                // No ID given -- find pre-existing council and update it
                updateCouncil(council, userEmail);
            }
        } else {
            // The only time a student (not professor) can save is when they're creating a new council
            if (council.getId() != null && !council.getId().isBlank()) {
                return ResponseEntity.badRequest().body(null);
            }
            updateCouncil(council, userEmail);
        }

        return ResponseEntity.ok().body(null);
    }

    private void updateCouncil(HeroCouncil council, String userEmail) {
        for (HeroCouncil otherCouncil : repo.findAll()) {
            // Make sure the student is in the council that he/she is creating
            // Find the ID from the Hero Council that was created from file/Declaration upload
            if (otherCouncil.getEmails().stream().anyMatch(email -> email != null && email.equalsIgnoreCase(userEmail))) {
                council.setId(otherCouncil.getId());
                council.setDeclarationFileName(otherCouncil.getDeclarationFileName());
                repo.save(council);
                break;
            }
        }

        // Doesn't match any other council -- make a new council
        repo.save(council);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> remove(HttpServletRequest request, @PathVariable String id) {
        if (userService.isProf(request)) {
            repo.deleteById(id);
            return ResponseEntity.ok().body("{\"error\": \"\"}");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }
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

    @PostMapping("/enterGrandChallengeCode")
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

    @PutMapping("/generateGrandChallengeCode")
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

    @PutMapping("/generateHeroCouncilCode")
    public ResponseEntity<String> generateCodeForHeroCouncil(HttpServletRequest request, @Valid @RequestBody GenerateHeroCouncilCodeRequest body) {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        HeroCouncil heroCouncil = repo.findById(body.getHeroCouncilId()).orElse(null);
        if (heroCouncil == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"No hero council could be found with the given ID.\"}");
        }

        if (heroCouncil.getQuestInfos() == null) heroCouncil.setQuestInfos(new ArrayList<>());

        for (HeroCouncil.QuestInfo questInfo : heroCouncil.getQuestInfos()) {
            if (questInfo.getQuestId().equals(body.getQuestId())) {
                questInfo.setCode(util.generateCode());
                repo.save(heroCouncil);
                return ResponseEntity.ok().body("{\"error\": \"\"}");
            }
        }

        heroCouncil.getQuestInfos().add(new HeroCouncil.QuestInfo(body.getQuestId(), util.generateCode()));
        repo.save(heroCouncil);
        return ResponseEntity.ok().body("{\"error\": \"\"}");
    }

    @PostMapping(value = "/uploadDeclaration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadHeroCouncilDeclaration(HttpServletRequest request, @RequestParam MultipartFile file) throws IOException {
        String userEmail = jwtTokenUtil.getUsernameFromRequest(request);

        // Make a new Hero Council for the file if the user doesn't already have a council
        HeroCouncil council = null;
        for (HeroCouncil otherCouncil : repo.findAll()) {
            if (otherCouncil.getEmails().stream().anyMatch(email -> email != null && email.equalsIgnoreCase(userEmail))) {
                council = otherCouncil;
                break;
            }
        }
        if (council == null) {
            council = new HeroCouncil("", Collections.singletonList(userEmail.toLowerCase()), false, "");
        }
        council = repo.save(council);

        String fileName = fileStorageService.storeHeroCouncilFile(council.getId(), file);
        council.setDeclarationFileName(fileName);
        repo.save(council);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/downloadDeclaration/{fileName}")
    public ResponseEntity<?> downloadHeroCouncilDeclaration(HttpServletRequest request, @PathVariable String fileName) throws IOException {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        // Load file as Resource
        Resource resource = fileStorageService.loadHeroCouncilFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.getFile().length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EnterCodeRequest {
        public String code;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GenerateCodeRequest {
        public String grandChallengeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GenerateHeroCouncilCodeRequest {
        public String heroCouncilId;
        public String questId;
    }

}
