import { createPost, fetchPosts } from "../../api/postApi.js";
import { byId, writeOutput } from "../../utils/dom.js";

export function bindPostActions() {
  byId("btn-list-posts").addEventListener("click", async () => {
    try {
      const res = await fetchPosts();
      writeOutput({ action: "listPosts", res });
    } catch (err) {
      writeOutput({ action: "listPosts", error: err.body ?? err.message });
    }
  });

  byId("btn-create-post").addEventListener("click", async () => {
    const title = byId("post-title").value.trim();
    const content = byId("post-content").value.trim();
    if (!title || !content) {
      writeOutput("포스트 생성: title/content가 필요합니다.");
      return;
    }

    try {
      const res = await createPost({ title, content });
      writeOutput({ action: "createPost", res });
    } catch (err) {
      writeOutput({ action: "createPost", error: err.body ?? err.message });
    }
  });
}
