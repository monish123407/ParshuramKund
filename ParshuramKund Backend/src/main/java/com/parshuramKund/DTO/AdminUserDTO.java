package com.parshuramKund.DTO;

import com.parshuramKund.Entity.AdminUser;

public class AdminUserDTO {

    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String password; // used during member creation / login request, hidden on list retrieval

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static AdminUserDTO entityToDTO(AdminUser entity) {
        if (entity == null) {
            return null;
        }
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFullName(entity.getFullName());
        dto.setRole(entity.getRole());
        return dto;
    }
}
