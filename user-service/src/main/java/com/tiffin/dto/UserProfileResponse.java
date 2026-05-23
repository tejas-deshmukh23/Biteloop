package com.tiffin.dto;

import com.tiffin.common.enums.UserRole;
import java.time.LocalDateTime;

/**
 * Returned when fetching user profile — GET /api/users/me
 * Never expose passwordHash in any response DTO.
 */
public class UserProfileResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;

    // ── Constructors ───────────────────────────────────────────────────────

    public UserProfileResponse() {}

    public UserProfileResponse(String id, String name, String email, String phone,
                               String address, UserRole role,
                               boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}