import { request } from "./http.js";

export function fetchPosts() {
  return request("/api/v1/posts");
}

export function createPost(payload) {
  return request("/api/v1/posts", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
