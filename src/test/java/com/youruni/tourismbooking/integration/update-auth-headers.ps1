# PowerShell script to add Authorization header to all mockMvc.perform calls in test files
# This script handles JWT token injection for Spring Boot MockMvc tests

param(
    [string]$FilePath
)

if (-not (Test-Path $FilePath)) {
    Write-Error "File not found: $FilePath"
    exit 1
}

$content = Get-Content $FilePath -Raw

# Pattern 1: mockMvc.perform(post(...).contentType(...).content(...))
# Add header before .content(
$pattern1 = '(mockMvc\.perform\([a-z]*\([^)]+\)\s*\.contentType[^\)]*\))\s*\.'
$replacement1 = '$1.header("Authorization", getBearerTokenHeader()).''

$content = [regex]::Replace($content, $pattern1, $replacement1)

# Pattern 2: mockMvc.perform(get(...)) without contentType
# These are simpler - just need to add header after the initial perform
$pattern2 = '(mockMvc\.perform\((?:get|delete)\([^)]+\))\s*(?=\))'
$replacement2 = '$1.header("Authorization", getBearerTokenHeader())'

$content = [regex]::Replace($content, $pattern2, $replacement2)

# Write back
Set-Content -Path $FilePath -Value $content -Encoding UTF8
Write-Host "Updated $FilePath"
