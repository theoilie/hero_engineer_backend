package com.heroengineer.hero_engineer_backend.avatars;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

/**
 * REST controller for avatars.
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/avatars")
public class AvatarsController {

    @Qualifier("webApplicationContext")
    @Autowired
    ResourceLoader resourceLoader;

    @GetMapping(value="/json/svgavatars-male-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMaleData() {
        return getJson("avatars/svgavatars-male-data.json");
    }

    @GetMapping(value="/json/svgavatars-female-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFemaleData() {
        return getJson("avatars/svgavatars-female-data.json");
    }

    private String getJson(String fileName) {
        InputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = resourceLoader.getResource("classpath:/" + fileName).getInputStream();
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
