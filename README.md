# 🧪 Laboratory Management System

## 📌 Project Overview

The **Laboratory Management System (LMS)** is an internal system designed to support the management and operation of hospital laboratory activities. The system focuses on user management, secure access control, and efficient handling of laboratory-related data to meet healthcare compliance and performance requirements.

This project was developed as an **academic group project** under a structured training program at **FPT Software Academy**. The repository represents a **learning-based clone/simulation** of a laboratory management system and is **not a production system**.

> ⚠️ This project is for educational and demonstration purposes only. It does not represent any real internal system of a hospital.

---

## 👥 Team & Scope

* **Project Type:** Academic / Training Project
* **Team Size:** 11 members
* **Development Period:** Oct 2025 – Dec 2025
* **Collaboration Model:** Group-based development following SRS and enterprise-style workflows

This repository does **not** imply that a single contributor implemented the entire system.

---

## 🙋‍♂️ My Responsibilities

I was responsible for designing and implementing the **Identity and Access Management (IAM) Service** and related integrations:

### 🔐 Authentication & Authorization

* Implemented authentication and authorization workflows using **Spring Data JPA**, **ORM**, and **PostgreSQL**
* Integrated **AWS Cognito** for login flows, access tokens, and refresh tokens
* Built APIs for login, first-time password setup, forgot-password, and token refresh

### 🔁 Token & Security Design

* Designed a **hybrid refresh-token model**:

  * Encrypted refresh tokens stored in the database for state validation
  * Cached in **Redis** for performance optimization
  * Secured using **HTTP-only cookies**
* Applied **AES-GCM**, **HmacSHA256**, and selective hashing/encryption to protect sensitive data
* Ensured alignment with **HIPAA** and **GDPR** security principles

### 👤 User & Role Management

* Implemented CRUD APIs for managing **users, roles, and privileges**
* Applied **role-based access control (RBAC)** based on SRS requirements

### 🔍 System Integration

* Supported the **Patient Service** by integrating **Elasticsearch** for medical record search, improving query performance

### 🌐 Frontend Integration

* Built the frontend integration layer using **React + TypeScript**
* Integrated all IAM APIs via **Axios**
* Applied routing and client-side role-based access control

### 🔑 Advanced Authentication Features

* Implemented a **custom Google OAuth2 login flow** with mandatory OTP verification
* Verified user existence and sent OTP via email
* Validated OTP in the backend before requesting tokens from Cognito
* Used **Redis** to prevent OTP spamming, manage OTP state, and handle expiration
* Integrated **JavaMailSender** for secure OTP email delivery

---

## 🎯 Purpose of This Repository

* Demonstrate hands-on experience with **enterprise-level IAM design**
* Practice secure authentication, authorization, and token management
* Showcase collaboration in a **large academic team project**
* Serve as a **portfolio and learning reference**

---

## 📄 Disclaimer

This repository is a **learning-oriented clone** created as part of an academic training program. All data, workflows, and implementations are **simulated** and do not reflect any real hospital system or proprietary business logic.
