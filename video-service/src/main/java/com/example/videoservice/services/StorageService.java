package com.example.videoservice.services;

import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    public String uploadVideo(MultipartFile file, String bucketName);
    public ResponseEntity<InputStreamResource> getVideo(String fileName, String bucketName);
    public String getPreSignedURL(String fileName, String bucketName);
    public List<String> listItems(String bucketName);
}
