import { request } from "./http.js";

export function fetchMe() {
  return request("/api/v1/users/me");
}
