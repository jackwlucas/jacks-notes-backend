# Notes API

A RESTful API for managing notes and tags built with Spring Boot.

## Features

- **Note Management**: Create, read, update, delete, and archive notes
- **Tag System**: Organize notes with custom tags
- **User Isolation**: Each user can only access their own notes and tags
- **Pagination**: Efficient handling of large datasets
- **Tag Filtering**: Filter notes by specific tags
- **JWT Authentication**: Secure API access with JSON Web Tokens
- **Input Validation**: Comprehensive request validation
- **RESTful Design**: Clean, intuitive API endpoints

## Technology

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.2+** - Application framework
- **Spring Data JPA** - Data persistence layer
- **Spring Security** - Authentication and authorization
- **PostgreSQL** - Primary database
- **JWT** - Token-based authentication
- **Lombok** - Reducing boilerplate code
- **Maven** - Build and dependency management

## API Documentation

### Authentication

All endpoints require a valid JWT token:

```
Authorization: Bearer <jwt-token>
```

### Notes Endpoints

| Method | Endpoint                 | Description                   |
|--------|--------------------------|-------------------------------|
| GET    | `/api/notes`             | List user's notes (paginated) |
| GET    | `/api/notes?tag=example` | Filter notes by tag           |
| GET    | `/api/notes/{id}`        | Get specific note             |
| POST   | `/api/notes`             | Create new note               |
| PUT    | `/api/notes/{id}`        | Update entire note            |
| PATCH  | `/api/notes/{id}`        | Partially update note         |
| DELETE | `/api/notes/{id}`        | Delete note                   |

### Tags Endpoints

| Method | Endpoint         | Description                  |
|--------|------------------|------------------------------|
| GET    | `/api/tags`      | List user's tags (paginated) |
| GET    | `/api/tags/{id}` | Get specific tag             |
| POST   | `/api/tags`      | Create new tag               |
| PUT    | `/api/tags/{id}` | Update tag                   |
| DELETE | `/api/tags/{id}` | Delete tag                   |

