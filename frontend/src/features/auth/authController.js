import { signIn, signUp } from "../../api/authApi.js";
import { byId, writeOutput } from "../../utils/dom.js";

function readUserForm() {
  return {
    username: byId("username").value.trim(),
    password: byId("password").value,
    email: byId("email").value.trim(),
  };
}

export function bindAuthActions() {
  byId("btn-sign-up").addEventListener("click", async () => {
    const { username, password, email } = readUserForm();
    if (!username || !password || !email) {
      writeOutput("회원 생성: username/password/email 모두 필요합니다.");
      return;
    }

    try {
      const res = await signUp({ username, password, email });
      writeOutput({ action: "signUp", res });
    } catch (err) {
      writeOutput({ action: "signUp", error: err.body ?? err.message });
    }
  });

  byId("btn-sign-in").addEventListener("click", async () => {
    const { username, password } = readUserForm();
    if (!username || !password) {
      writeOutput("로그인: username/password 필요합니다.");
      return;
    }

    try {
      const res = await signIn({ username, password });
      writeOutput({ action: "signIn", res });
    } catch (err) {
      writeOutput({ action: "signIn", error: err.body ?? err.message });
    }
  });
}
