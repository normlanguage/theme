param(
    [Parameter(Mandatory)][string]$Sources,
    [Parameter(Mandatory)][string]$Classes,
    [Parameter(Mandatory)][string]$Archive,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$target = [IO.Path]::GetFullPath($Classes)
$buildRoot = [IO.Path]::GetFullPath((Join-Path $root 'build')) + [IO.Path]::DirectorySeparatorChar
if (!$target.StartsWith($buildRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Class directory is outside build' }
if (Test-Path -LiteralPath $target) {
    $resolved = (Resolve-Path -LiteralPath $target).Path
    if (!$resolved.StartsWith($buildRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Resolved class directory is outside build' }
    Remove-Item -LiteralPath $resolved -Recurse -Force
}
New-Item -ItemType Directory -Force $target, (Split-Path $Archive -Parent) | Out-Null
$javac = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$jar = if ($JavaHome) { Join-Path $JavaHome 'bin/jar.exe' } else { 'jar' }
$compilerVersion = & $javac -version
if ($LASTEXITCODE -ne 0 -or $compilerVersion -notmatch '^javac 21\.') {
    throw 'Reproducible artifacts require JDK 21; pass -JavaHome with its installation directory'
}
$files = Get-ChildItem -LiteralPath $Sources -Filter '*.java' -Recurse | ForEach-Object FullName
& $javac --release 21 -encoding UTF-8 -d $target $files
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
& $jar --create --no-manifest --file $Archive --date=2026-01-01T00:00:00Z -C $target .
if ($LASTEXITCODE -ne 0) { throw 'Java packaging failed' }
