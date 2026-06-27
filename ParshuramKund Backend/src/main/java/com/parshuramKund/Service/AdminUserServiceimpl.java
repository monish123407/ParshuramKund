package com.parshuramKund.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.parshuramKund.DTO.AdminUserDTO;
import com.parshuramKund.Entity.AdminUser;
import com.parshuramKund.Repository.AdminUserRepository;

@Service
public class AdminUserServiceimpl implements AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Override
    public AdminUserDTO login(String username, String password) {
        return adminUserRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(AdminUserDTO::entityToDTO)
                .orElse(null);
    }

    @Override
    public List<AdminUserDTO> getAllMembers() {
        return adminUserRepository.findAll().stream()
                .map(AdminUserDTO::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserDTO saveMember(AdminUserDTO dto) {
        if (adminUserRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        AdminUser user = new AdminUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Store password directly for local mela development
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        adminUserRepository.save(user);
        return AdminUserDTO.entityToDTO(user);
    }

    @Override
    public AdminUserDTO updateMember(Long id, AdminUserDTO dto) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(dto.getPassword());
        }
        adminUserRepository.save(user);
        return AdminUserDTO.entityToDTO(user);
    }

    @Override
    public void deleteMember(Long id) {
        adminUserRepository.deleteById(id);
    }
}
