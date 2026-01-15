package org.overcode250204.iamservice.configs.cognito;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@RequiredArgsConstructor
public class CognitoConfig {

    private final CognitoProperties props;

    @Bean
    public CognitoIdentityProviderClient cognitoClient(){
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
        return CognitoIdentityProviderClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(props.getRegion()))
                .build();
    }
}
