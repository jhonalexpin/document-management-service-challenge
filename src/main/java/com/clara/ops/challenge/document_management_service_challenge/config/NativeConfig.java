package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.messages.ErrorResponse;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.util.ClassUtils;

@Configuration
@ImportRuntimeHints(NativeRuntimeHints.class)
public class NativeConfig {
}

class NativeRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // 1. MINIO (Keep these, as they are specific to your app logic)
        String[] minioClasses = {
                "io.minio.PutObjectArgs",
                "io.minio.ObjectArgs",
                "io.minio.BucketArgs",
                "io.minio.UploadObjectArgs",
                "io.minio.BaseArgs",
                "io.minio.StatObjectArgs",
                "io.minio.messages.ErrorResponse",
                "io.minio.messages.CreateBucketConfiguration",
                "io.minio.messages.VersioningConfiguration",
                "io.minio.messages.Bucket",
                "io.minio.messages.ListAllMyBucketsResult"
        };

        // 2. Register MinIO classes for reflection
        registerAll(hints, classLoader, minioClasses);

        // 3. OKHTTP (MinIO's client) - Use String based resolution
        hints.reflection().registerType(
                ClassUtils.resolveClassName("okhttp3.internal.publicsuffix.PublicSuffixDatabase", classLoader),
                builder -> builder.withMembers(MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_PUBLIC_METHODS)
        );

        // 4. Standard Java Helpers
        hints.reflection().registerType(ErrorResponse.class);
        hints.reflection().registerType(java.util.GregorianCalendar.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

        // NOTE: We have REMOVED all manual references to Woodstox (com.ctc.wstx)
        // and Netty here. The pom.xml flags handle them now.
    }

    private void registerAll(RuntimeHints hints, ClassLoader classLoader, String[] classNames) {
        for (String className : classNames) {
            Class<?> clazz = ClassUtils.resolveClassName(className, classLoader);
            hints.reflection().registerType(clazz, builder ->
                    builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.DECLARED_FIELDS
                    )
            );
        }
    }
}