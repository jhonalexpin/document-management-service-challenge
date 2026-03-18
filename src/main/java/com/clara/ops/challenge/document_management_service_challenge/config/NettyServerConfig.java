package com.clara.ops.challenge.document_management_service_challenge.config;

import io.netty.buffer.UnpooledByteBufAllocator;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyServerConfig {

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.addServerCustomizers(httpServer -> {
            // Force unpooled allocator
            return httpServer;
        });

        // Set the allocator on the factory - method may not exist in this Spring Boot version
        // factory.setByteBufAllocator(UnpooledByteBufAllocator.DEFAULT);

        return factory;
    }
}
