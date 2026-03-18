package com.clara.ops.challenge.document_management_service_challenge.utils;

import com.clara.ops.challenge.document_management_service_challenge.config.AppProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.Optional;

@Component
@AllArgsConstructor
public class FileValidatorUtils {

    private final AppProperties appProperties;

    /**
     * Validate if the file content is supported
     * @param filePart File in the request
     * @return boolean if the file content type is supported or not
     */
    public boolean isSupported(final FilePart filePart) {
        String contentType = Optional.of(filePart.headers())
                .map(HttpHeaders::getContentType)
                .map(MimeType::toString)
                .orElse("");
        for (String type : appProperties.getContentTypesAllowed()) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
