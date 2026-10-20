package com.sanedge.pointofsale.gateway.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.gateway.service.FolderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FolderServiceImpl implements FolderService {

    @Override
    public String createFolder(String basePath, String name) {
        String folderPath = basePath + File.separator + name;

        File folder = new File(folderPath);

        if (!folder.exists() && !folder.mkdirs()) {
            log.error("❌ Failed to create directory: {}", folderPath);
            return null;
        }

        log.info("📂 Created folder: {}", folderPath);
        return folderPath;
    }

    @Override
    public void deleteFolder(String basePath, String name) {
        String folderPath = basePath + File.separator + name;

        File folder = new File(folderPath);

        if (!folder.exists()) {
            log.warn("⚠️ Directory does not exist: {}", folderPath);
            return;
        }

        if (!deleteRecursive(folder)) {
            log.error("❌ Failed to delete directory: {}", folderPath);
        } else {
            log.info("✅ Deleted directory: {}", folderPath);
        }
    }

    private static boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (!deleteRecursive(f)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }
}