#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "../..");
const common = path.join(root, "composeApp/src/commonMain/kotlin/com/vortexa");
const excluded = path.join(root, "composeApp/migrated/excluded-from-common/platform-heavy");

const moveDirs = [];

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function moveIfExists(rel) {
  const src = path.join(common, rel);
  if (!fs.existsSync(src)) return;
  const dest = path.join(excluded, rel);
  ensureDir(path.dirname(dest));
  if (fs.existsSync(dest)) fs.rmSync(dest, { recursive: true, force: true });
  fs.renameSync(src, dest);
}

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(p, out);
    else if (entry.isFile() && p.endsWith(".kt")) out.push(p);
  }
  return out;
}

for (const dir of moveDirs) moveIfExists(dir);

for (const file of walk(common)) {
  let text = fs.readFileSync(file, "utf8");
  let changed = false;

  const drawableNames = [...text.matchAll(/\bRes\.drawable\.([A-Za-z_][A-Za-z0-9_]*)/g)]
    .map((m) => m[1])
    .filter((name, index, arr) => arr.indexOf(name) === index)
    .sort();

  if (drawableNames.length) {
    if (!text.includes("import vortexa.composeapp.generated.resources.Res")) {
      text = text.replace(/^(package .+\n)/m, "$1\nimport vortexa.composeapp.generated.resources.Res\n");
      changed = true;
    }
    const missing = drawableNames
      .map((name) => `import vortexa.composeapp.generated.resources.${name}`)
      .filter((line) => !text.includes(line));
    if (missing.length) {
      const importMatches = [...text.matchAll(/^import .+$/gm)];
      if (importMatches.length) {
        const last = importMatches[importMatches.length - 1];
        const insertAt = last.index + last[0].length;
        text = text.slice(0, insertAt) + "\n" + missing.join("\n") + text.slice(insertAt);
      } else {
        text = text.replace(/^(package .+\n)/m, "$1\n" + missing.join("\n") + "\n");
      }
      changed = true;
    }
  }

  if (text.includes("import androidx.annotation.DrawableRes")) {
    text = text.replace(/^import androidx\.annotation\.DrawableRes\n/gm, "");
    text = text.replace(/@DrawableRes\s+/g, "");
    if (!text.includes("import org.jetbrains.compose.resources.DrawableResource")) {
      const anchor = "import org.jetbrains.compose.resources.painterResource\n";
      if (text.includes(anchor)) {
        text = text.replace(anchor, "import org.jetbrains.compose.resources.DrawableResource\n" + anchor);
      }
    }
    changed = true;
  }

  text = text.replace(/::class\.java/g, "::class");
  if (text !== fs.readFileSync(file, "utf8")) changed = true;

  if (changed) fs.writeFileSync(file, text);
}
