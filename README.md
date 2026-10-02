# theme

[简体中文](README.zh-CN.md)

An independent Norm module for semantic colors and scoped theme lifetimes. One required seed color generates light and dark surfaces, foregrounds, outlines, and control states. UI libraries consume `ThemeSource`; applications control `ThemeManager`.

The core has no UI framework dependencies. It uses the Norm standard library and this repository's JDK-only color kernel. Swing appears only in the separate desktop sample. Scheduling, rendering, animation, operating-system preferences, and persistence belong to consumers.

## API

```norm
var themes = ThemeManager(
  definition: ThemeDefinition(primary: Color(hex: "#B2DBEB"))
)
var sidebar = themes.scope(ThemeOverride(mode: ThemeMode.Dark))
var subscription = themes.watch((Theme theme) {
  var colors = theme.control(tone: Tone.Primary, appearance: Appearance.Filled)
  printLine(colors.normal.background.hex())
})
themes.setMode(ThemeMode.Dark)
themes.close()
```

Complete executable usage and imports: [basic sample](samples/basic/application.norm). Host integration rules: [adapter contract](docs/adapters.md).

| API | Authoritative source |
| --- | --- |
| Colors and state pairs | [color.norm](theme/color.norm) |
| Seeds, overrides, roles, snapshots, generation | [palette.norm](theme/palette.norm) |
| Sources, manager, scopes, subscriptions | [lifecycle.norm](theme/lifecycle.norm) |
| sRGB, Oklab/OKLCH chroma scaling and gamut mapping | [ColorMath.java](src/main/java/dev/normlanguage/theme/ColorMath.java) |
| Module identity and dependencies | [module.norm](theme/module.norm) |
| Acceptance cases | [color tests](theme/tests/color_test.norm), [lifecycle tests](theme/tests/lifecycle_test.norm) |

## Development

Requires Windows PowerShell 7, Norm 0.25.4 and JDK 21. Run from the repository root:

Set `JAVA_HOME` to JDK 21, or pass `-JavaHome` to the build and desktop scripts. Java compiler versions can produce different bytecode even with the same `--release` target; the build requires JDK 21 to preserve artifact digests.

```powershell
./scripts/build.ps1
./scripts/norm.ps1 check theme
./scripts/norm.ps1 test theme --filter theme.scopesInheritAndPublishAtomically
./scripts/norm.ps1 package theme --output build/repository
./scripts/norm.ps1 package theme --output .norm-home/.norm/cache/packages
./scripts/norm.ps1 run samples/basic
./scripts/desktop.ps1 -Verify
```

`scripts/norm.ps1` isolates candidate packages in `.norm-home`; it does not install into the user's normal package cache. After intentionally changing the color kernel, rebuild with `./scripts/build.ps1 -UpdatePin` and review the resulting module digest.

The local Maven repository in `build/repository` contains the kernel JAR/POM and the packaged Norm module. Publish the packaged NAR and SHA-256 sidecar through GitHub Releases. Norm 0.26.1 and newer include and verify the kernel dependency in that package; consumers declare `dependency(repository: "github", name: "theme", version: 1)`. Building a candidate does not publish anything.

[Desktop sample](samples/README.md) opens an interactive preview without verification mode.
