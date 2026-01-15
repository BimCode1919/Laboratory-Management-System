package org.overcode250204.testorderservice.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.dtos.TestResultTrendDTO;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.FlaggingRules;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.overcode250204.testorderservice.repositories.FlaggingRulesRepository;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.overcode250204.testorderservice.services.TestResultsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultsServiceImpl implements TestResultsService {
    private final TestResultsRepository testResultRepository;
    private final PatientReferenceRepository patientReferenceRepository;
    private final FlaggingRulesRepository flaggingRulesRepository;

    @Override
    @Transactional
    public List<TestResults> getByTestOrderId(UUID testOrderId) {
        return testResultRepository.findByTestOrderId(testOrderId);
    }

    @Override
    public List<TestResultTrendDTO> getTrendResultsByPatientCode(
            UUID patientCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String parameterName
    ) {
        if (startTime.isAfter(endTime)) {
            throw new TestOrderException(ErrorCode.INVALID_DATE_RANGE);
        }

        List<TestResultTrendDTO> results = testResultRepository.findTrendResultsByPatientCode(
                patientCode,
                startTime,
                endTime,
                parameterName
        );

        Gender gender = Gender.valueOf(patientReferenceRepository.findGenderByPatientCode(patientCode));

        for (TestResultTrendDTO dto : results) {
            if (dto.getReferenceLow() == null || dto.getReferenceHigh() == null) {
                List<FlaggingRules> rules = flaggingRulesRepository
                        .findByParameterNameIgnoreCaseAndUnitIgnoreCaseAndGenderAndIsActivatedTrue(
                                dto.getParameterName(),
                                dto.getUnit(),
                                gender
                        );

                if (!rules.isEmpty()) {
                    FlaggingRules rule = rules.getFirst();
                    dto.setReferenceLow(rule.getNormalLow().toString());
                    dto.setReferenceHigh(rule.getNormalHigh().toString());
                }
            }
        }

        return results;
    }


    @Override
    public List<String> getAvailableParameterNamesByPatientCode(UUID patientCode) {
        return testResultRepository.findDistinctParameterNamesByPatientCode(patientCode);
    }
}
