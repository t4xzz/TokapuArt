package com.tokapuart.controller;

import com.tokapuart.dto.ApiResponse;
import com.tokapuart.dto.UserProfileResponse;
import com.tokapuart.model.User;
import com.tokapuart.repository.UserRepository;
import com.tokapuart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            UserProfileResponse profile = userService.getCurrentUserProfile(userId);
            return ResponseEntity.ok(ApiResponse.success("Perfil obtenido", profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            UserProfileResponse profile = userService.getUserProfile(userId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Perfil obtenido", profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestBody UserProfileResponse profileData,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            UserProfileResponse profile = userService.updateProfile(userId, profileData);
            return ResponseEntity.ok(ApiResponse.success("Perfil actualizado", profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            userService.followUser(userId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Ahora sigues a este usuario", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            userService.unfollowUser(userId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Dejaste de seguir a este usuario", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            if (photo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("No se proporcionó ninguna foto"));
            }

            String photoUrl = userService.uploadProfilePhoto(userId, photo);
            return ResponseEntity.ok(ApiResponse.success("Foto de perfil actualizada", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al subir foto: " + e.getMessage()));
        }
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }
}
