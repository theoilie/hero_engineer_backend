package com.heroengineer.hero_engineer_backend;

import com.heroengineer.hero_engineer_backend.hero.Hero;
import com.heroengineer.hero_engineer_backend.hero.HeroRepository;
import com.heroengineer.hero_engineer_backend.herocouncil.FileStorageProperties;
import com.heroengineer.hero_engineer_backend.herocouncil.GrandChallenge;
import com.heroengineer.hero_engineer_backend.herocouncil.GrandChallengeRepository;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quest.QuestRepository;
import com.heroengineer.hero_engineer_backend.quest.QuestService;
import com.heroengineer.hero_engineer_backend.quiz.Quiz;
import com.heroengineer.hero_engineer_backend.quiz.QuizAnswer;
import com.heroengineer.hero_engineer_backend.quiz.QuizQuestion;
import com.heroengineer.hero_engineer_backend.quiz.QuizRepository;
import com.heroengineer.hero_engineer_backend.section.Section;
import com.heroengineer.hero_engineer_backend.section.SectionRepository;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserWhitelist;
import com.heroengineer.hero_engineer_backend.user.UserWhitelistRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bson.codecs.ObjectIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.jsonwebtoken.security.Keys.secretKeyFor;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class HeroEngineerBackendApplication {

    @Autowired
    HeroEngineerBackendApplication(UserRepository userRepo,
                                   UserWhitelistRepository userWhitelistRepo,
                                   HeroRepository heroRepo,
                                   QuestRepository questRepo,
                                   QuizRepository quizRepo,
                                   SectionRepository sectionRepo,
                                   GrandChallengeRepository grandChallengeRepo,
                                   QuestService questService) {

        // Insert starter data if the database is empty
        if (grandChallengeRepo.count() == 0) {
            grandChallengeRepo.save(new GrandChallenge("sustainability", "SUSTAINABILITY", "Sustainability"));
            grandChallengeRepo.save(new GrandChallenge("health", "HEALTH", "Health"));
            grandChallengeRepo.save(new GrandChallenge("security", "SECURITY", "Security"));
            grandChallengeRepo.save(new GrandChallenge("joy_of_living", "JOY_OF_LIVING", "Joy of Living"));
        }
        if (userWhitelistRepo.count() == 0
                || userWhitelistRepo.findById("default").isEmpty()
                | userWhitelistRepo.findById("default").get().getEmails().isEmpty()) {
            userWhitelistRepo.insert(new UserWhitelist("default", Collections.singletonList("admin@usc.edu")));
        }
        if (userRepo.count() == 0) {
            if (heroRepo.count() == 0) {
                heroRepo.insert(new Hero("Example Hero Name", "Example Hero Description"));
                heroRepo.insert(new Hero("Second Hero Name", "Second Hero Description"));
            }
            userRepo.save(new User(
                    "admin@usc.edu",
                    "admin",
                    new BCryptPasswordEncoder().encode("{?)ud/q([2,<3{MLXRpXkQ9]g##WSQ4Q"),
                    heroRepo.findByNameIgnoreCase("Example Hero Name").getId(),
                    "",
                    null,
                    null,
                    null,
                    0,
                    0,
                    new ArrayList<>(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    true
            ));
        }
        if (sectionRepo.count() == 0) {
            sectionRepo.insert(new Section("admin", "admin", Collections.singletonList("admin@usc.edu")));
        }
        if (questRepo.count() == 0 && quizRepo.count() == 0) {
            Quiz quiz1 = new Quiz(
                    new ObjectIdGenerator().generate().toString(),
                    "Randy Pausch Review",
                    false,
                    false,
                    2,
                    Arrays.asList(
                            new QuizQuestion(
                                    new ObjectIdGenerator().generate().toString(),
                                "What is Randy Pausch's first name?",
                                1,
                                    Arrays.asList(
                                        new QuizAnswer(
                                                new ObjectIdGenerator().generate().toString(),
                                            "Randy",
                                            true
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Pausch",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Theo",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Harly",
                                                    false
                                            )
                                    )
                            ),
                            new QuizQuestion(
                                    new ObjectIdGenerator().generate().toString(),
                                    "What is Randy Pausch's last name?",
                                    1,
                                    Arrays.asList(
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Randy",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Pausch",
                                                    true
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Theo",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Harly",
                                                    false
                                            )
                                    )
                            ),
                            new QuizQuestion(
                                    new ObjectIdGenerator().generate().toString(),
                                    "What is Randy Pausch's middle name?",
                                    1,
                                    Arrays.asList(new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Randy",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Frederick",
                                                    true
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Theo",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Harly",
                                                    false
                                            )
                                    )
                            )
                    )
            );
            Quiz quiz2 = new Quiz(
                    new ObjectIdGenerator().generate().toString(),
                    "Randy Pausch Extras",
                    true,
                    false,
                    1,
                    Arrays.asList(
                            new QuizQuestion(
                                    new ObjectIdGenerator().generate().toString(),
                                    "Where was Randy Pausch born?",
                                    1,
                                    Arrays.asList(
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Maryland",
                                                    true
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Oklahoma",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Washington",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "California",
                                                    false
                                            )
                                    )
                            ),
                            new QuizQuestion(
                                    new ObjectIdGenerator().generate().toString(),
                                    "What was Randy Pausch's favorite movie or TV series?",
                                    1,
                                    Arrays.asList(
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Star Wars",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "The Oprah Winfrey Show",
                                                    false
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "Star Trek",
                                                    true
                                            ),
                                            new QuizAnswer(
                                                    new ObjectIdGenerator().generate().toString(),
                                                    "The Flintstones",
                                                    false
                                            )
                                    )
                            )
                    )
            );
            quizRepo.save(quiz1);
            quizRepo.save(quiz2);

            Quest quest1 = new Quest(
                    new ObjectIdGenerator().generate().toString(),
                    "Randy Pausch Review",
                    "Randy Pausch is an inspiration to many and a role model of a Hero Engineer. As part of your Hero's journey, answer some questions that will help you think about what service means to a hero.",
                    50,
                    true,
                    false,
                    true,
                    false,
                    false,
                    "",
                    "",
                    Collections.singletonList(quiz1.getId()),
                    null,
                    null
            );
            questService.saveQuest(quest1);
            questService.saveQuest(new Quest(
                    "Randy Pausch Deeper Dive",
                    "To optionally further improve your skills from the initial \"Randy Pausch Review\" main quest, accept this quest. Your Hero will test their ability in further exploring the roles of entertainment and social skills as heroic traits.",
                    5,
                    false,
                    false,
                    true,
                    true,
                    true,
                    "",
                    "",
                    Collections.singletonList(quiz2.getId()),
                    null,
                    Collections.singletonList(quest1.getId())
            ));
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey secretKey() {
        return secretKeyFor(SignatureAlgorithm.HS512);
    }

    public static void main(String[] args) {
        SpringApplication.run(HeroEngineerBackendApplication.class, args);
    }

}
