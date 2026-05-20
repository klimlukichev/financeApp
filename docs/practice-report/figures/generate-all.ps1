$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Get-ChildItem -Filter "*.mmd" | Sort-Object Name | ForEach-Object {
    $out = [System.IO.Path]::ChangeExtension($_.Name, ".png")
    Write-Host "Generating $out ..."
    npx --yes -p @mermaid-js/mermaid-cli@11 mmdc `
        -i $_.FullName `
        -o (Join-Path $PWD $out) `
        -b white `
        -w 1400 `
        -H 900
    if ($LASTEXITCODE -ne 0) { throw "Failed: $($_.Name)" }
}

Write-Host "Done. PNG files are in $PWD"
