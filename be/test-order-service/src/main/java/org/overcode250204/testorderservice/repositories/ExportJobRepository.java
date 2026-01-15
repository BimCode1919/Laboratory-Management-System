package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, UUID> {
}
