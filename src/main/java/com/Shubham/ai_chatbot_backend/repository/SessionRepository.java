package com.Shubham.ai_chatbot_backend.repository;

import com.Shubham.ai_chatbot_backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionToken(String sessionToken);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.sessionToken = :sessionToken")
    void deleteBySessionToken(@Param("sessionToken") String sessionToken);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredSessions();
}