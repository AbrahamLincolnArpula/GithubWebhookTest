package com.github.githubWebhookDataS3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

@Configuration
public class S3Config {

    @Bean
    @Profile("prod")
    public S3Client s3Client() {
		/*
		 * StsClient stsClient = StsClient.builder() .region(Region.AP_SOUTH_1)
		 * .build();
		 * 
		 * AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
		 * .roleArn("arn:aws:iam::590183986028:role/githubS3Role")
		 * .roleSessionName("sessionName") .build();
		 * 
		 * AssumeRoleResponse assumeRoleResponse =
		 * stsClient.assumeRole(assumeRoleRequest);
		 * 
		 * AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
		 * assumeRoleResponse.credentials().accessKeyId(),
		 * assumeRoleResponse.credentials().secretAccessKey(),
		 * assumeRoleResponse.credentials().sessionToken() );
		 * 
		 * return S3Client.builder() .region(Region.AP_SOUTH_1)
		 * .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
		 * .build();
		 */
    	
        StsClient stsClient = StsClient.builder()
                .region(Region.AP_SOUTH_1)  // Replace with your region
                .build();

        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn("arn:aws:iam::590183986028:role/githubS3Role")  // Replace with your role ARN
                .roleSessionName("sessionName")
                .build();

        return S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(StsAssumeRoleCredentialsProvider.builder()
                    .refreshRequest(assumeRoleRequest)
                    .stsClient(stsClient)
                    .build())
                .build();
    }

    @Bean
    @Profile("dev")
    public S3Client mockS3Client() {
        return null; // Returning null or a mock S3Client for dev profile
    }
}
