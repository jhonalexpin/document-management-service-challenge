package com.clara.ops.challenge.document_management_service_challenge;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableConfigurationProperties
@EnableR2dbcRepositories
public class DocumentManagementServiceChallengeApplication {

  static {
    // Force Netty to use unpooled allocator to avoid JCTools
    System.setProperty("io.netty.allocator.type", "unpooled");
    System.setProperty("io.netty.noPreferredDirect", "true");
  }

  public static void main(String[] args) {
    SpringApplication.run(DocumentManagementServiceChallengeApplication.class, args);
  }
}
