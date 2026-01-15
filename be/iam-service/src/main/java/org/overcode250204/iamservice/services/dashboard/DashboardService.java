package org.overcode250204.iamservice.services.dashboard;

import org.overcode250204.iamservice.dto.dashboard.RoleUserCountDTO;
import org.overcode250204.iamservice.dto.dashboard.UserStatusCountDTO;

import java.util.List;

public interface DashboardService {
    List<RoleUserCountDTO> getUserCountByRole();
    UserStatusCountDTO getUserStatusCounts();
}
