package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Query(value = "SELECT * FROM refresh_tokens WHERE token = :token LIMIT 1", nativeQuery = true)
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query(value = "UPDATE refresh_tokens SET revoked = TRUE WHERE token = :token", nativeQuery = true)
    void revokeToken(@Param("token") String token);
}
