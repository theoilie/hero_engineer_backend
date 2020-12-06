package com.heroengineer.hero_engineer_backend.util.file;

import com.itextpdf.kernel.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path uploadsLocation;
    private final Path reportsLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.uploadsLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.reportsLocation = Paths.get(fileStorageProperties.getReportsDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadsLocation);
            Files.createDirectories(this.reportsLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeHeroCouncilFile(String heroCouncilId, MultipartFile file) {
        return storeFile(heroCouncilId, file, this.uploadsLocation);
    }

    public PdfWriter getNewReportsPdfWriter(String fileName) throws FileNotFoundException {
        // Delete the file if it exists already
        File file = this.reportsLocation.resolve(fileName).toFile();
        if (file.exists()) file.delete();

        return new PdfWriter(file.getAbsolutePath());
    }

    public String storeFile(String fileName, MultipartFile file, Path path) {
        // Normalize file name
        String extension = "";
        if (file.getOriginalFilename() != null) {
            String[] split = file.getOriginalFilename().split("\\.");
            if (split.length > 0) {
                extension = "." + split[split.length - 1];
            }
        }
        fileName = fileName + extension;

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = path.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file '" + fileName + "'. Please try again.", ex);
        }
    }

    public Resource loadHeroCouncilFileAsResource(String fileName) {
        Path filePath = this.uploadsLocation.resolve(fileName).normalize();
        return loadFileAsResource(filePath);
    }

    public Resource loadReportsFileAsResource(String fileName) {
        Path filePath = this.reportsLocation.resolve(fileName).normalize();
        return loadFileAsResource(filePath);
    }

    public Resource loadFileAsResource(Path filePath) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + filePath.toString());
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + filePath.toString(), ex);
        }
    }

}
