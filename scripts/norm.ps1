$root = Split-Path $PSScriptRoot -Parent
$previousOptions = $env:JAVA_TOOL_OPTIONS
try {
    $developmentHome = Join-Path $root '.norm-home'
    $env:JAVA_TOOL_OPTIONS = "$previousOptions -Duser.home=`"$developmentHome`""
    & norm @args
    $result = $LASTEXITCODE
} finally {
    $env:JAVA_TOOL_OPTIONS = $previousOptions
}
exit $result
