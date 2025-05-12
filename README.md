# Translation Management Service

A scalable and high-performance REST API for managing translations across multiple languages and platforms.

## Features

- Support for multiple languages with ISO code compatibility
- Tag-based translation organization (mobile, desktop, web, etc.)
- Fast and efficient translation search by keys, content, or tags
- Redis-based caching for optimized performance
- JWT-based authentication
- Nested JSON export endpoint for frontend applications
- Built-in data loader for testing with 100k+ records
- Swagger/OpenAPI documentation

## Technologies

- Java 17
- Spring Boot 3.1.0
- Spring Security with JWT
- Spring Data JPA
- Redis for caching
- PostgreSQL
- Swagger/OpenAPI
- Maven

## Prerequisites

- JDK 17
- Maven
- PostgreSQL
- Redis

## Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/translation-service.git
cd translation-service
```

2. Configure PostgreSQL:
- Create a database named `translation_db`
- Update database credentials in `application.yml` if needed

3. Configure Redis:
- Ensure Redis is running on localhost:6379 (default)
- Update Redis configuration in `application.yml` if needed

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

## API Documentation

Access the Swagger UI documentation at: http://localhost:8080/swagger-ui.html

### Key Endpoints

- `POST /api/v1/auth/login` - Authenticate and get JWT token
- `GET /api/v1/translations/export/{languageCode}` - Export translations for a language
- `POST /api/v1/translations/search` - Search translations with pagination
- `POST /api/v1/translations` - Create a new translation
- `PUT /api/v1/translations/{key}/{languageCode}` - Update a translation
- `GET /api/v1/translations/{key}/{languageCode}` - Get a specific translation
- `DELETE /api/v1/translations/{key}/{languageCode}` - Delete a translation

## Performance Optimizations

1. **Database Indexing**
   - Indexed translation keys and language codes
   - Optimized query performance for frequently accessed data

2. **Caching Strategy**
   - Redis caching for translations and language data
   - Cache invalidation on updates
   - Configurable TTL for cached items

3. **Batch Processing**
   - Efficient bulk data loading
   - Batch size optimization for large datasets

4. **Query Optimization**
   - Fetch joins for related entities
   - Pagination for large result sets
   - Optimized search queries

## Security

- JWT-based authentication
- Token expiration and validation
- Protected endpoints with Spring Security
- CORS configuration for frontend access

## Loading Test Data

To populate the database with test data:

1. Enable the "dev" profile in `application.yml`:
```yaml
spring:
  profiles:
    active: dev
```

2. Start the application. The `TranslationDataLoader` will automatically:
   - Create sample languages (en, fr, es, de, it)
   - Generate 100,000+ sample translations
   - Add random tags to translations

## Performance Metrics

- Translation export response time: < 500ms for 100k+ records
- Search response time: < 200ms
- Cache hit ratio: > 90% for frequently accessed translations

## Design Decisions

1. **Nested Structure for Translations**
   - Hierarchical organization using dot notation (e.g., "common.errors.notFound")
   - Easily convertible to nested JSON for frontend consumption

2. **Caching Strategy**
   - Two-level caching with Redis
   - Separate caches for individual translations and language exports
   - Cache invalidation on updates to maintain consistency

3. **Database Schema**
   - Optimized indexes for common queries
   - Many-to-many relationship for tags
   - Efficient storage of translation content

4. **Security Implementation**
   - Stateless JWT authentication
   - Role-based access control
   - Token-based API protection

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
