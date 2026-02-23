import { initSessionUi } from "./state/sessionStore.js";
import { bindAuthActions } from "./features/auth/authController.js";
import { bindMfaActions } from "./features/mfa/mfaController.js";
import { bindPostActions } from "./features/posts/postController.js";
import { bindUserActions } from "./features/users/userController.js";

initSessionUi();
bindAuthActions();
bindUserActions();
bindMfaActions();
bindPostActions();
