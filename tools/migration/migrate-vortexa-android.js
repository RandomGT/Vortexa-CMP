#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const sourceRoot =
  process.argv[2] || "/Users/luxin/Documents/android/outline/vortexa-android";
const targetRoot =
  process.argv[3] || "/Users/luxin/Documents/android/outline/Vortexa-cmp";

const sourceJava = path.join(sourceRoot, "app/src/main/java/com/vortexa");
const targetCandidate = path.join(
  targetRoot,
  "composeApp/migrated/android-source-candidate/com/vortexa",
);
const targetResources = path.join(
  targetRoot,
  "composeApp/src/commonMain/composeResources/drawable",
);

const kotlinDirs = [
  "ui",
  "model",
  "repository",
  "api",
  "config",
  "router",
  "session",
  "net",
  "util",
];

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function copyDir(src, dest) {
  if (!fs.existsSync(src)) return;
  ensureDir(dest);
  for (const item of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, item.name);
    const destPath = path.join(dest, item.name);
    if (item.isDirectory()) copyDir(srcPath, destPath);
    else fs.copyFileSync(srcPath, destPath);
  }
}

function walk(dir, predicate, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const item of fs.readdirSync(dir, { withFileTypes: true })) {
    const itemPath = path.join(dir, item.name);
    if (item.isDirectory()) walk(itemPath, predicate, out);
    else if (!predicate || predicate(itemPath)) out.push(itemPath);
  }
  return out;
}

function rewriteResources(file) {
  let source = fs.readFileSync(file, "utf8");
  const before = source;
  source = source.replace(/^import com\.vortexa\.R\n/gm, "");
  source = source.replace(
    /\bcom\.vortexa\.R\.(drawable|mipmap)\.([A-Za-z0-9_]+)/g,
    "Res.drawable.$2",
  );
  source = source.replace(
    /\bR\.(drawable|mipmap)\.([A-Za-z0-9_]+)/g,
    "Res.drawable.$2",
  );
  source = source.replace(
    /painterResource\(id = (Res\.drawable\.[A-Za-z0-9_]+)\)/g,
    "painterResource($1)",
  );
  if (
    source.includes("Res.drawable.") &&
    !source.includes("vortexa.composeapp.generated.resources.Res")
  ) {
    const lines = source.split("\n");
    let insertAt = 0;
    while (
      insertAt < lines.length &&
      (lines[insertAt].startsWith("package ") || lines[insertAt].trim() === "")
    ) {
      insertAt += 1;
    }
    while (insertAt < lines.length && lines[insertAt].startsWith("import ")) {
      insertAt += 1;
    }
    lines.splice(insertAt, 0, "import vortexa.composeapp.generated.resources.Res");
    source = lines.join("\n");
  }
  if (source !== before) fs.writeFileSync(file, source);
}

function copyResources() {
  ensureDir(targetResources);
  const resRoot = path.join(sourceRoot, "app/src/main/res");
  for (const subdir of ["drawable", "mipmap-xxhdpi"]) {
    const dir = path.join(resRoot, subdir);
    for (const file of walk(dir, (p) => /\.(xml|png|webp|jpe?g)$/i.test(p))) {
      fs.copyFileSync(file, path.join(targetResources, path.basename(file)));
    }
  }
}

function writeReport() {
  const files = walk(targetCandidate, (p) => p.endsWith(".kt"));
  const androidOnly = files.filter((file) => {
    const source = fs.readFileSync(file, "utf8");
    return /(^import android\.|^import androidx\.activity|^import androidx\.core|\bLocalContext\b|\bIntent\b|\bActivity\b|\bContext\b|\bUri\b|Retrofit|okhttp3|retrofit2|com\.vortexa\.R)/m.test(
      source,
    );
  });
  const resourceNames = new Set();
  for (const file of files) {
    const source = fs.readFileSync(file, "utf8");
    for (const match of source.matchAll(/Res\.drawable\.([A-Za-z0-9_]+)/g)) {
      resourceNames.add(match[1]);
    }
  }
  const report = {
    sourceRoot,
    targetCandidate,
    copiedKotlinDirs: kotlinDirs,
    kotlinFileCount: files.length,
    androidOnlyFileCount: androidOnly.length,
    composeResourceReferenceCount: resourceNames.size,
    androidOnlySample: androidOnly.slice(0, 80).map((p) => path.relative(targetRoot, p)),
  };
  fs.writeFileSync(
    path.join(targetRoot, "composeApp/migrated/migration-report.json"),
    `${JSON.stringify(report, null, 2)}\n`,
  );
}

ensureDir(targetCandidate);
for (const dir of kotlinDirs) copyDir(path.join(sourceJava, dir), path.join(targetCandidate, dir));
copyResources();
for (const file of walk(targetCandidate, (p) => p.endsWith(".kt"))) rewriteResources(file);
writeReport();

console.log("Migration candidate refreshed.");
console.log(`Source: ${sourceRoot}`);
console.log(`Target candidate: ${targetCandidate}`);
