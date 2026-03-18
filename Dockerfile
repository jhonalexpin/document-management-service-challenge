# -------- Stage 1 : Native Image Builder --------
FROM ghcr.io/graalvm/native-image-community:17 AS builder

WORKDIR /build

# 1️⃣ Copy Maven wrapper and POM first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 2️⃣ Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw -B -q -e -C dependency:go-offline

# 3️⃣ Copy source code after dependencies
COPY src src

# 4️⃣ Build native image
RUN ./mvnw -Pnative -DskipTests clean native:compile \
  -Dnative-image.xmx=4g \
  -Dnative-image.build-args="\
  --no-fallback,\
  --parallelism=1,\
  --gc=serial,\
  -H:+StripDebugInfo,\
  --initialize-at-run-time=io.netty.channel.epoll.Epoll,\
  --initialize-at-run-time=io.netty.channel.kqueue.KQueue,\
  --initialize-at-run-time=io.netty.channel.unix.Socket,\
  --initialize-at-run-time=io.netty.util.internal.NativeLibraryUtil"

# -------- Stage 2 : Minimal Runtime --------
FROM alpine:3.19
WORKDIR /app
RUN apk add --no-cache gcompat libc6-compat libstdc++ zlib libssl3 libcrypto3

COPY --from=builder /build/target/document-management-service-challenge /app/application

EXPOSE 8080

ENTRYPOINT ["/app/application", "-Dio.netty.allocator.type=unpooled", "-Dio.netty.noPreferredDirect=true"]
