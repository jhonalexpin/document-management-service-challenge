package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
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
import java.util.concurrent.CompletableFuture;

/**
 * Service class for interacting with MinIO object storage.
 * Handles file uploads and generation of pre-signed URLs.
 */
@Service
@AllArgsConstructor
public class MinioService {


    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    /**
     * Uploads a document to MinIO bucket.
     * Uses piped streams to efficiently transfer data from the reactive FilePart to MinIO's input stream.
     *
     * @param filePart The file part containing the document content.
     * @param user     The user ID associated with the document.
     * @param name     The name of the document.
     * @return A Mono emitting the S3 key (path) of the uploaded object upon success.
     */
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

    /**
     * Generates a pre-signed URL for accessing an object in MinIO.
     * The URL is valid for 24 hours.
     *
     * @param minioPath The path (key) of the object in the MinIO bucket.
     * @return A Mono emitting the pre-signed URL string.
     */
    public Mono<String> generatePresignedUrl(String minioPath) {
        return Mono.fromCallable(() -> minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucketName())
                        .object(minioPath)
                        .expiry(60 * 60 * 24) // 24 hours
                        .build()
        ));
    }

}
