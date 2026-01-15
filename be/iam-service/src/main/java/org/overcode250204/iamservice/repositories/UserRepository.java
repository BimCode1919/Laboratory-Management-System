package org.overcode250204.iamservice.repositories;

import org.overcode250204.iamservice.entities.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.overcode250204.iamservice.enums.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByIdentifyNumberHash(String identifyNumberHash);

    Optional<UserProfile> findByIdentifyNumberHash(String identifyNumberHash);

    boolean existsByEmailHash(String emailHash);

    Optional<UserProfile> findByCognitoSub(String cognitoSub);

    @Query("""
        SELECT ur.role.name
        FROM UserRole ur
        WHERE ur.userProfile.cognitoSub = :username
""")
    List<String> findByRoleNamesByUsername(@Param("username") String username);

    @Query("""
         SELECT ur.role.code
         FROM UserRole ur
         WHERE ur.userProfile.cognitoSub = :username
    """)
    List<String> findRoleCodesByUsername(@Param("username") String username);


    List<UserProfile> id(UUID id);

    Optional<UserProfile> findByEmailHash(String emailHash);

    @Query("""
    SELECT DISTINCT u FROM UserProfile u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role
    """)
    List<UserProfile> findAllWithRoles();

    long countByStatus(Status status);
}
