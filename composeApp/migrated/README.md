# Vortexa Android Migration Candidate

This directory keeps the mechanically migrated Android source as reusable migration material.
It is intentionally outside Gradle source sets so iOS compilation is not blocked by Android-only APIs while core pages are migrated in small closures.

- `android-source-candidate/com/vortexa`: copied Android Kotlin source with resource references rewritten from `R.drawable/mipmap.*` to `Res.drawable.*`.
- `migration-report.json`: generated inventory of copied files, Android-only blockers, and resource references.
- `template-ios`: original Compose Multiplatform template files kept for reference.

Refresh the candidate tree with:

```bash
node tools/migration/migrate-vortexa-android.js
```

The first compiling iOS shell lives in `composeApp/src/commonMain/kotlin/com/vortexa`. Pull pages from this candidate tree into `commonMain` one closure at a time, replacing shell screens without reimplementing visual design.
