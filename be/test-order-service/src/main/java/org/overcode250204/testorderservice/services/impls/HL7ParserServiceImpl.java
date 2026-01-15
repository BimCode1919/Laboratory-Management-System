package org.overcode250204.testorderservice.services.impls;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.group.ORU_R01_ORCOBRNTEOBXNTECTI;
import ca.uhn.hl7v2.model.v231.group.ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI;
import ca.uhn.hl7v2.model.v231.message.ORU_R01;
import ca.uhn.hl7v2.model.v231.segment.OBX;
import ca.uhn.hl7v2.parser.PipeParser;
import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.dtos.TestResultItem;
import org.overcode250204.testorderservice.exceptions.HL7ParsingException;
import org.overcode250204.testorderservice.services.HL7ParserService;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class HL7ParserServiceImpl implements HL7ParserService {
    private final PipeParser parser;

    public HL7ParserServiceImpl() {
        DefaultHapiContext context = new DefaultHapiContext();
        this.parser = context.getPipeParser();
    }

    @Override
    public HL7TestResult parseHL7Message(String hl7Message) throws HL7ParsingException {
        try {
            String cleanedHl7Message = hl7Message.replace('\n', '\r');
            String correctedHl7Message = cleanedHl7Message.replaceAll("\\|\\|\\|F\\|", "||F\\|");

            Message message = parser.parse(correctedHl7Message);

            if (!(message instanceof ORU_R01 oru)) {
                throw new HL7ParsingException(
                        "Unsupported HL7 message type. Expected ORU_R01 (v2.3.1), got "
                                + (message == null ? "null" : message.getClass().getSimpleName()));
            }

            ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI[] groups =
                    oru.getPIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTIAll().toArray(new ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI[0]);
            if(groups.length == 0){
                throw new HL7ParsingException("No PID/OBX group found in HL7 message.");
            }

            String barcode = extractBarCode(groups[0]);
            HL7TestResult result = new HL7TestResult(barcode);
            extractResults(groups, result);

            return result;

        } catch (HL7Exception e) {
            throw new HL7ParsingException("Failed to parse HL7 message: " + e.getMessage(), e);
        }
    }

    private String extractBarCode(ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI group) throws HL7ParsingException {
        try {
            return group.getPIDPD1NK1NTEPV1PV2()
                    .getPID()
                    .getPatientIdentifierList(0)
                    .getID()
                    .getValue();
        } catch (Exception e) {
            throw new HL7ParsingException("Failed to extract sample barcode from PID segment", e);
        }
    }

    private void extractResults(ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI[] groups, HL7TestResult result) throws HL7Exception {
        for(ORU_R01_PIDPD1NK1NTEPV1PV2ORCOBRNTEOBXNTECTI group : groups) {
            ORU_R01_ORCOBRNTEOBXNTECTI obxGroup = group.getORCOBRNTEOBXNTECTI();

            IntStream.range(0, obxGroup.getOBXNTEReps())
                    .mapToObj(i -> obxGroup.getOBXNTE(i).getOBX())
                    .map(this::toTestResultItem)
                    .forEach(item -> result.addTestResult(item));
        }
    }

    private TestResultItem toTestResultItem(OBX obx) {
        try {
            String parameter = obx.getObx3_ObservationIdentifier().getIdentifier().getValue();
            String value = obx.getObx5_ObservationValue(0).encode();
            String unit = obx.getObx6_Units().encode();
            String flag = obx.getObx8_AbnormalFlags(0).getValue();
            return new TestResultItem(parameter, value, unit, flag);
        } catch(Exception e) {
            return new TestResultItem("UNKNOWN", "", "", "");
        }

    }
}