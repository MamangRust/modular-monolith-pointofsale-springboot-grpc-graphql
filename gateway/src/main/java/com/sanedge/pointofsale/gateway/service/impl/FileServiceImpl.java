package com.sanedge.pointofsale.gateway.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sanedge.pointofsale.gateway.service.FileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String createFileImage(MultipartFile file, String filepath) {
        try {
            Path destinationPath = Path.of(filepath);

            Files.createDirectories(destinationPath.getParent());

            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("📸 File created: {}", filepath);
            return filepath;
        } catch (IOException e) {
            log.error("❌ Failed to create file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String createFileImage(byte[] content, String filepath) {
        try {
            Path destinationPath = Path.of(filepath);

            Files.createDirectories(destinationPath.getParent());

            Files.write(destinationPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("📸 File created: {}", filepath);
            return filepath;
        } catch (IOException e) {
            log.error("❌ Failed to create file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteFileImage(String filepath) {
        try {
            Path filePath = Path.of(filepath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("✅ Deleted file: {}", filepath);
            } else {
                log.warn("⚠️ File does not exist: {}", filepath);
            }
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", e.getMessage(), e);
        }
    }
}