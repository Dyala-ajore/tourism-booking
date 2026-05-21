package com.youruni.tourismbooking.common;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp")
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; 
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    public String uploadHotelImage(MultipartFile file) {
        return uploadImage(file, "hotels");
    }
    public String uploadRoomTypeImage(MultipartFile file) {
        return uploadImage(file, "room-types");
    }
    private String uploadImage(MultipartFile file, String folder) {
        validateFile(file);
        String extension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String relativePath = folder + "/" + uniqueFilename;
        try {
            Path uploadPath = Paths.get(uploadDir, folder).toAbsolutePath();
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 5MB");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type. Allowed types: jpg, jpeg, png, webp. Received: " + extension
            );
        }
    }
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}