uvicorn src.water_my_plant.main:app --reload# Water My Plant API Documentation

## Overview
This document describes the REST API for the Water My Plant application. The API follows RESTful principles and uses JSON for request/response bodies. Authentication is handled via JWT tokens.

## Base URL
All endpoints are prefixed with `/api/v1`

## Authentication
Most endpoints require authentication using a Bearer token. The token should be included in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### Authentication

#### Register New User
- **Purpose**: Create a new user account
- **Method**: POST
- **Path**: `/auth/register`
- **Auth Required**: No
- **Request Body**:
  ```typescript
  {
    username: string;  // Required, unique
    password: string;  // Required, min length 8
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    id: string;        // UUID
    username: string;
    is_active: boolean;
  }
  ```
- **Error Responses**:
  - 400: Username already exists
  - 422: Invalid input data

#### Login
- **Purpose**: Authenticate user and get access token
- **Method**: POST
- **Path**: `/auth/token`
- **Auth Required**: No
- **Request Body** (form data):
  ```typescript
  {
    username: string;
    password: string;
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    access_token: string;
    token_type: "bearer";
  }
  ```
- **Error Responses**:
  - 401: Invalid credentials
  - 422: Invalid input data

#### Get Current User
- **Purpose**: Get information about the authenticated user
- **Method**: GET
- **Path**: `/auth/me`
- **Auth Required**: Yes
- **Success Response** (200):
  ```typescript
  {
    id: string;        // UUID
    username: string;
    is_active: boolean;
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 400: Inactive user

### Plants

#### Create Plant
- **Purpose**: Add a new plant to user's collection
- **Method**: POST
- **Path**: `/plants`
- **Auth Required**: Yes
- **Request Body**:
  ```typescript
  {
    name: string;                    // Required, unique per user
    species: string;                 // Required
    location: string;                // Required
    watering_frequency_days: number; // Required, positive integer
    last_watered: string;           // Required, ISO datetime
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    id: string;                     // UUID
    name: string;
    species: string;
    location: string;
    watering_frequency_days: number;
    last_watered: string;           // ISO datetime
    owner_id: string;              // UUID
    created_at: string;            // ISO datetime
  }
  ```
- **Error Responses**:
  - 400: Plant name already exists for user
  - 401: Invalid or missing token
  - 422: Invalid input data

#### Get All Plants
- **Purpose**: Retrieve all plants owned by the authenticated user
- **Method**: GET
- **Path**: `/plants`
- **Auth Required**: Yes
- **Success Response** (200):
  ```typescript
  Array<{
    id: string;                     // UUID
    name: string;
    species: string;
    location: string;
    watering_frequency_days: number;
    last_watered: string;           // ISO datetime
    owner_id: string;              // UUID
    created_at: string;            // ISO datetime
  }>
  ```
- **Error Responses**:
  - 401: Invalid or missing token

#### Get Plant by ID
- **Purpose**: Retrieve a specific plant by its ID
- **Method**: GET
- **Path**: `/plants/{plant_id}`
- **Auth Required**: Yes
- **Success Response** (200):
  ```typescript
  {
    id: string;                     // UUID
    name: string;
    species: string;
    location: string;
    watering_frequency_days: number;
    last_watered: string;           // ISO datetime
    owner_id: string;              // UUID
    created_at: string;            // ISO datetime
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found

#### Update Plant
- **Purpose**: Modify an existing plant's details
- **Method**: PUT
- **Path**: `/plants/{plant_id}`
- **Auth Required**: Yes
- **Request Body**:
  ```typescript
  {
    name: string;                    // Optional
    species: string;                 // Optional
    location: string;                // Optional
    watering_frequency_days: number; // Optional, positive integer
    last_watered: string;           // Optional, ISO datetime
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    id: string;                     // UUID
    name: string;
    species: string;
    location: string;
    watering_frequency_days: number;
    last_watered: string;           // ISO datetime
    owner_id: string;              // UUID
    created_at: string;            // ISO datetime
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found
  - 422: Invalid input data

#### Delete Plant
- **Purpose**: Remove a plant from user's collection
- **Method**: DELETE
- **Path**: `/plants/{plant_id}`
- **Auth Required**: Yes
- **Success Response**: 204 No Content
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found

### Watering Events

#### Record Watering Event
- **Purpose**: Log a new watering event for a plant
- **Method**: POST
- **Path**: `/watering`
- **Auth Required**: Yes
- **Request Body**:
  ```typescript
  {
    plant_id: string;              // Required, UUID
    fertilizer_used: boolean;     // Required
    notes: string;                // Optional
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    id: string;                   // UUID
    plant_id: string;            // UUID
    watered_at: string;          // ISO datetime with timezone
    fertilizer_used: boolean;
    notes: string;
    created_at: string;          // ISO datetime with timezone
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found
  - 422: Invalid input data

#### Update Watering Event
- **Purpose**: Modify an existing watering event
- **Method**: PUT
- **Path**: `/watering/{event_id}`
- **Auth Required**: Yes
- **Request Body**:
  ```typescript
  {
    fertilizer_used: boolean;     // Optional
    notes: string;                // Optional
  }
  ```
- **Success Response** (200):
  ```typescript
  {
    id: string;                   // UUID
    plant_id: string;            // UUID
    watered_at: string;          // ISO datetime with timezone
    fertilizer_used: boolean;
    notes: string;
    created_at: string;          // ISO datetime with timezone
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Watering event not found
  - 422: Invalid input data

#### Delete Watering Event
- **Purpose**: Remove a watering event
- **Method**: DELETE
- **Path**: `/watering/{event_id}`
- **Auth Required**: Yes
- **Success Response**: 204 No Content
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Watering event not found

#### Get Plant Watering History
- **Purpose**: Retrieve all watering events for a specific plant
- **Method**: GET
- **Path**: `/watering/plant/{plant_id}`
- **Auth Required**: Yes
- **Success Response** (200):
  ```typescript
  Array<{
    id: string;                   // UUID
    plant_id: string;            // UUID
    watered_at: string;          // ISO datetime with timezone
    fertilizer_used: boolean;
    notes: string;
    created_at: string;          // ISO datetime with timezone
  }>
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found

#### Get Last Watering Event
- **Purpose**: Retrieve the most recent watering event for a plant
- **Method**: GET
- **Path**: `/watering/plant/{plant_id}/last`
- **Auth Required**: Yes
- **Success Response** (200):
  ```typescript
  {
    id: string;                   // UUID
    plant_id: string;            // UUID
    watered_at: string;          // ISO datetime with timezone
    fertilizer_used: boolean;
    notes: string;
    created_at: string;          // ISO datetime with timezone
  }
  ```
- **Error Responses**:
  - 401: Invalid or missing token
  - 404: Plant not found or no watering events

## Common Error Responses

### 400 Bad Request
```typescript
{
  detail: string;  // Error message
}
```

### 401 Unauthorized
```typescript
{
  detail: "Could not validate credentials";
  headers: {
    "WWW-Authenticate": "Bearer";
  }
}
```

### 404 Not Found
```typescript
{
  detail: string;  // Error message
}
```

### 422 Unprocessable Entity
```typescript
{
  detail: Array<{
    loc: string[];
    msg: string;
    type: string;
  }>;
}
```

## Data Types

### UUID
- Format: 8-4-4-4-12 hexadecimal digits
- Example: "123e4567-e89b-12d3-a456-426614174000"

### ISO Datetime
- Format: ISO 8601 with timezone
- Example: "2024-02-20T15:30:00+02:00"

## Notes for AI Agents

1. All endpoints return JSON responses
2. All datetime fields use ISO 8601 format with timezone information
3. All IDs are UUID strings
4. Authentication is required for all endpoints except `/auth/register` and `/auth/token`
5. The API uses JWT tokens for authentication
6. All endpoints are prefixed with `/api/v1`
7. Request bodies should be sent as JSON with `Content-Type: application/json`
8. Form data should be sent as `application/x-www-form-urlencoded`
9. All endpoints that require authentication will return 401 if the token is missing or invalid
10. The API follows RESTful principles and uses standard HTTP methods 