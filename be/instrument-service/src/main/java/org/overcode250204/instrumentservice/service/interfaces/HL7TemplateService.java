package org.overcode250204.instrumentservice.service.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import org.overcode250204.instrumentservice.entity.Instrument;

public interface HL7TemplateService {

    String buildOruMessage(Instrument instrument, String barcode, String testType, JsonNode rawData);

}
