package com.clara.ops.challenge.document_management_service_challenge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private ArrayList<String> contentTypesAllowed;

}
