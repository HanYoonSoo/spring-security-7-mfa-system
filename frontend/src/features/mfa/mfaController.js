import { generateOtt, verifyOttByToken } from "../../api/authApi.js";
import { byId, writeOutput } from "../../utils/dom.js";

export function bindMfaActions() {
  byId("btn-generate-ott").addEventListener("click", async () => {
    try {
      const res = await generateOtt();
      writeOutput({ action: "generateOtt", res, message: "메일함에서 매직링크를 확인하세요." });
    } catch (err) {
      writeOutput({ action: "generateOtt", error: err.body ?? err.message });
    }
  });

  byId("btn-verify-manual").addEventListener("click", async () => {
    const token = byId("manual-token").value.trim();
    if (!token) {
      writeOutput("수동 검증: token 값이 필요합니다.");
      return;
    }
    await verifyToken(token);
  });
}

export async function verifyToken(token) {
  try {
    const res = await verifyOttByToken(token);
    writeOutput({ action: "verifyOtt", res, message: "MFA 완료 토큰 발급됨" });
    return { ok: true, res };
  } catch (err) {
    writeOutput({ action: "verifyOtt", error: err.body ?? err.message });
    return { ok: false, err };
  }
}
