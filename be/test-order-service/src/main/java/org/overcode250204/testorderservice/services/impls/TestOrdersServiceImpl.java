package org.overcode250204.testorderservice.services.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.overcode250204.common.grpc.PatientIdentityInfo;
import org.overcode250204.common.grpc.PatientRecord;
import org.overcode250204.common.grpc.PatientServiceGrpc;
import org.overcode250204.testorderservice.dtos.PatientReferenceDTO;
import org.overcode250204.testorderservice.dtos.TestOrderDTO;
import org.overcode250204.testorderservice.elastic.services.TestOrderIndexingService;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.mappers.PatientReferenceMapper;
import org.overcode250204.testorderservice.mappers.TestOrdersMapper;
import org.overcode250204.testorderservice.models.entites.OutboxEvent;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.repositories.OutboxRepository;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.services.TestOrdersService;
import org.overcode250204.testorderservice.utils.BarcodeGeneratorUtils;
import org.overcode250204.testorderservice.utils.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestOrdersServiceImpl implements TestOrdersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestOrdersServiceImpl.class);

    private final TestOrdersRepository testOrderRepository;

    private final PatientReferenceRepository patientReferenceRepository;

    private final Validator validator;

    private final PatientReferenceMapper patientMapper;

    private final TestOrdersMapper testOrdersMapper;

    private final TestOrderIndexingService indexingService;

    @GrpcClient("patient-service")
    private PatientServiceGrpc.PatientServiceBlockingStub patientStub;


    private final ObjectMapper objectMapper;

    private final OutboxRepository outboxRepository;

    private final CodeGenerator codeGenerator;

    private  final BarcodeGeneratorUtils barcodeGeneratorUtils;


    @Override
    @Transactional
    public TestOrderDTO createTestOrder(TestOrderDTO request, String createdByLabUser) throws JsonProcessingException {
        TestOrders savedOrder;
        try {
            log.warn("Time to create test order");
            // Validate DTO
            validator.validate(request);

            // Tính age từ DOB nếu không được cung cấp
            PatientReferenceDTO patientDTO = request.getPatient();
            Integer age = patientDTO.getAge();
            if (age == null && patientDTO.getDateOfBirth() != null) {
                age = Period.between(patientDTO.getDateOfBirth(), LocalDate.now()).getYears();
            }
            patientDTO.setAge(age);

            PatientIdentityInfo patientInfoRequestToPatientService = PatientIdentityInfo.newBuilder()
                    .setEmail(patientDTO.getEmail())
                    .setAddress(patientDTO.getAddress())
                    .setDob(patientDTO.getDateOfBirth().toString())
                    .setGender(patientDTO.getGender())
                    .setFullName(patientDTO.getFullName())
                    .setAge(patientDTO.getAge().toString())
                    .setEmail(patientDTO.getEmail())
                    .setPhone(patientDTO.getPhoneNumber())
                    .setCreatedByLabUser(createdByLabUser)
                    .build();
            PatientRecord patientRecord = patientStub.ensurePatient(patientInfoRequestToPatientService);
            PatientReference patientReference = patientReferenceRepository.findByPatientCode(UUID.fromString(patientRecord.getPatientCode()));
            if (patientReference == null) {
                patientReference = new PatientReference();
                patientReference.setPatientCode(UUID.fromString(patientRecord.getPatientCode()));
                patientReference.setEmail(patientRecord.getEmail());
                patientReference.setAddress(patientRecord.getAddress());
                patientReference.setGender(patientRecord.getGender());
                patientReference.setFullName(patientRecord.getFullName());
                patientReference.setDateOfBirth(LocalDate.parse(patientRecord.getDob()));
                patientReference.setAge(Integer.parseInt(patientRecord.getAge()));
                patientReference.setPhoneNumber(patientRecord.getPhone());
                patientReferenceRepository.save(patientReference);
            }

            TestOrders testOrder = new TestOrders();
            testOrder.setPatient(patientReference);
            testOrder.setMedicalRecordId(UUID.fromString(patientRecord.getMedicalRecordId()));
            testOrder.setCreatedBy(UUID.fromString(createdByLabUser));
            testOrder.setCreatedAt(LocalDateTime.now());
            testOrder.setNotes(request.getNotes());
            testOrder.setStatus(TestOrderStatus.PENDING);
            testOrder.setPriority(request.getPriority());
            testOrder.setTestType(request.getTestType());
            testOrder.setBarCode(barcodeGeneratorUtils.generateBarcode());
            testOrder.setOrderCode(codeGenerator.generateRecordCode());
            savedOrder = testOrderRepository.save(testOrder);

            Map<String, Object> payload = new HashMap<>();

            payload.put("eventType", "TEST_ORDER_CREATED");
            payload.put("testOrderId", savedOrder.getId().toString());
            payload.put("priority", request.getPriority());
            payload.put("medicalRecordId", savedOrder.getMedicalRecordId().toString());
            payload.put("barCode", savedOrder.getBarCode());
            payload.put("patientCode", savedOrder.getPatient().getPatientCode());
            payload.put("patientName", savedOrder.getPatient().getFullName());
            payload.put("patientDateOfBirth", savedOrder.getPatient().getDateOfBirth());
            payload.put("phone", savedOrder.getPatient().getPhoneNumber());
            payload.put("email", savedOrder.getPatient().getEmail());
            payload.put("createdBy", createdByLabUser);
            payload.put("createAt", savedOrder.getCreatedAt().toString());
            payload.put("testType", savedOrder.getTestType().name());
            payload.put("notes", request.getNotes());
            payload.put("noteBy", createdByLabUser);
            LOGGER.info("Created test order ID: {}", savedOrder.getId());
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("TEST_ORDER");
            outboxEvent.setAggregateId(testOrder.getId().toString());
            outboxEvent.setEventType("TEST_ORDER_CREATED");
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            outboxEvent.setCreatedAt(Instant.now());
            outboxRepository.save(outboxEvent);
            LOGGER.info("Created outbox event {}", outboxEvent);

            try {
                indexingService.reindexTestOrder(savedOrder.getId());
            } catch (Exception ex) {
                log.error("Failed to sync new order to Elastic: {}", ex.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while saving test order with error: ", e);
            throw new TestOrderException(ErrorCode.FAIL_TO_CREAT_TEST_ORDER);
        }
        return testOrdersMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional
    public TestOrderDTO updateTestOrder(UUID id, TestOrderDTO request, String createdByLabUser) {
        validator.validate(request);

        TestOrders testOrder = testOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test order not found"));

        // Cập nhật PatientReference
        PatientReference patient = testOrder.getPatient();
        PatientReferenceDTO patientDTO = request.getPatient();
        Integer age = patientDTO.getAge();
        if (age == null && patientDTO.getDateOfBirth() != null) {
            age = Period.between(patientDTO.getDateOfBirth(), LocalDate.now()).getYears();
        }
        patientDTO.setAge(age);
        patientMapper.updateFromDto(patientDTO, patient);
        patientReferenceRepository.save(patient);

        // Cập nhật TestOrder
        testOrder.setCreatedBy(UUID.fromString(createdByLabUser));
        testOrder.setNotes(request.getNotes());
        // Status có thể update riêng, không từ request

        TestOrders updatedOrder = testOrderRepository.save(testOrder);
        LOGGER.info("Updated test order ID: {}", id);

        try {
            indexingService.reindexTestOrder(updatedOrder.getId());
        } catch (Exception ex) {
            log.error("Failed to sync updated order to Elastic: {}", ex.getMessage());
        }

        return testOrdersMapper.toDTO(updatedOrder);
    }

    @Override
    public void deleteTestOrder(UUID id, String deletedByLabUser) {
        TestOrders testOrder = testOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test order not found"));

        if (testOrder.getStatus() == TestOrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot delete a completed test order");
        }

        if (testOrder.getStatus().equals(TestOrderStatus.PENDING)) {
            testOrder.setStatus(TestOrderStatus.CANCELLED);
        }

        testOrder.setIsDeleted(true); // Soft delete
        testOrderRepository.save(testOrder);
        LOGGER.info("Deleted test order ID: {}", id);

        try {
            indexingService.deleteOrder(id);
        } catch (Exception ex) {
            log.error("Failed to sync deleted order to Elastic: {}", ex.getMessage());
        }
    }

    @Override
    public Page<TestOrderDTO> getAllTestOrders(String patientName, TestOrderStatus status, Pageable pageable) {
        Page<TestOrders> page = testOrderRepository.searchTestOrders(patientName, status, pageable);
        return page.map(testOrdersMapper::toDTO);
    }

    @Override
    public Optional<TestOrderDTO> getTestOrderDetail(UUID id) {
        return testOrderRepository.findByIdAndIsDeletedFalse(id)
                .map(testOrdersMapper::toDTO);
    }
}