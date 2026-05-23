package com.tiffin.dto;

import com.tiffin.common.enums.UserRole;

/**
 * Response returned after successful register or login.
 * Contains the JWT token the client must store and send
 * in every subsequent request as:
 * Authorization: Bearer <token>
 */
public class AuthResponse {

	private String token;
	private String userId;
	private String name;
	private String email;
	private UserRole role;

	// ── Constructors ───────────────────────────────────────────────────────

	public AuthResponse() {
	}

	public AuthResponse(String token, String userId, String name, String email, UserRole role) {
		this.token = token;
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
	}
	
	// ── Getters and Setters ────────────────────────────────────────────────
	 
    public String getToken() {
        return token;
    }
 
    public void setToken(String token) {
        this.token = token;
    }
 
    public String getUserId() {
        return userId;
    }
 
    public void setUserId(String userId) {
        this.userId = userId;
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
 
    public UserRole getRole() {
        return role;
    }
 
    public void setRole(UserRole role) {
        this.role = role;
    }

}
