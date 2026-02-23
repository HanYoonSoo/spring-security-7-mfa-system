# Toy MFA Backend (Spring Security 7 + FACTOR)

## 1. 프로젝트 목적

이 백엔드는 **Spring Security 7의 MFA FACTOR 권한 모델**을 실제로 검증하기 위해 만들었습니다.

핵심 목표:

- `PASSWORD`만 가진 사용자와 `PASSWORD + OTT`까지 가진 사용자를 분리
- Spring Security의 OTT 흐름을 그대로 쓰되, 아래 컴포넌트를 커스텀
  - `OneTimeTokenService` (`RedisOneTimeTokenService`)
  - `GenerateOneTimeTokenRequestResolver`
  - `AuthenticationConverter`
  - `SuccessHandler` / `FailureHandler`

---

## 2. 아키텍처 요약

- Framework: Spring Boot 4.x, Spring Security 7.x
- DB: MySQL
- Cache/Token Store: Valkey(Redis 호환)
- Auth:
  - Access Token: JWT (Authorization 헤더)
  - Refresh Token: HttpOnly Cookie + Redis 저장
- MFA 2차 인증:
  - OTT(One-Time Token) 생성
  - 이메일 매직 링크 전달
  - `/api/v1/auth/mfa/ott/verify`로 검증

---

## 3. 백엔드 프로젝트 구조

```text
src/main/java/com/hanyoonsoo/mfa
├─ api
│  ├─ dto/request        # 요청 DTO
│  ├─ dto/response       # 응답 DTO
│  └─ *Controller        # REST API 엔드포인트
├─ common                # ApiResponse, Pair 등 공통 타입
├─ config                # ObjectMapper, Redis, WebMvc, PasswordEncoder 설정
├─ entity                # JPA 엔티티(User, Post, UserRole)
├─ exception             # 도메인/인증 예외
├─ infra
│  ├─ email              # 메일 관련 enum
│  └─ redis
│     ├─ enums           # Redis 키/TTL enum
│     ├─ repository      # Redis 접근 레이어
│     └─ service         # Redis 조합 서비스(AuthRedisService)
├─ repository            # JPA Repository
├─ security
│  ├─ config             # SecurityFilterChain, allow path, cors properties
│  ├─ custom             # EntryPoint, AccessDeniedHandler, CORS 헤더 처리, UserDetailsService
│  ├─ exception          # JWT 인증 예외
│  ├─ filter             # JwtAuthenticationFilter
│  ├─ jwt                # JwtProvider, JwtUserClaims
│  ├─ mfa                # OTT Resolver/Converter/Service/Handler
│  └─ utils              # SecurityContext 유틸
└─ service               # Auth/User/Post/MFA 메일 서비스
```

## 4. FACTOR 권한 모델

권한 문자열:

- `PASSWORD`
- `OTT`

적용 정책:

- `GET /api/v1/users/me` -> `PASSWORD` 필요
- `GET /api/v1/posts/**` -> `PASSWORD` 필요
- `POST /api/v1/posts/**` -> `PASSWORD` + `OTT` 필요
- `POST /api/v1/auth/mfa/**` -> `PASSWORD` 필요
- `POST /api/v1/users`, `POST /api/v1/auth/sign-in` -> `permitAll`

Security 설정 위치:

- `src/main/java/com/hanyoonsoo/mfa/security/config/SecurityConfig.java`

---

## 5. OTT(MFA) 커스텀 구성 상세

### 5.1 Generate

- URL: `POST /api/v1/auth/mfa/ott/generate`
- Resolver: `MfaGenerateOneTimeTokenRequestResolver`
  - `SecurityContext`에서 `JwtUserClaims` 확인
  - 사용자 조회 후 OTT subject(현재 userId) 설정
  - 요청 attribute에 이메일(`mfaEmail`) 저장

### 5.2 Token Service

- 구현체: `RedisOneTimeTokenService`
- 저장 방식:
  - Token 원문은 저장하지 않고 SHA-256 해시 키 사용
  - `OTT:TOKEN:{tokenHash}` 키에 `userId|expiresAt` 저장
  - TTL 만료 자동 삭제
- 소비 방식:
  - 1회 사용 즉시 삭제
  - 실패 횟수(`OTT:ATTEMPTS:*`) 누적 및 제한

### 5.3 Verify

