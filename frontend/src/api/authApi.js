import { request } from "./http.js";

export function signUp(payload) {
  return request("/api/v1/users", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function signIn(payload) {
  return request("/api/v1/auth/sign-in", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function generateOtt() {
  return request("/api/v1/auth/mfa/ott/generate", {
    method: "POST",
  });
}

export function verifyOttByToken(token) {
  const body = new URLSearchParams({ token });
  return request("/api/v1/auth/mfa/ott/verify", {
    method: "POST",
    body,
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  });
}
