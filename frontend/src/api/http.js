import { env } from "../config/env.js";
import { getAccessToken, setAccessToken } from "../state/sessionStore.js";

async function parseBody(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function request(path, options = {}) {
  const token = getAccessToken();
  const headers = new Headers(options.headers ?? {});

  if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData) && !(options.body instanceof URLSearchParams)) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    method: options.method ?? "GET",
    credentials: "include",
    ...options,
    headers,
  });

  const authHeader = response.headers.get("Authorization");
  if (authHeader?.startsWith("Bearer ")) {
    setAccessToken(authHeader.slice("Bearer ".length));
  }

  const body = await parseBody(response);
  if (!response.ok) {
    const error = new Error(`HTTP ${response.status}`);
    error.status = response.status;
    error.body = body;
    throw error;
  }
  return body;
}
