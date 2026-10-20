package com.sanedge.pointofsale.gateway.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String createFileImage(MultipartFile file, String filepath);

    String createFileImage(byte[] content, String filepath);

    void deleteFileImage(String filepath);
}