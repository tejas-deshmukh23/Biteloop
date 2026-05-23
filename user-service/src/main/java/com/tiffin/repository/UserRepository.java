package com.tiffin.repository;

import com.tiffin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA automatically provides basic CRUD operations:
 * save(), findById(), findAll(), deleteById() etc.
 * We only need to declare methods that are specific to our business logic.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // String because our BaseEntity id is String (UUID)

    // Used in login flow — find user by email to verify credentials
    Optional<User> findByEmail(String email);

    // Used in registration — check if email already exists before saving
    boolean existsByEmail(String email);

    // Used in registration — check if phone already exists before saving
    boolean existsByPhone(String phone);
}