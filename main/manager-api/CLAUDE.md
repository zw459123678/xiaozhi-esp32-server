# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.4.3 backend API for the XiaoZhi ESP32 device management system. It's a multi-module Java application built with Maven that provides REST APIs for managing intelligent agents, devices, models, and user administration.

## Development Environment

- **Java**: JDK 21
- **Build Tool**: Maven 3.8+
- **Framework**: Spring Boot 3.4.3
- **Database**: MySQL 8.0+ with Liquibase for migrations
- **Cache**: Redis 5.0+
- **Security**: Apache Shiro with JWT token authentication
- **API Documentation**: Swagger/OpenAPI 3 with Knife4j

## Common Commands

### Building and Running
```bash
# Build the project (skips tests by default in pom.xml)
mvn clean compile

# Build with tests
mvn clean test

# Package the application
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the jar directly after packaging
java -jar target/xiaozhi-esp32-api.jar
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DeviceTest

# Run tests with coverage
mvn clean test jacoco:report
```

### Development
- **API Documentation**: http://localhost:8002/xiaozhi/doc.html (after starting the application)
- **Server Port**: 8002
- **Context Path**: /xiaozhi
- **Active Profile**: dev (configured in application.yml)

## Architecture Overview

### Module Structure
The application follows a modular architecture with these main functional modules:

- **agent**: AI agent management, chat history, voice print recognition, and MCP access points
- **device**: ESP32 device registration, binding, OTA updates, and management
- **model**: AI model configuration and provider management (LLM models, voice models)
- **security**: Authentication, authorization, JWT token management, and Shiro configuration
- **sys**: System administration, user management, dictionary data, and parameters
- **timbre**: Voice/audio management for TTS functionality

### Key Components
- **Base Classes**: `BaseEntity`, `BaseService`, `CrudService` for common CRUD operations
- **Security**: OAuth2-style token authentication with Shiro realm
- **Database**: MyBatis-Plus for ORM with XML mappers in `/mapper/` directory
- **Caching**: Redis integration with custom key management
- **Validation**: Bean validation with custom groups (`AddGroup`, `UpdateGroup`)
- **Exception Handling**: Global exception handler with custom `RenException`

### Database Management
- **Migrations**: Liquibase changesets in `src/main/resources/db/changelog/`
- **Mappers**: MyBatis XML mappers in `src/main/resources/mapper/`
- **Entity Naming**: All entities end with `Entity` suffix
- **Primary Keys**: Uses `ASSIGN_ID` strategy (snowflake IDs)

### API Design
- RESTful endpoints grouped by functionality
- Swagger documentation with grouped APIs (device, agent, models, etc.)
- Standard Result wrapper for all API responses
- Permission-based access control using `@RequiresPermissions`

### Testing
- JUnit 5 platform with test classes in standard Maven test directory
- Test classes follow naming convention: `*Test.java`
- Integration tests available for key components (Device, Login, Utils)

## Configuration Files
- **Main Config**: `application.yml` and `application-dev.yml`
- **Database**: Configured through Spring Boot auto-configuration
- **Redis**: Custom configuration in `RedisConfig`
- **Security**: Shiro configuration with custom filters and realms