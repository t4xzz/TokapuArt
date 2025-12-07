package com.tokapuart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final Cloudinary cloudinary;

    @Value("${upload.directory}")
    private String uploadDir;

    @Value("${cloudinary.enabled:true}")
    private boolean cloudinaryEnabled;

    /**
     * Almacena un archivo en Cloudinary o en el sistema de archivos local
     * según la configuración.
     */
    public String storeFile(MultipartFile file) {
        try {
            if (cloudinaryEnabled) {
                return storeInCloudinary(file);
            } else {
                return storeLocally(file);
            }
        } catch (IOException ex) {
            log.error("Error al guardar el archivo: {}", ex.getMessage());
            throw new RuntimeException("Error al guardar el archivo: " + ex.getMessage());
        }
    }

    /**
     * Almacena el archivo en Cloudinary y devuelve la URL pública
     */
    private String storeInCloudinary(MultipartFile file) throws IOException {
        // Generar un identificador único para la imagen
        String publicId = "tokapuart/" + UUID.randomUUID();

        // Subir a Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", "tokapuart",
                        "resource_type", "auto",
                        "quality", "auto:good",
                        "fetch_format", "auto"
                )
        );

        // Obtener la URL segura (HTTPS)
        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("Archivo subido a Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Almacena el archivo localmente (para desarrollo)
     */
    private String storeLocally(MultipartFile file) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID() + fileExtension;

        // Guardar archivo
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("Archivo guardado localmente: /uploads/{}", fileName);
        return "/uploads/" + fileName;
    }

    /**
     * Elimina un archivo de Cloudinary o del sistema local
     */
    public void deleteFile(String fileUrl) {
        try {
            if (cloudinaryEnabled && fileUrl != null && fileUrl.contains("cloudinary.com")) {
                deleteFromCloudinary(fileUrl);
            } else if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                deleteLocally(fileUrl);
            }
        } catch (Exception ex) {
            log.error("Error al eliminar el archivo: {}", ex.getMessage());
            throw new RuntimeException("Error al eliminar el archivo: " + ex.getMessage());
        }
    }

    /**
     * Elimina un archivo de Cloudinary
     */
    private void deleteFromCloudinary(String fileUrl) throws IOException {
        // Extraer el public_id de la URL de Cloudinary
        // URL ejemplo: https://res.cloudinary.com/cloud-name/image/upload/v123456/tokapuart/uuid.jpg
        String[] parts = fileUrl.split("/");
        if (parts.length >= 2) {
            // Encontrar la parte después de "upload/"
            boolean foundUpload = false;
            StringBuilder publicId = new StringBuilder();
            for (String part : parts) {
                if (foundUpload && !part.startsWith("v")) {
                    if (publicId.length() > 0) {
                        publicId.append("/");
                    }
                    // Remover extensión
                    publicId.append(part.replaceFirst("\\.[^.]+$", ""));
                }
                if (part.equals("upload")) {
                    foundUpload = true;
                }
            }

            if (publicId.length() > 0) {
                cloudinary.uploader().destroy(publicId.toString(), ObjectUtils.emptyMap());
                log.info("Archivo eliminado de Cloudinary: {}", publicId);
            }
        }
    }

    /**
     * Elimina un archivo del sistema local
     */
    private void deleteLocally(String fileUrl) throws IOException {
        String fileName = fileUrl.substring("/uploads/".length());
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.deleteIfExists(filePath);
        log.info("Archivo eliminado localmente: {}", fileName);
    }
}