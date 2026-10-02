# Minecraft 26.3 Fabric development port

Use JDK 25 and Fabric Loader 0.19.5 or newer.

```sh
./gradlew build collectRuntimeMods
./gradlew runArtifactSmoke
```

The mod JAR is in `build/libs`; installable dependencies are collected in
`build/runtime-mods`: Fabric API 0.161.0+26.3, YACL 3.9.7+26.3-fabric, Fabric
Language Kotlin 1.14.1+kotlin.2.4.20, and Mod Menu 21.0.0. Mod Menu is recommended
for opening settings. Avoid installing duplicate dependency versions.

The artifact smoke task exercises the packaged JAR and dependencies in a
separate disposable development instance. Smoke-test sources are excluded
from the production JAR. The port passed build and 20 packaged-client smoke
assertions; rendering screenshots were inspected. Existing configuration keys
and profiles are preserved. Multiplayer and compatibility with other rendering
mods remain unverified; this is a development build.

The port also fixes previously ignored block rainbow/fill-color settings and
uses a projectile owner's team color when no team gradient is configured,
avoiding a null gradient color.
