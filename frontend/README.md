# Toy MFA Frontend

백엔드(Spring Security 7 FACTOR + OTT MFA)를 빠르게 검증하기 위한 테스트 프론트입니다.

짧은 코멘트: 이 프론트는 **AI를 활용해 초기 구조와 테스트 UI를 구성**했습니다.

---

## 1. 실행 방법

```bash
cd frontend
npm install
npm run dev
```

기본 주소:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`

---

## 2. 환경변수

`frontend/.env` 파일(필요 시):

```bash
VITE_API_BASE_URL=http://localhost:8080
```

---

## 3. 화면/라우트

- `/` : 테스트 콘솔 메인
  - 회원 생성
  - 로그인
  - 내 정보 조회
  - 매직 링크 발송
  - 게시글 조회/생성
- `/mfa/callback.html` : 매직 링크 콜백 페이지
  - `token` 쿼리로 자동 검증
  - 성공 시 상태 UI 표시 후 자동 리다이렉트
  - 실패 시 자동 리다이렉트 없이 실패 메시지 표시

---

## 4. 사용 흐름 (권장)

1. 회원 생성
2. 로그인 (PASSWORD factor 토큰 획득)
3. 매직 링크 발송 요청
4. 이메일 링크 클릭 -> `/mfa/callback.html?token=...`
5. 콜백 성공 후 원래 페이지 복귀
6. `POST /api/v1/posts` 호출로 MFA(OTT) 완료 여부 확인

---

## 5. 동작 포인트

- API 응답 헤더의 `Authorization: Bearer ...`를 읽어 access token 갱신
- CORS + 쿠키(`credentials: include`)를 사용해 refresh cookie를 백엔드와 연동
- 백엔드 권한 정책 반영:
  - `GET /api/v1/posts` : PASSWORD만 필요
  - `POST /api/v1/posts` : PASSWORD + OTT 필요

---

## 6. 트러블슈팅

- 로그인 후에도 `ott/generate`가 실패하면:
  - Network 탭에서 `Authorization` 헤더 포함 여부 확인
- 콜백에서 실패하면:
  - 백엔드 로그의 `RedisOneTimeTokenService` 메시지 확인
  - 토큰 만료/재사용 여부 확인
- API가 백엔드에 도달하지 않으면:
  - `VITE_API_BASE_URL` 값과 백엔드 실행 포트 확인