- URL: `POST /api/v1/auth/mfa/ott/verify`
- Converter: `MfaOttAuthenticationConverter`
  - request parameter의 `token` -> `OneTimeTokenAuthenticationToken`
- SuccessHandler: `MfaOttAuthenticationSuccessHandler`
  - `PASSWORD + OTT` factor로 새 Access/Refresh 발급
- FailureHandler: `MfaOttAuthenticationFailureHandler`
  - 표준 `ApiResponse`로 401 반환

### 5.4 Email

- `MfaOttGenerationSuccessHandler`에서 매직 링크 생성
- `MfaEmailService`에서 HTML 메일 전송
- 메일에는 raw URL 대신 버튼 기반 링크 사용

---

## 6. API 흐름 (권장 테스트 순서)

1. `POST /api/v1/users` 회원 생성
2. `POST /api/v1/auth/sign-in` 로그인 (PASSWORD factor 토큰)
3. `POST /api/v1/auth/mfa/ott/generate` 매직 링크 발송
4. 메일 링크 클릭 -> 프론트 콜백에서 verify 수행
5. `POST /api/v1/posts` 호출 (PASSWORD + OTT 필요)

---

## 7. 실행 방법

## 7.1 사전 요구사항

- Java 25
- Docker / Docker Compose
- (메일 테스트 시) SMTP 계정 정보

## 7.2 인프라 실행 (MySQL + Valkey)

```bash
cd backend/toy-mfa-system
docker compose up -d
```

기본 포트:

- MySQL: `30200`
- Valkey: `30300`

## 7.3 백엔드 실행

```bash
cd backend/toy-mfa-system
./gradlew bootRun
```

기본 서버:

- `http://localhost:8080`

---

## 8. 환경변수

`application.yml`에서 아래 변수들을 사용합니다.

### 8.1 데이터베이스

- `MFA_DB_URL` (default: `jdbc:mysql://localhost:30200/mfa?serverTimezone=UTC&characterEncoding=UTF-8`)
- `MFA_DB_USERNAME` (default: `mfa`)
- `MFA_DB_PASSWORD` (default: `mfa`)

### 8.2 메일(SMTP)

- `MFA_MAIL_HOST` (default: `smtp.gmail.com`)
- `MFA_MAIL_PORT` (default: `587`)
- `MFA_MAIL_USERNAME` (필수)
- `MFA_MAIL_PASSWORD` (필수, Gmail은 앱 비밀번호 권장)

### 8.3 Redis/Valkey

- `MFA_REDIS_HOST` (default: `localhost`)
- `MFA_REDIS_PORT` (default: `30300`)
- `MFA_REDIS_PASSWORD` (default: `password`)

### 8.4 JWT

- `MFA_JWT_SECRET` (base64 인코딩된 HMAC 키)
- `MFA_JWT_ISSUER` (default: `toy-mfa-system`)
- `MFA_JWT_ACCESS_TOKEN_EXPIRATION` (초, default: `1800` = 30분)
- `MFA_JWT_REFRESH_TOKEN_EXPIRATION` (초, default: `1296000` = 15일)

### 8.5 MFA 콜백

- `MFA_MAGIC_LINK_BASE_URL` (default: `http://localhost:3000/mfa/callback.html`)

---

## 9. IntelliJ 실행용 예시 환경변수

```text
MFA_DB_URL=jdbc:mysql://localhost:30200/mfa?serverTimezone=UTC&characterEncoding=UTF-8;MFA_DB_USERNAME=mfa;MFA_DB_PASSWORD=mfa;MFA_REDIS_HOST=localhost;MFA_REDIS_PORT=30300;MFA_REDIS_PASSWORD=password;MFA_MAIL_HOST=smtp.gmail.com;MFA_MAIL_PORT=587;MFA_MAIL_USERNAME=your_email@gmail.com;MFA_MAIL_PASSWORD=your_app_password;MFA_MAGIC_LINK_BASE_URL=http://localhost:3000/mfa/callback.html
```

---

## 10. 주의사항

- `verify`는 `PASSWORD` factor(Authorization 헤더)가 필요하므로 로그인 상태가 아니면 실패합니다.
- 매직 링크는 일회성 토큰이라 재사용하면 실패합니다.
- Refresh Token은 쿠키, Access Token은 헤더 방식입니다.
