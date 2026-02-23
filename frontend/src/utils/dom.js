export function byId(id) {
  const el = document.getElementById(id);
  if (!el) {
    throw new Error(`Element not found: ${id}`);
  }
  return el;
}

export function writeOutput(payload) {
  const output = byId("output");
  output.textContent = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
}
