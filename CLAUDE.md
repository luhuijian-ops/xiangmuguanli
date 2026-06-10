# CLAUDE.md

This file provides guidance to Loo (claude.ai/code) when working with code in this repository.

## Project Overview

A project management system (项目管理系统) inspired by DingTalk Teambition, supporting WeChat and DingTalk OAuth login. It uses a front-end/back-end separated architecture.

- **Backend**: `backend-spring/` — Spring Boot 3.2 + Java 17 + Spring Data JPA + MySQL 8.0 + Spring Security + JWT
- **Frontend**: `frontend/` — Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router
- **Deleted legacy backend**: The `backend/` directory (Node.js/Express/Prisma) was removed; all backend work happens in `backend-spring/`.

## Common Commands

### Frontend
```bash
cd frontend
npm install
npm run dev          # Dev server on http://localhost:5173
npm run build        # Production build
npm run test         # Unit tests (Vitest)
npm run test:unit    # Alias for Vitest
npm run test:watch   # Vitest watch mode
npm run test:coverage # Vitest with coverage (v8 provider)
npm run test:e2e     # E2E tests (Playwright, tests/e2e/)
```

### Backend
```bash
cd backend-spring
mvn clean compile
mvn spring-boot:run  # Dev server on http://localhost:6200
mvn test             # Run Spring Boot tests
mvn clean package -DskipTests  # Production JAR build
```

### One-shot startup
```bash
./start-project.sh   # Linux/Mac
start-project.bat    # Windows
```
The startup script checks ports, compiles/starts the Spring Boot backend, installs deps/starts the Vite frontend, and tails logs.

## Architecture

### Backend (`backend-spring/`)
- **Entry point**: `XiangmuguanliApplication.java`
- **Configuration**: `application.yml` uses environment variables for DB (`DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`), JWT (`JWT_SECRET`), and OAuth (`WECHAT_*`, `DINGTALK_*`).
- **Layering**: Controller -> Service -> Repository -> Entity. DTOs live in `dto/request/` and `dto/response/`.
- **API response format**: All controllers return `ApiResponse<T>` (`backend-spring/.../dto/response/ApiResponse.java`) with fields `code`, `message`, `data`. `200` means success.
- **Security**: JWT token filter (`JwtAuthenticationFilter`) + `UserDetailsServiceImpl`. OAuth flows for WeChat and DingTalk are in `WeChatOAuthService` and `DingTalkOAuthService`.
- **File uploads**: Max 10MB; storage path configured by `UPLOAD_DIR` (defaults to `./uploads`).
- **Database**: JPA `ddl-auto: update` against MySQL. Entities use `BaseEntity` for common audit fields.

### Frontend (`frontend/`)
- **Entry point**: `src/main.ts`. Global registration of all Element Plus icons.
- **Vite config**: Dev/proxy targets `http://localhost:6200` for `/api`. Port 5173.
- **API layer**: `src/api/request.ts` creates an Axios instance. It injects `Bearer <token>` from `localStorage`, and on `401` clears tokens and redirects to `/login`. The actual API modules are in `src/api/*.ts` and barrel-exported from `src/api/index.ts`.
- **State management**: Pinia stores in `src/stores/`. Key stores: `user` (token/userInfo/isAdmin), `project` (myProjects/currentProject).
- **Routing**: `src/router/index.ts` uses `createWebHistory`. Route meta: `requiresAuth`, `requiresAdmin`. The global `beforeEach` guard:
  - Redirects unauthenticated users to `/login`.
  - Fetches user info if missing.
  - Blocks non-admin users from `requiresAdmin` routes.
  - Validates project membership for `/projects/:id*` routes before allowing access.
- **Page structure**: `src/pages/` grouped by feature (`auth/`, `dashboard/`, `projects/`, `tasks/`, `schedule/`, `statistics/`, `users/`, `audit/`). Layout is `src/layouts/MainLayout.vue`.
- **Environment variables**: `VITE_API_BASE_URL` (empty in dev, points to production API in prod).

### Testing
- **Frontend unit**: Vitest + jsdom. Config in `vitest.config.ts`.
- **Frontend E2E**: Playwright. Config in `playwright.config.ts`. Base URL `http://localhost:3001`.
- **Backend unit/integration**: Spring Boot Test + `spring-security-test`. H2 is available for test runtime scope.

## Critical Development Constraints (from README)

These rules are project policy and must be followed strictly:
- **Always read the original file before modifying it.** Never guess file contents, structure, errors, or outputs.
- **Only make minimal, targeted fixes.** Do not perform global refactors, architecture changes, batch renames, or directory restructuring without explicit user approval.
- **Additive changes only.** Do not modify, delete, or change existing mature business logic unless explicitly instructed. Existing code should stay as-is.
- When suggesting changes, always cite: **filename + approximate line number + original snippet**.
- If unsure about business logic or configuration, say so directly instead of fabricating an explanation.
