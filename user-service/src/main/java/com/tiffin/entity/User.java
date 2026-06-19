package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import com.tiffin.common.enums.UserRole;
import jakarta.persistence.*;

/**
 * Maps to the 'users' table in tiffin_users database.
 *
 * Extends BaseEntity so we get id, createdAt, updatedAt for free.
 *
 * We do NOT store plain-text passwords here.
 * The service layer always BCrypt-hashes before saving.
 *
 * UserRole is stored as a STRING in DB (not ordinal number)
 * so the DB is readable and adding new roles never breaks old data.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
    },
    indexes = {
    		@Index(name="idx_users_email", columnList = "email"),
    		@Index(name="idx_users_phone", columnList = "phone"),
    		@Index(name="idx_users_active", columnList = "is_active")
    }
)
public class User extends BaseEntity {
	
	@Column(name = "provider_id", length = 40)
	private String providerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /**
     * Always stores BCrypt hash, never plain text.
     * Length 255 because BCrypt hashes are 60 chars but
     * leaving room for future algorithm changes.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    /**
     * Full address string for customers.
     * Providers have their own detailed address in the providers table.
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Stored as VARCHAR ("CUSTOMER", "PROVIDER", "ADMIN")
     * EnumType.STRING is critical — never use ORDINAL
     * because adding a new enum value would shift ordinal numbers
     * and corrupt existing data silently.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * Soft delete flag.
     * We never hard-delete users — just mark them inactive.
     * This preserves order history and audit trail.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ── Constructors ───────────────────────────────────────────────────────

    // Default constructor required by JPA — do not remove
    public User() {}

    // Convenience constructor for registration flow
    public User(String name, String email, String passwordHash,
                String phone, UserRole role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.isActive = true;
    }

    // ── Getters and Setters ────────────────────────────────────────────────
    
    public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

    public String getName() {
        return name;
    }

	public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // ── toString (never include passwordHash in logs) ──────────────────────

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
        // passwordHash intentionally excluded — never log it
    }

	@Override
	protected String getIdPrefix() {
		return PrefixConstants.USER;
	}
}
