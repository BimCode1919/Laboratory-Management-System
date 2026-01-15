package org.overcode250204.testorderservice.configs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSClientConfig {
    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.roleArn}")
    private String roleArn;

    @Bean
    public AWSSecurityTokenService stsClient() {
        return AWSSecurityTokenServiceClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    @Bean
    public AmazonS3 s3Client(AWSSecurityTokenService stsClient) {
        STSAssumeRoleSessionCredentialsProvider assumeRoleCredentialsProvider =
                new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "test-order-service")
                        .withStsClient(stsClient)
                        .withRoleSessionDurationSeconds(3600)
                        .build();

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(assumeRoleCredentialsProvider)
                .build();
    }
}