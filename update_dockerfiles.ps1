# Update all Dockerfiles to use MACHINE_IP environment variable
$dockerfiles = Get-ChildItem -Path "f:\AppStorage\shopsphere\services" -Filter "Dockerfile" -Recurse

foreach ($file in $dockerfiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Skip if already updated
    if ($content -contains "ARG MACHINE_IP") {
        Write-Host "Already updated: $($file.FullName)"
        continue
    }
    
    # Replace localhost references with ${MACHINE_IP}
    $updated = $content -replace 'http://localhost:', 'http://${MACHINE_IP}:'
    
    # Add ARG MACHINE_IP line after ARG JAR_FILE if not present
    $updated = $updated -replace '(ARG JAR_FILE=.*\n)', "`$1ARG MACHINE_IP=127.0.0.1`n"
    
    Set-Content -Path $file.FullName -Value $updated
    Write-Host "Updated: $($file.FullName)"
}

Write-Host "All Dockerfiles updated successfully!"
