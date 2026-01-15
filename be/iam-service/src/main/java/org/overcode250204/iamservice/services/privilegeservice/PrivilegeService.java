package org.overcode250204.iamservice.services.privilegeservice;

import java.util.List;

public interface PrivilegeService {
    List<String> getPrivilegesByUserName(String username);
}
