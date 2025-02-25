package com.example.videoservice.controller;

import com.example.videoservice.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final StorageService storageService;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Autowired
    public VideoController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String fileName = storageService.uploadVideo(file, bucketName);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadVideo(@PathVariable String fileName) {
        return storageService.getVideo(fileName, bucketName);
    }

    @GetMapping("/presigned-url/{fileName}")
    public ResponseEntity<String> getPresignedUrl(@PathVariable String fileName) {
        String url = storageService.getPreSignedURL(fileName, bucketName);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listItems() {
        List<String> list = storageService.listItems(bucketName);
        return ResponseEntity.ok(list);
    }
}