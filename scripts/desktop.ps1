param([switch]$Verify, [switch]$UpdatePin, [string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$classes = Join-Path $root 'build/preview-classes'
$library = Join-Path $root 'samples/desktop/lib'
& (Join-Path $PSScriptRoot 'compile.ps1') -Sources (Join-Path $root 'samples/desktop/java') -Classes $classes -Archive (Join-Path $library 'preview.jar') -JavaHome $JavaHome
& (Join-Path $PSScriptRoot 'norm.ps1') package (Join-Path $root 'theme') --output (Join-Path $root '.norm-home/.norm/cache/packages')
if ($LASTEXITCODE -ne 0) { throw 'Theme packaging failed' }
if ($UpdatePin) {
    $module = Join-Path $root 'samples/desktop/module.norm'
    $body = [regex]::Replace((Get-Content -Raw $module), ', integrity: sha256\("[a-f0-9]+"\)', '')
    Set-Content -LiteralPath $module -Value $body -NoNewline
}
& (Join-Path $PSScriptRoot 'norm.ps1') resolve (Join-Path $root 'samples/desktop')
if ($LASTEXITCODE -ne 0) { throw 'Preview resolution failed' }
Push-Location $root
try {
    if ($Verify) {
        & (Join-Path $PSScriptRoot 'norm.ps1') test (Join-Path $root 'samples/desktop') --filter desktop.switchingPreservesLiveControls --format json
    } else {
        & (Join-Path $PSScriptRoot 'norm.ps1') run (Join-Path $root 'samples/desktop')
    }
    $result = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $result
