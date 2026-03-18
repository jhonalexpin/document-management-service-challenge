package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class MinioService {


    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public Mono<String> uploadDocumentToMinio(final FilePart filePart, final String user, final String name) {
        String s3Key = user + "/" + name;

        return Mono.fromCallable(() -> {
            // Piped streams allow us to write from the Flux and read for MinIO simultaneously
            PipedOutputStream os = new PipedOutputStream();
            PipedInputStream is = new PipedInputStream(os, 1024 * 1024); // 1MB internal buffer

            // 1. Start the MinIO upload in a separate thread (it will wait for data in the pipe)
            CompletableFuture<String> uploadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(minioProperties.getBucketName())
                                    .object(s3Key)
                                    // -1 size means unknown, 5MB part size is good for memory
                                    .stream(is, -1, 5 * 1024 * 1024)
                                    .contentType(filePart.headers().getContentType().toString())
                                    .build());
                    return s3Key;
                } catch (Exception e) {
                    throw new RuntimeException("MinIO Upload Failed", e);
                }
            }, Schedulers.boundedElastic()::schedule);

            // 2. Stream the FilePart content into the PipedOutputStream
            DataBufferUtils.write(filePart.content(), os)
                    .doOnTerminate(() -> {
                        try { os.close(); } catch (IOException ignored) {}
                    })
                    .subscribe();

            return uploadFuture.join();
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
