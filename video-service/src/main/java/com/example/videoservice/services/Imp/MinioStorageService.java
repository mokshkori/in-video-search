package com.example.videoservice.services.Imp;

import com.example.videoservice.services.StorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService implements StorageService {
    private final MinioClient minioClient;
    private final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);
    @Autowired
    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadVideo(MultipartFile file, String bucketName) {
        try {
            logger.info("Uploading video");
            logger.info("Bucket name: " + bucketName);
            logger.info("File name: " + file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            logger.info("Video uploaded");
            return "File uploaded successfully: " + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading video to MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> getVideo(String fileName, String bucketName) {
        try {
            InputStream videoStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(videoStream));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving video from MinIO: " + e.getMessage(), e);
        }
    }
    @Override
    public String getPreSignedURL(String fileName, String bucketName) {
        try {
            logger.info("Getting pre-signed URL");
            logger.info("File name: " + fileName);
            logger.info("Bucket name: " + bucketName);
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error generating URL: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listItems(String bucketName) {
        try {
            Iterable<Result<Item>> buckets = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build()
            );
            List<String> resultList = new ArrayList<>();
            for (Result<Item> result : buckets) {
                Item item = result.get();
                resultList.add(item.objectName());
            }
            return resultList;
        } catch (Exception e) {
            throw new RuntimeException("Error listing objects: " + e.getMessage(), e);
        }
    }
}
