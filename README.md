# EAUT SUPPORT – Support Request and Incident Management System

**EAUT SUPPORT** is a web application developed to support the management, reception, assignment, and tracking of technical support requests in a university environment. The system enables students to submit requests when encountering issues, technicians to receive and resolve tasks, and administrators to access management and overall statistics functions.

The project is developed using Java Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, and MySQL, with the aim of applying knowledge of system analysis and design, Java web programming, and database management to a practical problem.

## 1. Project Objectives

* Build a centralized support request management system for a university.
* Enable students to submit support requests and track their processing progress.
* Enable technicians to receive, process, and update request statuses.
* Enable administrators to manage user accounts, incident categories, rooms, devices, and SLA configurations.
* Track processing history, notifications, and support service quality evaluations.
* Provide statistical data to support management activities.
* Apply layered architecture, DTOs, REST APIs, and authentication and authorization mechanisms in a Java application.

## 2. Technologies Used

| Technology              | Role                                             |
| ----------------------- | ------------------------------------------------ |
| Java JDK 21             | Programming language and development environment |
| Spring Boot 3.3.4       | Backend application development                  |
| Spring Security         | Authentication and authorization                 |
| Spring Data JPA         | Data access and manipulation                     |
| Hibernate               | ORM, mapping Java objects to the database        |
| Thymeleaf               | Server-side interface rendering                  |
| HTML5, CSS3, JavaScript | User interface development                       |
| MySQL                   | Database management system                       |
| Maven                   | Dependency management and build process          |
| XAMPP                   | Local database runtime environment               |
| Git, GitHub             | Source code management and development history   |

## 3. Main Features

### 3.1. Account Management and Authorization

* Log in and log out.
* Manage personal information.
* Change passwords.
* Manage user accounts.
* Support student, technician, and administrator roles.
* Manage account statuses.

### 3.2. Support Request Management

* Create new support requests.
* View request lists and details.
* Search and filter requests.
* Update request information.
* Track processing statuses.
* Cancel requests according to the system's business rules.
* Assign and reassign technicians.
* Receive requests, begin processing, and update progress.
* Request additional information.
* Complete or reject requests according to permissions and business workflows.

### 3.3. Technician Task Management

* View the list of assigned requests.
* View request information, submitter details, and incident locations.
* Accept assigned requests.
* Update the processing progress.
* Track statuses and processing history.

### 3.4. Category and Resource Management

* Manage incident categories.
* Manage classrooms and working rooms.
* Manage devices.
* Monitor device and room statuses.
* Link support requests to relevant categories, rooms, and devices.

### 3.5. History and Attachment Management

* Store the history of request changes and processing activities.
* Record the users who perform actions.
* Manage request attachments.
* Support uploading, viewing attachment lists, and downloading files according to the implemented features.

### 3.6. Notifications and Feedback

* Manage user notifications.
* Track read and unread statuses.
* Submit evaluations after requests have been processed according to the system's rules.
* View feedback and evaluation information.

### 3.7. Statistics and SLA Configuration

* Count requests by status.
* Generate request statistics by category and priority level.
* Aggregate statistics by technician.
* Monitor overdue requests and SLA alerts.
* Manage response time, resolution time, and alert time configurations according to priority levels.

## 4. User Roles

| Role          | Permission Code   | Main Responsibilities                                                                       |
| ------------- | ----------------- | ------------------------------------------------------------------------------------------- |
| Student       | `ROLE_STUDENT`    | Submit requests, track progress, update personal information, and evaluate support services |
| Technician    | `ROLE_TECHNICIAN` | Receive assigned requests, resolve incidents, and update processing progress                |
| Administrator | `ROLE_ADMIN`      | Manage accounts, resources, request assignments, system configurations, and statistics      |

Specific operations are controlled by Spring Security configuration and the business rules of each feature.

## 5. Request Processing Workflow

Support requests may go through the following statuses:

* `PENDING`: A new request has been created.
* `RECEIVED`: The request has been received.
* `ASSIGNED`: The request has been assigned.
* `IN_PROGRESS`: The request is being processed.
* `WAITING_INFO`: The request is waiting for additional information.
* `COMPLETED`: The request has been completed.
* `REJECTED`: The request has been rejected.
* `CANCELLED`: The request has been cancelled.

Status transitions are controlled according to the system's business rules to prevent invalid transitions.

## 6. System Architecture

The application is organized using a layered architecture model:

* **Presentation Layer:** Thymeleaf, HTML, CSS, and JavaScript for the user interface.
* **Controller Layer:** Receives HTTP requests, handles page navigation, and provides REST APIs.
* **Service Layer:** Implements business logic, validates conditions, and coordinates data processing.
* **Repository Layer:** Uses Spring Data JPA to query and manipulate the database.
* **Entity Layer:** Maps Java objects to MySQL tables.
* **DTO Layer:** Standardizes API input and output data, combined with validation to check data.
* **Security Layer:** Handles authentication and access authorization.

General processing flow:

`Browser → Controller → Service → Repository → MySQL`

Returned data is transferred through DTOs and used to display information on the interface or provide responses to API callers.

## 7. Directory Structure

