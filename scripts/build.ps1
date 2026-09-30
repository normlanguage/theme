param([string]$JavaHome = $env:JAVA_HOME, [switch]$UpdatePin)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$classes = Join-Path $root 'build/classes'
$modulePath = Join-Path $root 'theme/module.norm'
$module = Get-Content -Raw $modulePath
$coordinate = [regex]::Match($module, 'mavenJar\(group: "([^"]+)", artifact: "([^"]+)", version: "([^"]+)"')
if (!$coordinate.Success) { throw 'Missing color kernel Maven coordinate in module.norm' }
$group = $coordinate.Groups[1].Value
$artifact = $coordinate.Groups[2].Value
$version = $coordinate.Groups[3].Value
$coordinatePath = $group.Replace('.', '/') + '/' + $artifact + '/' + $version
$library = Join-Path $root ('build/repository/' + $coordinatePath)
$cache = Join-Path $root ('.norm-home/.norm/cache/maven/' + $coordinatePath)
$artifactPath = Join-Path $library "$artifact-$version.jar"
& (Join-Path $PSScriptRoot 'compile.ps1') -Sources (Join-Path $root 'src/main/java') -Classes $classes -Archive $artifactPath -JavaHome $JavaHome
$pom = '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>' + $group + '</groupId><artifactId>' + $artifact + '</artifactId><version>' + $version + '</version></project>'
Set-Content -LiteralPath (Join-Path $library "$artifact-$version.pom") -Value $pom -NoNewline
New-Item -ItemType Directory -Force $cache | Out-Null
Copy-Item -LiteralPath $artifactPath -Destination $cache
Copy-Item -LiteralPath (Join-Path $library "$artifact-$version.pom") -Destination $cache
foreach ($file in Get-ChildItem $library -File | Where-Object Extension -In '.jar', '.pom') {
    Set-Content -LiteralPath ($file.FullName + '.sha256') -Value (Get-FileHash $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant() -NoNewline
}
if ($UpdatePin) {
    $module = [regex]::Replace($module, ', resolution: sha256\("[a-f0-9]+"\)', '')
    Set-Content -LiteralPath $modulePath -Value $module -NoNewline
}
& (Join-Path $PSScriptRoot 'norm.ps1') resolve (Join-Path $root 'theme')
if ($LASTEXITCODE -ne 0) { throw 'Module resolution failed; use -UpdatePin after changing the kernel' }
