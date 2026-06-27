package com.parshuramKund.Service;

import java.util.List;
import com.parshuramKund.DTO.AdminUserDTO;

public interface AdminUserService {
    AdminUserDTO login(String username, String password);
    List<AdminUserDTO> getAllMembers();
    AdminUserDTO saveMember(AdminUserDTO dto);
    AdminUserDTO updateMember(Long id, AdminUserDTO dto);
    void deleteMember(Long id);
}
