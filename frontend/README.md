# Frontend (MFA Test Console)

## Run

```bash
cd frontend
npm install
npm run dev
```

- 기본 주소: http://localhost:3000
- 백엔드 기본 주소: http://localhost:8080
- 변경하려면 `.env` 파일에 아래 설정 추가:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Test Flow

1. 회원 생성 (`회원 생성`)
2. 로그인 (`로그인`) - PASSWORD factor access token 발급
3. 매직링크 발송 요청 (`매직링크 발송 요청`)
4. 이메일 링크 클릭으로 `/mfa/callback.html?token=...` 진입 (혹은 수동 토큰 검증)
5. MFA 완료 후 포스트 생성 테스트 (`포스트 생성`)

## Notes

- `GET /api/v1/posts`는 PASSWORD factor만 필요
- `POST /api/v1/posts`는 PASSWORD + OTT factor 필요
- 응답 헤더의 `Authorization`(Bearer)을 자동으로 저장/갱신합니다.
