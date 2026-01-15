package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3BackupService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadHL7(String barcode, String hl7Message) {
        String key = "hl7/" + barcode + "-" + System.currentTimeMillis() + ".hl7";
        Path tempFile = null;

        try {
            // Tạo file tạm để upload
            tempFile = Files.createTempFile("hl7-", ".tmp");
            Files.writeString(tempFile, hl7Message);

            // Upload lên S3
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(request, tempFile);

            log.info("[S3BackupService] Uploaded HL7 to S3 key={}", key);
            return key;

        } catch (IOException e) {
            log.error("[S3BackupService] Error creating temp file or writing content", e);
            throw new RuntimeException("Failed to write temporary file for upload", e);

        } catch (Exception e) {
            log.error("[S3BackupService] Error uploading HL7 to S3", e);
            throw new RuntimeException("Failed to upload HL7 message to S3", e);

        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}