```text
qlhotro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/donga/qlhotro/
│   │   │       ├── config/        # Application and Spring MVC configuration
│   │   │       ├── controller/    # Interface controllers
│   │   │       ├── api/           # REST API controllers
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── entity/         # JPA entities
│   │   │       ├── enums/          # Enum data types
│   │   │       ├── exception/      # Exception handling
│   │   │       ├── repository/     # Database access
│   │   │       ├── security/       # Authentication and user information
│   │   │       ├── service/        # Business logic interfaces
│   │   │       └── QlHoTroApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── templates/          # Thymeleaf templates
│   │       └── application.properties
│   └── test/                       # Test code, if any
├── .gitignore
├── pom.xml
└── README.md

```

*Note: The structure above is a general representation. Some directories or packages may differ depending on the actual project source code.*

## 8. Database

The database uses MySQL and is named:

`qlhotrotruonghoc`

The main table groups include:

| Data Group               | Tables                                    |
| ------------------------ | ----------------------------------------- |
| Users and authorization  | `users`, `roles`, `user_roles`            |
| Categories and resources | `categories`, `rooms`, `devices`          |
| Support requests         | `support_requests`, `request_assignments` |
| Processing tracking      | `request_history`, `request_attachments`  |
| User interactions        | `notifications`, `feedbacks`              |
| SLA management           | `sla_configs`                             |

The tables are linked through primary keys and foreign keys to maintain data relationships between users, requests, devices, rooms, and processing history.

## 9. Environment Requirements

Before running the project, prepare the following:

* JDK 21.
* Maven 3.9.x or a compatible version.
* MySQL or MariaDB compatible with the project configuration.
* XAMPP if using a local database environment.
* An IDE that supports Java, such as VS Code or Antigravity.
* Git to retrieve the source code.

## 10. Installation and Running Instructions

### Step 1: Clone the Source Code

```bash
git clone <GITHUB_REPOSITORY_URL>
cd qlhotro

```

Replace `<GITHUB_REPOSITORY_URL>` with the actual GitHub repository URL.

### Step 2: Create the Database

Start MySQL in XAMPP, then create the database:

```sql
CREATE DATABASE qlhotrotruonghoc
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

```

If the pre-designed database already exists, use the existing database instead of recreating it or deleting its data.

### Step 3: Configure the Connection

Open `src/main/resources/application.properties` and check the database connection parameters, for example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/qlhotrotruonghoc
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

```

The values above are examples. Adjust them according to your actual MySQL configuration and check the other properties in the existing configuration file.

Do not include actual database passwords, tokens, or confidential information in a public repository.

### Step 4: Build the Project

In Windows PowerShell, run:

```powershell
mvn clean package

```

To build without running tests:

```powershell
mvn -DskipTests package

```

### Step 5: Run the Application

```powershell
mvn spring-boot:run

```

Alternatively, run the generated JAR file in the `target` directory:

```powershell
java -jar target/qlhotro-0.0.1-SNAPSHOT.jar

```

The JAR filename may vary depending on the Maven version and configuration.

### Step 6: Access the Application

Open a browser and navigate to:

`http://localhost:8080`

Login page:

`http://localhost:8080/login`

## 11. Test Accounts

In the development environment, the application may initialize default test accounts if they do not already exist:

| Role          | Username | Sample Password |
| ------------- | -------- | --------------- |
| Administrator | `admin`  | `123456`      |
| Technician    | `kt001`  | `123456`       |
| Student       | `sv001`  | `123456`    |

These accounts are intended only for the development environment, according to the `DataInitializer` configuration. Change or disable the default credentials before deploying the application in a real environment; do not use these sample passwords in a public system.

## 12. REST API

The application provides groups of REST APIs to support communication between the interface and the Backend, including:

| API Group         | Representative Endpoint     | Function                          |
| ----------------- | --------------------------- | --------------------------------- |
| Users             | `/api/users`                | Account management                |
| Categories        | `/api/categories`           | Incident category management      |
| Rooms             | `/api/rooms`                | Room management                   |
| Devices           | `/api/devices`              | Device management                 |
| Support requests  | `/api/requests`             | Request management and processing |
| Technicians       | `/api/technician/requests`  | View assigned requests            |
| History           | `/api/history`              | Retrieve processing history       |
| Notifications     | `/api/notifications`        | Notification management           |
| Feedback          | `/api/feedbacks`            | Feedback management               |
| Statistics        | `/api/dashboard/statistics` | Overall statistics                |
| SLA configuration | `/api/sla`                  | Service time management           |

The actual endpoints, HTTP methods, and access permissions are defined in the corresponding API controllers and security configuration.

## 13. Security and Data Control

* Use Spring Security for authentication and authorization.
* Use BCrypt to hash passwords.
* Use DTOs to control data exchanged through APIs.
* Apply Jakarta Bean Validation to input data in features where validation is configured.
* Standardize API responses through `ApiResponse`.
* Handle exceptions centrally through `GlobalExceptionHandler`.
* Control request status transitions according to business rules.
* Access permissions, CSRF configuration, default login credentials, and deployment settings should be reviewed before deploying the application to a production environment.

## 14. Future Development Directions

Potential areas for further improvement include:

* Adding unit tests and integration tests.
* Strengthening API access control and security.
* Adding pagination, filtering, and advanced search.
* Completing dashboard statistics and charts.
* Optimizing attachment processing.
* Adding business activity logging and system monitoring.
* Deploying the application to a server or cloud platform.
* Adding API documentation and automated deployment processes.

## 15. Author and Purpose

The project was developed as part of Java software development learning and practice, focusing on the problem of managing support requests and resolving incidents in a university environment.

**Project Name:** EAUT SUPPORT

**Institution:** East Asia University of Technology

**Field:** Java Web Application Development

**Core Technologies:** Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, and MySQL

---
