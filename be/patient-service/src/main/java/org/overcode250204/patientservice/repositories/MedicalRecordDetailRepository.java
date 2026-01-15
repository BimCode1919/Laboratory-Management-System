package org.overcode250204.patientservice.repositories;

import org.overcode250204.patientservice.documents.MedicalRecordDetailDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MedicalRecordDetailRepository extends ElasticsearchRepository<MedicalRecordDetailDocument, String> {}