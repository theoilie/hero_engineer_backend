package com.heroengineer.hero_engineer_backend.avatars;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

// TODO: Add origins value so that only the web server (and not students) can use REST

/**
 * REST controller for avatars.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/avatars")
public class AvatarsController {

    @GetMapping(value="/json/svgavatars-male-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMaleData() {
        return getJson("src/main/resources/avatars/svgavatars-male-data.json");
    }

    @GetMapping(value="/json/svgavatars-female-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFemaleData() {
        return getJson("src/main/resources/avatars/svgavatars-female-data.json");
    }

    private static String getJson(String fileName) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(fileName);
            reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
