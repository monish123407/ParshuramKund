package com.parshuramKund.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.parshuramKund.DTO.AdminUserDTO;
import com.parshuramKund.Entity.AdminUser;
import com.parshuramKund.Repository.AdminUserRepository;

@Service
public class AdminUserServiceimpl implements AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Override
    public AdminUserDTO login(String username, String password) {
        return adminUserRepository.findByUsername(username)
                .filter(user -> ENCODER.matches(password, user.getPassword()))
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
        user.setPassword(ENCODER.encode(dto.getPassword())); // Store encrypted password
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
            user.setPassword(ENCODER.encode(dto.getPassword())); // Update with encrypted password
        }
        adminUserRepository.save(user);
        return AdminUserDTO.entityToDTO(user);
    }

    @Override
    public void deleteMember(Long id) {
        adminUserRepository.deleteById(id);
    }
}
