package com.tokapuart.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.tokapuart.dto.AuthResponse;
import com.tokapuart.model.User;
import com.tokapuart.repository.UserRepository;
import com.tokapuart.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            // Verificar el token con Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new RuntimeException("Token de Google inválido");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Obtener información del usuario
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String googleId = payload.getSubject();

            // Buscar o crear usuario
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createUserFromGoogle(email, name, pictureUrl, googleId));

            // Actualizar foto de perfil si cambió
            if (pictureUrl != null && !pictureUrl.equals(user.getProfilePhotoUrl())) {
                user.setProfilePhotoUrl(pictureUrl);
                userRepository.save(user);
            }

            // Generar JWT
            String token = jwtUtil.generateToken(email, user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

        } catch (Exception e) {
            log.error("Error al autenticar con Google", e);
            throw new RuntimeException("Error al autenticar con Google: " + e.getMessage());
        }
    }

    private User createUserFromGoogle(String email, String name, String pictureUrl, String googleId) {
        // Generar username único basado en el email
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .fullName(name != null ? name : username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Password aleatorio
                .profilePhotoUrl(pictureUrl)
                .isArtist(false)
                .isPublic(true)
                .build();

        return userRepository.save(user);
    }
}
