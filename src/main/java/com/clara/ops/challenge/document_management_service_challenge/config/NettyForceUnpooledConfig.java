package com.clara.ops.challenge.document_management_service_challenge.config;

import io.netty.buffer.UnpooledByteBufAllocator;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class NettyForceUnpooledConfig {

    @PostConstruct
    public void configureNetty() {
        // Force Netty to use unpooled allocator globally
        System.setProperty("io.netty.allocator.type", "unpooled");
        System.setProperty("io.netty.noPreferredDirect", "true");

        // Also set the default allocators
        io.netty.buffer.ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        System.out.println("Netty allocator configured to: " + allocator.getClass().getSimpleName());
    }
}
