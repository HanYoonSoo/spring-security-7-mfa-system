import { fetchMe } from "../../api/userApi.js";
import { byId, writeOutput } from "../../utils/dom.js";

export function bindUserActions() {
  byId("btn-me").addEventListener("click", async () => {
    try {
      const res = await fetchMe();
      writeOutput({ action: "getMyInfo", res });
    } catch (err) {
      writeOutput({ action: "getMyInfo", error: err.body ?? err.message });
    }
  });
}
