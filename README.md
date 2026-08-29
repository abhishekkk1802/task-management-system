# Task Management API

A backend REST API for task tracking and project collaboration, built with Spring Boot. The application provides secure JWT-based authentication, task management, project collaboration, comments, file attachments, filtering, searching, and sorting.

## Features

### Authentication & User Management

* User registration
* Secure password hashing using BCrypt
* JWT-based authentication
* Login
* View current user profile
* Update current user profile
* Logout endpoint
* Duplicate email validation

### Task Management

* Create tasks
* View tasks accessible to the authenticated user
* View tasks assigned to the current user
* Update tasks
* Delete tasks
* Assign tasks to project members
* Update task status and priority
* Due dates
* Task filtering
* Task searching by title and description
* Task sorting

### Project Collaboration

* Create projects
* View projects
* Update projects
* Add members to projects
* Project roles

    * OWNER
    * MEMBER
* Project-level authorization

### Comments

* Add comments to tasks
* View task comments
* Update comments
* Delete comments
* Comment authorization

### Attachments

* Upload files to tasks
* View task attachments
* Delete attachments
* Store files on the server filesystem
* Store attachment metadata in PostgreSQL
* Maximum upload size of 10 MB

## Tech Stack

* **Java:** 17
* **Framework:** Spring Boot 4.1.0
* **Build Tool:** Gradle
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security
* **Authentication:** JWT
* **JWT Library:** JJWT 0.13.0
* **Validation:** Jakarta Bean Validation
* **API:** REST
* **File Storage:** Local filesystem

## Architecture

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Security is handled through a JWT authentication filter:

```text
Client
   │
   │ Authorization: Bearer <JWT>
   ▼
JwtAuthenticationFilter
   │
   ▼
JWTService
   │
   ▼
UserRepository
   │
   ▼
SecurityContext
   │
   ▼
Controller
```

The main application layers are:

```text
controller/
    REST API endpoints

service/
    Business logic and authorization

repository/
    Database access

entity/
    JPA entities

dto/
    API request/response objects

security/
    JWT authentication

exception/
    Application exceptions and global error handling

config/
    Spring Security configuration
```

## Authentication

The application uses JWT bearer authentication.

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

Example:

```json
{
  "name": "Abhishek",
  "email": "abhishek@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Example:

```json
{
  "email": "abhishek@example.com",
  "password": "password123"
}
```

The login response contains a JWT.

Authenticated requests should include:

```http
Authorization: Bearer <JWT>
```

## User Profile

### Get current user

```http
GET /api/v1/users/me
Authorization: Bearer <JWT>
```

### Update current user

```http
PUT /api/v1/users/me
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "name": "Abhishek Gangwar",
  "email": "abhishek@example.com"
}
```

## Tasks

### Create task

```http
POST /api/v1/tasks
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "title": "Learn Spring Security",
  "description": "Complete JWT authentication",
  "dueDate": null
}
```

### Get accessible tasks

```http
GET /api/v1/tasks
Authorization: Bearer <JWT>
```

A user can see a task when they are:

* The task creator
* The task assignee
* A member of the task's project

### Get assigned tasks

```http
GET /api/v1/tasks/assigned-to-me
Authorization: Bearer <JWT>
```

### Filtering

Filter by status:

```http
GET /api/v1/tasks?status=TODO
```

Filter by priority:

```http
GET /api/v1/tasks?priority=HIGH
```

Filter by project:

```http
GET /api/v1/tasks?projectId=<PROJECT_ID>
```

### Searching

Search task titles and descriptions:

```http
GET /api/v1/tasks?search=Spring
```

### Sorting

Supported sort fields:

```text
title
status
priority
dueDate
createdAt
updatedAt
```

Example:

```http
GET /api/v1/tasks?sortBy=dueDate&sortDirection=asc
```

### Update task

```http
PUT /api/v1/tasks/{taskId}
Authorization: Bearer <JWT>
Content-Type: application/json
```

### Delete task

```http
DELETE /api/v1/tasks/{taskId}
Authorization: Bearer <JWT>
```

Task deletion is restricted to the task creator or project owner.

## Projects

### Create project

```http
POST /api/v1/projects
Authorization: Bearer <JWT>
Content-Type: application/json
```

### Get project

```http
GET /api/v1/projects/{projectId}
Authorization: Bearer <JWT>
```

Only project members can access the project.

### Update project

```http
PUT /api/v1/projects/{projectId}
Authorization: Bearer <JWT>
Content-Type: application/json
```

Only the project owner can update the project.

### Add project member

```http
POST /api/v1/projects/{projectId}/members
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "email": "member@example.com"
}
```

Only the project owner can add members.

## Comments

### Add comment

```http
POST /api/v1/tasks/{taskId}/comments
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "content": "JWT authentication is completed."
}
```

Users who can collaborate on the task can add comments.

### Get comments

```http
GET /api/v1/tasks/{taskId}/comments
Authorization: Bearer <JWT>
```

### Update comment

```http
PUT /api/v1/comments/{commentId}
Authorization: Bearer <JWT>
Content-Type: application/json
```

### Delete comment

```http
DELETE /api/v1/comments/{commentId}
Authorization: Bearer <JWT>
```

A comment creator or project owner can modify/delete a comment.

## Attachments

Attachments are stored using the local filesystem, while their metadata is stored in PostgreSQL.

```text
uploads/
└── attachments/
    └── <generated-uuid>.pdf
