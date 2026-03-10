# URL Shortener Service

A simple URL shortener service built with Spring Boot. It allows users to create short URLs that redirect to long URLs.

## Project Structure

- controllers - REST API endpoints
- services - Business logic
- respositories - data access layer
- entities - domain model
- dtos - request/response models
- exceptions - custom exceptions
- advices - global exception handler
- filters - http request interceptors
- configs - application configuration
- utils - helper classes

## Running the Application

```./gradlew bootRun --args='--spring.profiles.active=dev'```
