# Zakat Management System — API Endpoints Documentation

## Table of Contents
1. [Authentication](#1-authentication)
2. [User Management](#2-user-management)
3. [Donor Endpoints](#3-donor-endpoints)
4. [Beneficiary Endpoints](#4-beneficiary-endpoints)
5. [Zakat Assignment Endpoints](#5-zakat-assignment-endpoints-admin-only)
6. [Inventory Endpoints](#6-inventory-endpoints)
7. [Report & Dashboard Endpoints](#7-report--dashboard-endpoints)
8. [Receipt Endpoints](#8-receipt-endpoints)
9. [Priority Score Calculation](#9-priority-score-calculation)
10. [Sample Testing Flow](#10-sample-testing-flow)

---

## Endpoints Quick Reference

| # | Method | Endpoint | Purpose | Access |
|---|--------|----------|---------|--------|
| | | **Authentication** | | |
| 1 | POST | /api/users/register | Register new user (DONOR, ADMIN, BENEFICIARY) | Public |
| 2 | POST | /api/auth/login | Login to get JWT token | Public |
| 3 | GET | /api/auth/refresh | Refresh access token | Public |
| 4 | GET | /api/auth/me | Get current logged-in user info | Authenticated |
| 5 | DELETE | /api/auth/logout | Logout and revoke refresh token | Authenticated |
| | | **User Management** | | |
| 6 | GET | /api/users | Get all registered users | ADMIN |
| 7 | DELETE | /api/users/{id} | Delete a user by ID | ADMIN |
| | | **Donor** | | |
| 8 | POST | /api/donors | Donor donates money to org (creates receipt) | DONOR |
| 9 | GET | /api/donors/{id} | Get donor details by ID | DONOR, ADMIN |
| 10 | POST | /api/donors/{id}/beneficiaries | Direct donation to beneficiary | DONOR, ADMIN |
| 11 | GET | /api/donors/{id}/beneficiaries | Get beneficiaries donor donated to | DONOR, ADMIN |
| 12 | GET | /api/donors/{id}/total-donations | Get total amount donated by a donor | DONOR, ADMIN |
| | | **Beneficiary** | | |
| 13 | PATCH | /api/beneficiaries/form | Beneficiary submits/updates application form | BENEFICIARY |
| 14 | GET | /api/beneficiaries | Get all beneficiaries | ADMIN |
| 15 | GET | /api/beneficiaries/{id} | Get specific beneficiary by ID | ADMIN, BENEFICIARY |
| 16 | GET | /api/beneficiaries/queue | Get eligible beneficiaries by priority | ADMIN, DONOR |
| | | **Zakat Assignment** | | |
| 17 | POST | /api/assignments | Admin assigns money/inventory to beneficiary | ADMIN |
| 18 | GET | /api/assignments | Get all assignments | ADMIN |
| | | **Inventory** | | |
| 19 | POST | /api/inventory | Donor donates inventory item | DONOR |
| 20 | GET | /api/inventory | Get all inventory items | ADMIN |
| 21 | GET | /api/inventory/{id} | Get specific inventory item | ADMIN |
| 22 | GET | /api/inventory/available | Get available inventory items | ADMIN |
| | | **Receipt** | | |
| 23 | GET | /api/receipts | Get all receipts | ADMIN |
| 24 | GET | /api/receipts/donor/{donorId} | Get all receipts for a donor | ADMIN, DONOR |
| | | **Dashboard** | | |
| 25 | GET | /api/dashboard | Get dashboard summary | ADMIN |
| 26 | GET | /api/report | Get comprehensive system report | Public |

---

## 1. Authentication

### POST /api/users/register
**Purpose:** Register a new user (DONOR, BENEFICIARY, or ADMIN)

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "DONOR"
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "DONOR",
  "createdAt": "2026-04-28T10:00:00Z"
}
```

---

### POST /api/auth/login
**Purpose:** Authenticate and get access token

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### GET /api/auth/me
**Purpose:** Get currently logged in user (requires auth)

**Headers:** `Authorization: Bearer <access_token>`

**Response (200):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "DONOR"
}
```

---

### DELETE /api/auth/logout
**Purpose:** Logout and revoke refresh token

**Headers:** `Cookie: refreshToken=<token>`

**Response (200):** No content

---

## 2. User Management

### GET /api/users
**Purpose:** Get all users (requires auth as admin)

**Response (200):**
```json
[
  { "id": 1, "name": "John Doe", "email": "john@example.com", "role": "DONOR" },
  { "id": 2, "name": "Admin User", "email": "admin@zakat.com", "role": "ADMIN" }
]
```

### DELETE /api/users/{id}
**Purpose:** Delete a user (requires auth as admin)

**Response (204):** No content

---

## 3. Donor Endpoints

### POST /api/donors
**Purpose:** Donor donates money to the organization (requires auth)

**Request Body:**
```json
{
  "donorId": 1,
  "amount": 5000.00,
  "description": "Optional notes"
}
```

**Response (201):**
```json
{
  "receiptId": 1,
  "recepNum": "ZKT-2604-K9R3P",
  "donorName": "John Doe",
  "amount": 5000.00,
  "description": "Optional notes",
  "issuedAt": "2026-04-28T10:05:00Z"
}
```

---

### GET /api/donors/{id}
**Purpose:** Get donor details (requires auth)

**Response (200):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2026-04-28T10:00:00Z"
}
```

---

### POST /api/donors/{id}/beneficiaries
**Purpose:** Direct donation to a beneficiary (requires auth)

**Request Body:**
```json
{
  "beneficiaryId": 3,
  "amount": 500.00
}
```

**Response (201):** No content

---

### GET /api/donors/{id}/beneficiaries
**Purpose:** Get full details of beneficiaries the donor has donated to (requires auth)

**Response (200):** 
```json
[
  {
    "id": 3,
    "userId": 3,
    "fullName": "Jane Beneficiary",
    "reason": "Lost my job",
    "priorityScore": 70,
    "eligible": true
  }
]
```

---

### GET /api/donors/{id}/total-donations
**Purpose:** Get the sum of all money donated by a specific donor (requires auth)

**Response (200):** `7500.00`

---

## 4. Beneficiary Endpoints

### PATCH /api/beneficiaries/form
**Purpose:** Beneficiary submits or updates application form (requires auth). Multiple submissions are allowed and will refresh the `checkedAt` timestamp.

**Request Body:**
```json
{
  "beneficiaryId": 3,
  "reason": "Lost my job and need help",
  "dependents": 2,
  "income": 0,
  "emergency": false,
  "disability": false,
  "age": 35,
  "isOrphan": false,
  "hasDebt": true,
  "unemployed": true,
  "illness": false
}
```

**Response (200):**
```json
{
  "id": 3,
  "priorityScore": 70,
  "eligible": true,
  "checkedAt": "2026-05-04T12:00:00Z"
}
```

---

### GET /api/beneficiaries
**Purpose:** Get all beneficiaries (requires auth as admin)

**Response (200):**
```json
[
  { "id": 3, "priorityScore": 70, "eligible": true }
]
```

---

### GET /api/beneficiaries/{id}
**Purpose:** Get specific beneficiary details (requires auth as ADMIN or the logged-in BENEFICIARY)

**Response (200):**
```json
{
  "id": 3,
  "fullName": "Jane Beneficiary",
  "age": 35,
  "priorityScore": 70,
  "eligible": true,
  "totalReceivedValue": 500.00,
  "checkedAt": "2026-05-04T12:00:00Z"
}
```

---

### GET /api/beneficiaries/queue
**Purpose:** Get eligible beneficiaries sorted by priority (requires auth as ADMIN or DONOR)

**Response (200):**
```json
[
  { "id": 5, "priorityScore": 1000, "eligible": true },
  { "id": 3, "priorityScore": 70, "eligible": true }
]
```

---

## 5. Zakat Assignment Endpoints (Admin Only)

### POST /api/assignments
**Purpose:** Admin assigns money/inventory to beneficiary (requires auth as admin)

**Request Body (Money):**
```json
{
  "beneficiaryId": 3,
  "adminId": 2,
  "amountAssigned": 1000.00
}
```

**Request Body (Inventory):**
```json
{
  "beneficiaryId": 3,
  "adminId": 2,
  "inventoryId": 1
}
```

**Response (201):**
```json
{
  "id": 1,
  "amountAssigned": 1000.00,
  "assignedAt": "2026-04-28T10:15:00Z"
}
```

---

### GET /api/assignments
**Purpose:** Get all assignments (requires auth as admin)

**Response (200):**
```json
[
  { "id": 1, "amountAssigned": 1000.00 }
]
```

---

## 6. Inventory Endpoints

### GET /api/inventory
**Purpose:** Get all inventory items (requires auth)

**Response (200):**
```json
[
  { "id": 1, "name": "Laptop", "approxValue": 500.00, "status": "AVAILABLE" }
]
```

---

### GET /api/inventory/available
**Purpose:** Get available items (requires auth)

**Response (200):** List of available items

---

### GET /api/inventory/{id}
**Purpose:** Get specific item (requires auth)

---

### POST /api/inventory
**Purpose:** Donor donates inventory item (requires auth)

**Request Body:**
```json
{
  "donorId": 1,
  "name": "Used Laptop",
  "approxValue": 25000.00
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Used Laptop",
  "status": "AVAILABLE"
}
```

---

## 7. Dashboard & Report Endpoints

### GET /api/dashboard
**Purpose:** Get dashboard summary (requires auth as admin)

**Response (200):**
```json
{
  "totalDonors": 10,
  "totalBeneficiaries": 25,
  "totalDonated": 50000.00,
  "totalDistributed": 15000.00,
  "remaining": 35000.00
}
```

---

### GET /api/report
**Purpose:** Get comprehensive report (public - no authentication required)

**Response (200):**
```json
{
  "totalDonated": 50000.00,
  "totalDistributed": 15000.00,
  "remaining": 35000.00,
  "totalDonors": 10,
  "totalBeneficiaries": 25,
  "totalAssignments": 12
}
```

---

## 8. Receipt Endpoints

### GET /api/receipts
**Purpose:** Get all receipts (requires ADMIN role)

**Response (200):**
```json
[
  { "id": 1, "recepNum": "ZKT-2604-K9R3P", "amount": 5000.00, "DId": 1, "issuedAt": "2026-04-28T10:05:00Z" }
]
```

---

### GET /api/receipts/donor/{donorId}
**Purpose:** Get receipts for a donor

---

## 9. Priority Score Calculation

| Condition | Points |
|-----------|--------|
| Emergency case | +1000 |
| Orphan | +25 |
| Disability | +20 |
| Illness | +15 |
| Unemployed | +15 |
| Has debt | +10 |
| Age >= 65 | +10 |
| Zero income | +20 |
| Income < 5000 | +10 |
| Dependents | +5 per dependent (max 25) |

---

## 10. Sample Testing Flow

This section provides a simple step-by-step flow showing how to test the API with authentication.

### Step 1: Register Users

```
POST http://localhost:8080/api/users/register
{
  "name": "John Donor",
  "email": "john@donor.com",
  "password": "pass123",
  "role": "DONOR"
}

POST http://localhost:8080/api/users/register
{
  "name": "Admin User",
  "email": "admin@zakat.com",
  "password": "admin123",
  "role": "ADMIN"
}

POST http://localhost:8080/api/users/register
{
  "name": "Jane Beneficiary",
  "email": "jane@beneficiary.com",
  "password": "pass123",
  "role": "BENEFICIARY"
}
```

### Step 2: Login (Get Access Token)

```
POST http://localhost:8080/api/auth/login
{
  "email": "john@donor.com",
  "password": "pass123"
}
Response: {"accessToken": "eyJhbGciOiJIUzI1NiJ9..."}
Save as $DONOR_TOKEN

POST http://localhost:8080/api/auth/login
{
  "email": "admin@zakat.com",
  "password": "admin123"
}
Response: {"accessToken": "eyJhbGciOiJIUzI1NiJ9..."}
Save as $ADMIN_TOKEN

POST http://localhost:8080/api/auth/login
{
  "email": "jane@beneficiary.com",
  "password": "pass123"
}
Response: {"accessToken": "eyJhbGciOiJIUzI1NiJ9..."}
Save as $BENEFICIARY_TOKEN
```

### Step 3: Get Current User

```
GET http://localhost:8080/api/auth/me
Authorization: Bearer $DONOR_TOKEN

GET http://localhost:8080/api/auth/me
Authorization: Bearer $ADMIN_TOKEN

GET http://localhost:8080/api/auth/me
Authorization: Bearer $BENEFICIARY_TOKEN
```

### Step 4: Get All Users (Admin Only)

```
GET http://localhost:8080/api/users
Authorization: Bearer $ADMIN_TOKEN
```

### Step 5: Donor Makes Org Donation

```
POST http://localhost:8080/api/donors
Authorization: Bearer $DONOR_TOKEN
{
  "donorId": 1,
  "amount": 5000.00
}
```

### Step 6: Get Donor by ID

```
GET http://localhost:8080/api/donors/1
Authorization: Bearer $DONOR_TOKEN
```

### Step 7: Get All Inventory Items

```
GET http://localhost:8080/api/inventory
Authorization: Bearer $ADMIN_TOKEN
```

### Step 8: Add Inventory Item

```
POST http://localhost:8080/api/inventory
Authorization: Bearer $DONOR_TOKEN
{
  "donorId": 1,
  "name": "Used Laptop",
  "approxValue": 25000.00
}
```

### Step 9: Get Inventory by ID

```
GET http://localhost:8080/api/inventory/1
Authorization: Bearer $ADMIN_TOKEN
```

### Step 10: Get Available Inventory

```
GET http://localhost:8080/api/inventory/available
Authorization: Bearer $ADMIN_TOKEN
```

### Step 11: Beneficiary Submits Form

```
PATCH http://localhost:8080/api/beneficiaries/form
Authorization: Bearer $BENEFICIARY_TOKEN
{
  "beneficiaryId": 3,
  "reason": "Lost my job and need help",
  "dependents": 2,
  "income": 0,
  "emergency": false,
  "disability": false,
  "age": 35,
  "isOrphan": false,
  "hasDebt": true,
  "unemployed": true,
  "illness": false
}
```

### Step 12: Get All Beneficiaries (Admin Only)

```
GET http://localhost:8080/api/beneficiaries
Authorization: Bearer $ADMIN_TOKEN
```

### Step 13: Get Beneficiary by ID

```
GET http://localhost:8080/api/beneficiaries/3
Authorization: Bearer $ADMIN_TOKEN
```

### Step 14: Admin Approves Beneficiary

```sql
UPDATE beneficiary SET eligible = TRUE WHERE id = 3;
```

### Step 15: Get Eligible Beneficiary Queue

```
GET http://localhost:8080/api/beneficiaries/queue
Authorization: Bearer $ADMIN_TOKEN
```

### Step 16: Admin Assigns Zakat (Money)

```
POST http://localhost:8080/api/assignments
Authorization: Bearer $ADMIN_TOKEN
{
  "beneficiaryId": 3,
  "adminId": 2,
  "amountAssigned": 1000.00
}
```

### Step 17: Get All Assignments

```
GET http://localhost:8080/api/assignments
Authorization: Bearer $ADMIN_TOKEN
```

### Step 18: Direct Donation (Donor → Beneficiary)

```
POST http://localhost:8080/api/donors/1/beneficiaries
Authorization: Bearer $DONOR_TOKEN
{
  "beneficiaryId": 3,
  "amount": 500.00
}
```

### Step 19: Get Beneficiaries Donated To

```
GET http://localhost:8080/api/donors/1/beneficiaries
Authorization: Bearer $DONOR_TOKEN
```

### Step 20: Get Dashboard

```
GET http://localhost:8080/api/dashboard
Authorization: Bearer $ADMIN_TOKEN
```

### Step 21: Get Report

```
GET http://localhost:8080/api/report
Authorization: Bearer $ADMIN_TOKEN
```

### Step 22: Get All Receipts (Admin Only)

```
GET http://localhost:8080/api/receipts
Authorization: Bearer $ADMIN_TOKEN
```

### Step 23: Get Receipts by Donor ID

```
GET http://localhost:8080/api/receipts/donor/1
Authorization: Bearer $ADMIN_TOKEN
```

### Step 24: Logout

```
DELETE http://localhost:8080/api/auth/logout
```

*Document Version: 1.0*
*Last Updated: April 28, 2026*