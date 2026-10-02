package com.crm.BackendApp.repo;

import com.crm.BackendApp.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByToken(String token);
    List<UserInvitation> findByEmailAndAcceptedFalseAndInvalidatedFalse(String email);
    boolean existsByEmailAndAcceptedTrue(String email);
}
