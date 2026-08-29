package com.abhishek.task_management.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation =
            Paths.get("uploads/attachments");

    public FileStorageService() {

        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }

    public String store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        String originalFileName =
                file.getOriginalFilename();

        String extension = "";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension =
                    originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );
        }

        String storedFileName =
                UUID.randomUUID() + extension;

        Path targetLocation =
                storageLocation.resolve(storedFileName);

        try {

            Files.copy(
                    file.getInputStream(),
                    targetLocation
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not store file",
                    e
            );
        }

        return targetLocation.toString();
    }

    public void delete(String filePath) {

        try {
            Files.deleteIfExists(
                    Paths.get(filePath)
            );
        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not delete file",
                    e
            );
        }
    }
}