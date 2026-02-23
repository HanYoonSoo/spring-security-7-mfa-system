import { verifyOttByToken } from "./api/authApi.js";
import { byId } from "./utils/dom.js";

function renderStatus(type, title, message) {
  const status = byId("callback-status");
  const icon = byId("callback-icon");
  const titleEl = byId("callback-title");
  const messageEl = byId("callback-message");

  status.classList.remove("pending", "success", "failed");
  status.classList.add(type);
  titleEl.textContent = title;
  messageEl.textContent = message;

  if (type === "success") {
    icon.textContent = "✓";
  } else if (type === "failed") {
    icon.textContent = "!";
  } else {
    icon.textContent = "...";
  }
}

async function run() {
  const goBackButton = byId("btn-go-back");
  const url = new URL(window.location.href);
  const token = url.searchParams.get("token");
  const returnUrl = url.searchParams.get("returnUrl") || "/";
  let redirectTimer = null;

  if (!token) {
    renderStatus("failed", "Authentication Failed", "token 파라미터가 없습니다. 매직링크를 다시 확인하세요.");
    return;
  }

  goBackButton.addEventListener("click", () => {
    window.location.href = returnUrl;
  });

  renderStatus("pending", "Checking Authentication", "인증 상태를 확인하고 있습니다.");

  try {
    await verifyOttByToken(token);
    renderStatus(
      "success",
      "Authenticated is Success",
      "MFA 인증이 완료되었습니다. 잠시 후 원래 페이지로 이동합니다.",
    );

    url.searchParams.delete("token");
    window.history.replaceState({}, "", url.toString());
    goBackButton.disabled = false;
    redirectTimer = window.setTimeout(() => {
      window.location.href = returnUrl;
    }, 2000);
  } catch (err) {
    if (redirectTimer) {
      window.clearTimeout(redirectTimer);
    }
    goBackButton.disabled = true;
    const reason = typeof err.body === "string"
      ? err.body
      : (err.body?.message ?? err.message);
    renderStatus(
      "failed",
      "Authentication Failed",
      `MFA 검증에 실패했습니다. (${reason})`,
    );
  }
}

run();
