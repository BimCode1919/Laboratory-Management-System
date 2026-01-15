package org.overcode250204.iamservice.services.dashboard.impl;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.dto.dashboard.RoleUserCountDTO;
import org.overcode250204.iamservice.dto.dashboard.UserStatusCountDTO;
import org.overcode250204.iamservice.enums.Status;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.repositories.UserRoleRepository;
import org.overcode250204.iamservice.services.dashboard.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    @Override
    public List<RoleUserCountDTO> getUserCountByRole() {
        return userRoleRepository.countUsersByRole();
    }

    @Override
    public UserStatusCountDTO getUserStatusCounts() {
        long active = userRepository.countByStatus(Status.ACTIVE);
        long inactive = userRepository.countByStatus(Status.INACTIVE);
        long locked = userRepository.countByStatus(Status.LOCKED);

        return UserStatusCountDTO.builder().active(active).inactive(inactive).locked(locked).build();
    }

}
