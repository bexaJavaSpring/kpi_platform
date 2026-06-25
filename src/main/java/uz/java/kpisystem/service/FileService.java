package uz.java.kpisystem.service;

import io.minio.StatObjectResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.java.kpisystem.dto.file.FileStat;
import uz.java.kpisystem.exception.FileStorageException;

import java.util.Objects;

@Service
@Slf4j
public class FileService {

    private final MinioService minioService;

    public FileService(MinioService minioService) {
        this.minioService = minioService;
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty())
            throw new FileStorageException("file.invalid.path");

        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename())
        );

        if (originalFilename.contains(".."))
            throw new ValidationException("Failed to store file with relative path");

        String contentType = Objects.requireNonNull(file.getContentType());
        String path;
        if (contentType.startsWith("image") && !contentType.contains("svg+xml")) {
            MultipartFile fileToUpload = file;
            path = minioService.saveFile(fileToUpload, originalFilename);
        } else {
            path = minioService.saveFile(file, originalFilename);
        }
        return path;
    }

    public String getPresignedUrl(String objectName) {
        if (!StringUtils.hasText(objectName))
            return null;
        try {
            return minioService.generatePresignedUrl(objectName);
        } catch (Exception e) {
            // o'qish (getAll/getOne) paytida URL yaratib bo'lmasa, butun javobni buzmaymiz
            log.warn("Presigned URL yaratib bo'lmadi: {}", objectName, e);
            return null;
        }
    }

    public boolean exists(String objectName) {
        return minioService.objectExists(objectName);
    }

    public FileStat stat(String objectName) {
        StatObjectResponse s = minioService.statObject(objectName);
        if (s == null)
            return null;
        String name = objectName.substring(objectName.lastIndexOf('/') + 1);
        return new FileStat(name, s.size(), s.contentType());
    }

    public void deleteFile(String objectName) {
        minioService.removeObject(objectName);
    }
}
