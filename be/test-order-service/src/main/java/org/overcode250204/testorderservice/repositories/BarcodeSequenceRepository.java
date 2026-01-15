package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.BarcodeSequence;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BarcodeSequenceRepository extends CrudRepository<BarcodeSequence, LocalDate> {
}
