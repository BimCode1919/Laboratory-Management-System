package org.overcode250204.iamservice.services.privilege.impl;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.dto.privilege.PrivilegeDTO;
import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.mappers.PrivilegeMapper;
import org.overcode250204.iamservice.repositories.PrivilegeRepository;
import org.overcode250204.iamservice.services.privilege.PrivilegeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivilegeServiceImpl implements PrivilegeService {
    private final PrivilegeRepository privilegeRepository;
    private final PrivilegeMapper privilegeMapper;

    @Override
    public List<PrivilegeDTO> getAllPrivilege() {
        List<Privilege> privileges = privilegeRepository.findAll();
        return privilegeMapper.toPrivilegeDTOList(privileges);
    }
}
