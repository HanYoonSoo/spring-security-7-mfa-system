import { byId } from "../utils/dom.js";

const ACCESS_TOKEN_KEY = "toy_mfa_access_token";
let accessToken = localStorage.getItem(ACCESS_TOKEN_KEY) ?? "";

function decodePayload(token) {
  try {
    const base64 = token.split(".")[1];
    if (!base64) return {};
    const normalized = base64.replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(decodeURIComponent(escape(atob(normalized))));
  } catch {
    return {};
  }
}

export function setAccessToken(token) {
  accessToken = token ?? "";
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
  renderSession();
}

export function getAccessToken() {
  return accessToken;
}

export function clearSession() {
  setAccessToken("");
}

export function initSessionUi() {
  byId("btn-clear-session").addEventListener("click", clearSession);
  renderSession();
}

function renderSession() {
  const accessTokenEl = document.getElementById("access-token");
  const payloadEl = document.getElementById("token-payload");
  if (!accessTokenEl || !payloadEl) {
    return;
  }

  accessTokenEl.value = accessToken;
  payloadEl.textContent = JSON.stringify(decodePayload(accessToken), null, 2);
}
