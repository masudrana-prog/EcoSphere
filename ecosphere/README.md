

# EcoSphere

### Smart Tree Plantation, Monitoring & Sustainability Platform

EcoSphere is a proposed web platform that brings together individuals, nurseries, NGOs, and administrators to support tree plantation and environmental awareness. Users can record their resource consumption, estimate their carbon footprint, submit evidence of tree planting, and follow the verification process. Verified activities contribute to sustainability rewards.

> **Project status:** This README summarizes the features and design described in the Software Requirements Specification (SRS). Individual features should be marked as implemented only after they are completed in the codebase.

## Main modules

| Module | Planned responsibilities |
| --- | --- |
| **User web portal** | Registration and login; profile management; carbon footprint calculation; monthly electricity, water, waste, and transport tracking; plantation location and tree search; photo-supported plantation submissions; status tracking; EcoPoints, badges, Green Score, leaderboard, and dashboard. |
| **Nursery portal** | Registration and approval; nursery profile; tree and sapling listings; stock and availability updates; plantation-related requests. |
| **NGO portal** | Registration and approval; review of plantation records and supporting evidence; approve or reject submissions with reasons; verification history and notifications. |
| **Admin dashboard** | Manage users, nurseries, NGOs, trees, plantations, rewards, reports, complaints, notifications, and platform analytics. |

## Plantation workflow

1. A user finds tree and nursery information and submits a plantation record with the species, date, location, and photo evidence.
2. The record starts as **Pending**.
3. An authorized NGO reviews the evidence and **Approves** or **Rejects** the record. Rejected submissions can include a reason.
4. The user sees the decision. Eligible approved records can update EcoPoints and other rewards; the same plantation must not receive duplicate rewards.
5. Administrators monitor submissions, verification, and platform activity.

## Proposed technology stack

| Layer | Technologies specified in the SRS |
| --- | --- |
| Frontend | HTML, CSS, JavaScript, Bootstrap |
| Backend | Java, Spring Boot, REST API |
| Data access | Spring Data JPA |
| Database | MySQL |
| Architecture | Model–View–Controller (MVC) with a centralized database |

The web interface is intended for modern browsers on desktop and mobile devices. Access to each module is controlled by user role.

## Future enhancements in the SRS

- GPS-based and photo geotagged plantation verification.
- AI-assisted verification and tree recommendations.
- Mobile applications and real-time notifications.
- Smart meter or IoT-based monitoring integrations.
- Advanced environmental analytics and digital certificates.

## Project team

| Role | Name |
| --- | --- |
| Project Manager | Afia Anam Mim |
| Team Lead | MD. Masud Rana |
| QA Lead | Fatiha Binte Shahid |
| Report Writing Lead | Sadiya Bani |

**Academic project:** Department of Computer Science and Engineering, University of Asia Pacific.

## Documentation

This overview is based on the project report, *EcoSphere – Smart Tree Plantation, Monitoring & Sustainability Platform*, dated September 7, 2026. The SRS includes functional and non-functional requirements, use case and activity diagrams, architecture, and component-level design.

> Setup and run instructions should be added from the actual repository configuration (for example, its build file and database settings).


# EcoSphere - Sprint 1: Foundation, Navigation & Submit Plantation
**Goal:** every role can sign in, and citizens can navigate their dashboard and submit trees they planted.

## Delivered in this sprint
- Project setup (Spring Boot 3, JPA, MySQL, Thymeleaf, Spring Security)
- `AppUser` entity + `UserRepo`; registration and login/logout (`AuthController`)
- BCrypt passwords, role-based URL rules, role-based redirect after login (`Config`)
- Account status: USER = ACTIVE; NGO / NURSERY / ADMIN = PENDING until approved
- **Navigation:** top bar (brand, role, logout) and dashboard side menu (Overview, Plantation Submissions, Submit Plantation) with a mobile quick-nav
- **Submit Plantation:** tree/species, location, planting date, photo upload and GPS ("Use my GPS"), saved as PENDING (`Plantation`, `PlantationRepo`, `UserController`)
- Submission list with status tracking (PENDING / VERIFIED / REJECTED) and Green Score / EcoPoints cards
- `DataSeeder` demo accounts and sample plantations; `HomeController` placeholder for NGO, Nursery and Admin

## Demo
Log in as `afia@ecosphere.com`, use the side menu, and submit a plantation with a photo and GPS. It appears as PENDING.

## Not yet built (later sprints)
Carbon consumption, badges, rank and leaderboard (S2) - NGO verification, admin, config (S3) - nursery marketplace and orders (S4)

## Run
1. Install JDK 17+, Maven 3.9+ and MySQL 8, and start MySQL.
2. Edit `src/main/resources/application.properties` (DB username/password). The `ecosphere` database is created automatically.
3. `mvn spring-boot:run` then open http://localhost:8080

Demo accounts (password `password123`): `afia@ecosphere.com` (User), `ngo@ecosphere.com` (NGO), `nursery@ecosphere.com` (Nursery), `admin@ecosphere.com` (Admin).
