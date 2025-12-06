package com.tokapuart.repository;

import com.tokapuart.model.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {

    Optional<Follower> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follower> findByFollowerId(Long followerId);

    List<Follower> findByFollowingId(Long followingId);

    Boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Long countByFollowerId(Long followerId);

    Long countByFollowingId(Long followingId);
}
