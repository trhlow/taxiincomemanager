package com.taxiincome.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findFirstByOrderByCreatedAtAsc();

    @Query("select count(u) > 0 from User u")
    boolean existsAnyUser();
}
