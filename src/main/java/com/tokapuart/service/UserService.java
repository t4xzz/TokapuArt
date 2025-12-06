package com.tokapuart.service;

import com.tokapuart.dto.UserProfileResponse;
import com.tokapuart.model.User;
import com.tokapuart.repository.FollowerRepository;
import com.tokapuart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Boolean isFollowing = currentUserId != null &&
                followerRepository.existsByFollowerIdAndFollowingId(currentUserId, userId);

        return mapToProfileResponse(user, isFollowing);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mapToProfileResponse(user, null);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileResponse profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (profileData.getFullName() != null) {
            user.setFullName(profileData.getFullName());
        }
        if (profileData.getBio() != null) {
            user.setBio(profileData.getBio());
        }
        if (profileData.getCity() != null) {
            user.setCity(profileData.getCity());
        }
        if (profileData.getIsArtist() != null) {
            user.setIsArtist(profileData.getIsArtist());
        }
        if (profileData.getIsPublic() != null) {
            user.setIsPublic(profileData.getIsPublic());
        }

        user = userRepository.save(user);

        return mapToProfileResponse(user, null);
    }

    @Transactional
    public void followUser(Long followingId, Long followerId) {
        if (followingId.equals(followerId)) {
            throw new RuntimeException("No puedes seguirte a ti mismo");
        }

        if (followerRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Ya sigues a este usuario");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        com.tokapuart.model.Follower followerEntity = com.tokapuart.model.Follower.builder()
                .follower(follower)
                .following(following)
                .build();

        followerRepository.save(followerEntity);
    }

    @Transactional
    public void unfollowUser(Long followingId, Long followerId) {
        if (!followerRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("No sigues a este usuario");
        }

        followerRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    private UserProfileResponse mapToProfileResponse(User user, Boolean isFollowing) {
        Long artworksCount = userRepository.countArtworksByUserId(user.getId());
        Long favoritesCount = userRepository.countFavoritesByUserId(user.getId());
        Long commentsCount = userRepository.countCommentsByUserId(user.getId());
        Long followersCount = userRepository.countFollowersByUserId(user.getId());
        Long followingCount = userRepository.countFollowingByUserId(user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .city(user.getCity())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .isArtist(user.getIsArtist())
                .isPublic(user.getIsPublic())
                .artworksCount(artworksCount)
                .favoritesCount(favoritesCount)
                .commentsCount(commentsCount)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .createdAt(user.getCreatedAt())
                .isFollowing(isFollowing)
                .build();
    }
}
