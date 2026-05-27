# Dockerfile for usoft-bi

# --- Build Stage ---
# Use a Maven image to build the Java application with JDK 21.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory.
WORKDIR /app

# --- Configure Maven for Aliyun Mirror ---
# Create a settings.xml file to use Aliyun's Maven mirror.
RUN mkdir -p /root/.m2 && \
    echo '<settings>' > /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>Aliyun Maven</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# --- Install Node.js and pnpm ---
# Install Node.js (LTS version) and pnpm for building the frontend.
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g pnpm && \
    pnpm config set registry http://registry.npmmirror.com && \
    npm config set registry http://registry.npmmirror.com

# --- Install zip ---
# Install zip as it's needed for creating the release package.
RUN apt-get install -y zip

# Copy the entire project to the container.
COPY . .

# Run the build script to generate the release package.
# This will create the final zip file in the build directory.
RUN bash /app/assembly/bin/build.sh

# --- Runtime Stage ---
# Use an Eclipse Temurin JRE image for the final application (JDK 21).
FROM eclipse-temurin:21-jre-jammy

# Install unzip and procps as they are needed for the entrypoint script.
RUN apt-get update && apt-get install -y unzip procps && rm -rf /var/lib/apt/lists/*

# Set the working directory.
WORKDIR /app

# Copy the extracted release from the builder stage.
# The build.sh script creates a zip file, so we need to unzip it.
COPY --from=builder /app/assembly/build/usoft-bi-0.0.1-SNAPSHOT.zip /app/release.zip

# Unzip the release and clean up.
RUN unzip release.zip && rm release.zip

# Move the extracted directory to the release directory.
RUN mv usoft-bi-0.0.1-SNAPSHOT release

# The application should now be in /app/usoft-bi-*/
# We need to find the exact directory name.
# Let's assume the entrypoint script will handle this.

# Expose the application port (default is 8080, adjust if needed).
EXPOSE 8080

# --- Environment Variables ---
# Declare arguments that can be passed during the build.
ARG DB_TYPE
ARG MYSQL_HOST
ARG MYSQL_PORT
ARG MYSQL_DATABASE
ARG MYSQL_USERNAME
ARG MYSQL_PASSWORD
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_PASSWORD
ARG REDIS_DATABASE
ARG FILE_STORAGE_PATH
ARG FILE_VISIT_PATH_PREFIX
ARG OPENAI_API_BASE_URL
ARG OPENAI_API_KEY
ARG OPENAI_DEFAULT_MODEL
ARG OPENAI_DEFAULT_MODEL_MAX_TOKENS
ARG OPENAI_CODE_MODEL
ARG OPENAI_CODE_MODEL_MAX_TOKENS
ARG PROMPT_GENERATE_SQL_OUTPUT_MODEL
ARG JWT_SECRET
ARG JWT_TTL_SECONDS
ARG DB_INIT_MODE
ARG SM2_PRIVATE_KEY
ARG SKILL_STORE_DIR

# Set environment variables from the build arguments.
ENV DB_TYPE=${DB_TYPE}
ENV MYSQL_HOST=${MYSQL_HOST}
ENV MYSQL_PORT=${MYSQL_PORT}
ENV MYSQL_DATABASE=${MYSQL_DATABASE}
ENV MYSQL_USERNAME=${MYSQL_USERNAME}
ENV MYSQL_PASSWORD=${MYSQL_PASSWORD}
ENV REDIS_HOST=${REDIS_HOST}
ENV REDIS_PORT=${REDIS_PORT}
ENV REDIS_PASSWORD=${REDIS_PASSWORD}
ENV REDIS_DATABASE=${REDIS_DATABASE}
ENV FILE_STORAGE_PATH=${FILE_STORAGE_PATH}
ENV FILE_VISIT_PATH_PREFIX=${FILE_VISIT_PATH_PREFIX}
ENV OPENAI_API_BASE_URL=${OPENAI_API_BASE_URL}
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV OPENAI_DEFAULT_MODEL=${OPENAI_DEFAULT_MODEL}
ENV OPENAI_DEFAULT_MODEL_MAX_TOKENS=${OPENAI_DEFAULT_MODEL_MAX_TOKENS}
ENV OPENAI_CODE_MODEL=${OPENAI_CODE_MODEL}
ENV OPENAI_CODE_MODEL_MAX_TOKENS=${OPENAI_CODE_MODEL_MAX_TOKENS}
ENV PROMPT_GENERATE_SQL_OUTPUT_MODEL=${PROMPT_GENERATE_SQL_OUTPUT_MODEL}
ENV JWT_SECRET=${JWT_SECRET}
ENV JWT_TTL_SECONDS=${JWT_TTL_SECONDS}
ENV DB_INIT_MODE=${DB_INIT_MODE}
ENV SM2_PRIVATE_KEY=${SM2_PRIVATE_KEY}
ENV SKILL_STORE_DIR=${SKILL_STORE_DIR}

# Set the entrypoint to run the application.
# The launcher script is typically in the bin directory of the release.
ENTRYPOINT ["/app/release/bin/entrypoint.sh", "start", "standalone_service", "mysql"]
