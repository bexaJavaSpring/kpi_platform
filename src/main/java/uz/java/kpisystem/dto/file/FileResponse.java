package uz.java.kpisystem.dto.file;

import lombok.Data;

@Data
public class FileResponse {
    private String fileUrl;
    private String fileName;
    private String contentType;
    private Long size;
}