```

The database stores:

```text
id
file_name
file_path
content_type
file_size
task_id
uploaded_by
created_at
```

### Upload attachment

```http
POST /api/v1/tasks/{taskId}/attachments
Authorization: Bearer <JWT>
Content-Type: multipart/form-data
```

Form field:

```text
file
```

Example using curl:

```bash
curl -X POST \
  http://localhost:8080/api/v1/tasks/<TASK_ID>/attachments \
  -H "Authorization: Bearer <JWT>" \
  -F "file=@/path/to/file.pdf"
```

### Get task attachments

```http
GET /api/v1/tasks/{taskId}/attachments
Authorization: Bearer <JWT>
```

### Delete attachment

```http
DELETE /api/v1/attachments/{attachmentId}
Authorization: Bearer <JWT>
```

The uploader or project owner can delete an attachment.

## Error Handling

The application provides centralized error handling using `@RestControllerAdvice`.

Common HTTP responses include:

| Status | Meaning                                           |
| ------ | ------------------------------------------------- |
| 400    | Invalid request / validation failure              |
| 401    | Authentication required or invalid authentication |
| 403    | Authenticated user does not have permission       |
| 404    | Resource not found                                |
| 409    | Resource conflict                                 |
| 500    | Internal server/file storage error                |

Validation errors return field-level error information.

## Database Configuration

The application uses PostgreSQL.

Example local database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=task_app
spring.datasource.password=task_app_password
```

Hibernate schema updates are currently enabled:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Create the PostgreSQL database before starting the application:

```sql
CREATE DATABASE task_management;
```

Create/configure the application database user as appropriate for your local PostgreSQL installation.

## Environment Variables

The JWT signing secret is supplied through an environment variable.

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000
```

Set the environment variable before running the application.

Linux/macOS:

```bash
export JWT_SECRET="your-long-random-secret"
```

Windows PowerShell:

```powershell
$env:JWT_SECRET="your-long-random-secret"
```

**Do not commit the real JWT secret or database credentials to GitHub.**

## Running the Application

### Prerequisites

Install:

* Java 17
* PostgreSQL
* Git

### Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd task-management
```

### Configure PostgreSQL

Create the database and configure the credentials in `application.properties`.

### Configure JWT secret

Set:

```text
JWT_SECRET
```

as an environment variable.

### Run with Gradle

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The application runs on:

```text
http://localhost:8080
```

## Build

To create a production build:

```bash
./gradlew clean build
```

Run tests:

```bash
./gradlew test
```

## Security & Authorization Model

The application uses two layers of security.

### Authentication

JWT authentication identifies the currently logged-in user.

### Authorization

Business-level authorization is enforced in the service layer.

Examples:

```text
Task
├── Creator
├── Assignee
└── Project Member
```

A user can access a task when they satisfy the application's task visibility rules.

Project ownership is required for sensitive project operations such as updating a project and adding members.

## Logout

The application exposes:

```http
POST /api/v1/auth/logout
```

The application uses stateless JWT authentication. Therefore, logout does not revoke an already-issued JWT on the server.

The client should discard the JWT after logout. An already-issued token remains valid until it expires.

A future production enhancement would be to introduce refresh-token rotation and server-side token revocation.

## Project Structure

```text
src/
└── main/
    └── java/
        └── com/
            └── abhishek/
                └── task_management/
                    ├── config/
                    ├── controller/
                    ├── dto/
                    ├── entity/
                    ├── exception/
                    ├── repository/
                    ├── security/
                    └── service/
```

## Future Improvements

The following features can be added as future extensions:

* Real-time task notifications using WebSockets or Server-Sent Events
* Generative AI for task description/summary generation
* Refresh-token based authentication
* Server-side JWT revocation
* Cloud/object storage for attachments
* Pagination for task and comment lists
* Automated integration tests
* API documentation using OpenAPI/Swagger
* Production database migrations using Flyway or Liquibase

## License

This project was created as a backend task-management application demonstrating REST API development, authentication, authorization, database persistence, and project collaboration using Spring Boot.
