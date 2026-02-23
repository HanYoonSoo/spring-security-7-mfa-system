# toy-mfa-system

Spring Security 7의 MFA FACTOR(`PASSWORD`, `OTT`)를 실제로 적용해보기 위한 학습/검증용 프로젝트입니다.

핵심 포인트:

- `PASSWORD`만으로 접근 가능한 API와 `PASSWORD + OTT`가 모두 필요한 API를 분리
- OTT(One-Time Token) 흐름을 Spring Security 기본 구조 위에서 커스텀 구현
  - `OneTimeTokenService`
  - `GenerateOneTimeTokenRequestResolver`
  - `AuthenticationConverter`
  - `SuccessHandler` / `FailureHandler`
- 이메일 매직링크 + 콜백 페이지로 MFA 검증

## 프로젝트 구조

- `/backend/toy-mfa-system` : Spring Boot 백엔드
- `/frontend` : MFA 테스트용 프론트

상세 문서:

- 백엔드: `/backend/toy-mfa-system/README.md`
- 프론트엔드: `/frontend/README.md`

## 빠른 실행

1. 인프라 실행(MySQL, Valkey)

```bash
cd backend/toy-mfa-system
docker compose up -d
```

2. 백엔드 실행

```bash
cd backend/toy-mfa-system
./gradlew bootRun
```

3. 프론트 실행

```bash
cd frontend
npm install
npm run dev
```

기본 주소:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`
