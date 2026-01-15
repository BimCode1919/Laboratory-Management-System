package org.overcode250204.testorderservice.services.impls;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.overcode250204.testorderservice.dtos.TestOrderReportDetailDTO;
import org.overcode250204.testorderservice.dtos.TestResultReportDTO;
import com.opencsv.CSVWriter;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.mappers.TestOrdersMapper;
import org.overcode250204.testorderservice.mappers.TestResultsMapper;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.models.enums.ExportFileType;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.overcode250204.testorderservice.services.ExportJobService;
import org.overcode250204.testorderservice.utils.ExportFileNameGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportJobServiceImpl implements ExportJobService {
    private final TestOrdersRepository testOrdersRepository;
    private final TestResultsRepository testResultsRepository;
    private final TestOrdersMapper testOrdersMapper;
    private final TestResultsMapper testResultsMapper;
    private final AmazonS3 amazonS3;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Override
    public String generatePresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour
        return amazonS3.generatePresignedUrl(bucketName, key, expiration).toString();
    }

    @Override
    public String writeCsv(UUID testOrderId) {
        Map<String, Object> meta = prepareExportMetadata(testOrderId, ExportFileType.CSV);
        LocalDateTime timestamp = (LocalDateTime) meta.get("timestamp");
        String fileName = (String) meta.get("fileName");

        List<TestResults> results = testResultsRepository.findByTestOrderId(testOrderId);

        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // header
            String[] header = {
                    "Parameter", "Value", "Reference Low", "Reference High",
                    "Unit", "Alert Level", "AI Review Comment"
            };
            csvWriter.writeNext(header);

            // data rows
            for (TestResults r : results) {
                TestResultReportDTO dto = testResultsMapper.toReportDTO(r);
                String[] row = {
                        dto.getParameterName(),
                        String.valueOf(dto.getResultValue()),
                        String.valueOf(dto.getReferenceLow()),
                        String.valueOf(dto.getReferenceHigh()),
                        dto.getUnit(),
                        String.valueOf(dto.getAlertLevel()),
                        dto.getAiReviewComment()
                };
                csvWriter.writeNext(row);
            }
        } catch (IOException e) {
            throw new TestOrderException(ErrorCode.EXPORT_CSV_FAILED);
        }

        // s3 key
        String key = String.format("csv/%d/%02d/%02d/%s",
                timestamp.getYear(),
                timestamp.getMonthValue(),
                timestamp.getDayOfMonth(),
                fileName
        );
        amazonS3.putObject(bucketName, key, stringWriter.toString());
        return key;
    }

    @Override
    public String writeExcel(UUID testOrderId) {
        Map<String, Object> meta = prepareExportMetadata(testOrderId, ExportFileType.XLSX);
        TestOrders testOrder = (TestOrders) meta.get("testOrder");
        String patientName = (String) meta.get("patientName");
        LocalDateTime timestamp = (LocalDateTime) meta.get("timestamp");
        String fileName = (String) meta.get("fileName");

        TestOrderReportDetailDTO reportData = testOrdersMapper.toReportDetailDTO(testOrder);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("patientCode", testOrder.getPatient().getPatientCode().toString());
        parameters.put("orderCode", reportData.getOrderCode());
        parameters.put("patientName", reportData.getPatientName());
        parameters.put("gender", reportData.getGender());
        parameters.put("dateOfBirth", reportData.getDateOfBirthAsDate());
        parameters.put("phoneNumber", reportData.getPhoneNumber());
        parameters.put("status", reportData.getStatus());
        parameters.put("createdBy", reportData.getCreatedBy());
        parameters.put("createdOn", reportData.getCreatedOnAsDate());
        parameters.put("notes", reportData.getNotes());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            InputStream jasperStream = getClass().getResourceAsStream("/reports/TestResultsReport.jasper");
            if (jasperStream == null) throw new IllegalStateException("Jasper file not found");

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData.getTestResults());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            config.setCollapseRowSpan(false);
            config.setOnePagePerSheet(true);
            config.setWrapText(true);
            config.setAutoFitRow(true);
            config.setWhitePageBackground(true);

            exporter.setConfiguration(config);

            exporter.exportReport();

            String key = String.format("xlsx/%d/%02d/%02d/%s",
                    timestamp.getYear(),
                    timestamp.getMonthValue(),
                    timestamp.getDayOfMonth(),
                    fileName
            );
            byte[] bytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            amazonS3.putObject(bucketName, key, inputStream, metadata);
            return key;
        } catch (Exception e) {
            log.error("Excel export failed", e);
            throw new TestOrderException(ErrorCode.EXPORT_EXCEL_FAILED);
        }
    }

    @Override
    public String writePdf(UUID testOrderId) {
        Map<String, Object> meta = prepareExportMetadata(testOrderId, ExportFileType.PDF);
        TestOrders testOrder = (TestOrders) meta.get("testOrder");
        LocalDateTime timestamp = (LocalDateTime) meta.get("timestamp");
        String fileName = (String) meta.get("fileName");

        if (!TestOrderStatus.COMPLETED.equals(testOrder.getStatus())) {
            throw new TestOrderException(ErrorCode.EXPORT_PDF_NOT_ALLOWED);
        }

        TestOrderReportDetailDTO reportDetail = testOrdersMapper.toReportDetailDTO(testOrder);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderCode", reportDetail.getOrderCode());
        parameters.put("patientName", reportDetail.getPatientName());
        parameters.put("gender", reportDetail.getGender());
        parameters.put("dateOfBirth", reportDetail.getDateOfBirthAsDate());
        parameters.put("phoneNumber", reportDetail.getPhoneNumber());
        parameters.put("status", reportDetail.getStatus());
        parameters.put("createdBy", reportDetail.getCreatedBy());
        parameters.put("createdOn", reportDetail.getCreatedOnAsDate());
        parameters.put("notes", reportDetail.getNotes());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            InputStream jasperStream = getClass().getResourceAsStream("/reports/TestOrderDetailedReport.jasper");
            if (jasperStream == null) throw new IllegalStateException("Jasper file not found");

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            List<TestResultReportDTO> processed =
                    reportDetail.getTestResults().stream()
                            .map(r -> {
                                if ("Result is within expected limits (Automated Review).".equals(r.getAiReviewComment())) {
                                    r.setAiReviewComment(null);
                                }
                                return r;
                            })
                            .collect(Collectors.toList());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(processed);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            String key = String.format("pdf/%d/%02d/%02d/%s",
                    timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(), fileName);

            byte[] bytes = outputStream.toByteArray();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType("application/pdf");

            amazonS3.putObject(bucketName, key, new ByteArrayInputStream(bytes), metadata);
            return key;
        } catch (Exception e) {
            log.error("PDF export failed", e);
            throw new TestOrderException(ErrorCode.EXPORT_PDF_FAILED);
        }
    }

    private Map<String, Object> prepareExportMetadata(UUID testOrderId, ExportFileType type) {
        TestOrders testOrder = testOrdersRepository.findById(testOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Test Order with ID: " + testOrderId));

        String patientName = testOrder.getPatient().getFullName();
        LocalDateTime timestamp = LocalDateTime.now();
        String fileName = ExportFileNameGenerator.generateExportFileName(testOrderId, patientName, timestamp, type);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testOrder", testOrder);
        metadata.put("patientName", patientName);
        metadata.put("timestamp", timestamp);
        metadata.put("fileName", fileName);
        return metadata;
    }
}
