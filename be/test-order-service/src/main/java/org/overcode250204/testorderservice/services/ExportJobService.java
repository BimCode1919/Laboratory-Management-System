package org.overcode250204.testorderservice.services;

import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

public interface ExportJobService {
    String generatePresignedUrl(String key);
    String writeCsv(UUID testOrderId);
    String writeExcel(UUID testOrderId);
    String writePdf(UUID testOrderId);

}
