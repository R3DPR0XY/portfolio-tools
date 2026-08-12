# Release Rail

Use for CI/CD, GitHub Actions, Maven/Gradle builds, artifact packaging, and GitHub Releases.

## Inspect

- `.github/workflows/*.yml`
- trigger events
- runner OS
- Java setup
- Gradle/Maven commands
- matrix entries
- artifact upload and release actions

## Layout

Use a horizontal rail with five slots:

1. `TRIGGER`
2. `RUNNER`
3. `BUILD`
4. `PACKAGE`
5. `PUBLIC OUTPUT`

The rail itself is the main shape. Place the red `signal` stroke through the critical path. Optional/manual routes are thin dashed steel lines above or below the rail.

## Labels

Use concrete public labels:

- `Push / PR`
- `Release published`
- `Ubuntu runner`
- `Java 21`
- `Gradle matrix`
- `Maven package`
- `JAR bundle`
- `GitHub Release`

## Finish

Add a small footer strip with:

- workflow names
- number of build targets
- artifact type
- release permission if relevant
