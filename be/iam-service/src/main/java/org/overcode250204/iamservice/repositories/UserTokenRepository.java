package org.overcode250204.iamservice.repositories;

import org.overcode250204.iamservice.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    @Query("select t from UserToken t where t.status = 'ACTIVE' and t.expiresAt > :now")
    List<UserToken> findActiveNotExpired(@Param("now") LocalDateTime now);
}

