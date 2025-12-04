# Check which services are covered by workflows
Write-Host "=== Checking Workflow Coverage ===" -ForegroundColor Cyan

$workflows = @(
    "build-commons.yml",
    "build-infrastructure.yml",
    "build-core-services.yml",
    "build-data-services.yml",
    "build-commerce-services.yml",
    "build-support-services.yml",
    "build-complete.yml",
    "ci.yml"
)

$allServices = @(
    "discovery",
    "config-server",
    "api-gateway",
    "auth-service",
    "user-service",
    "catalog-service",
    "inventory-service",
    "pricing-service",
    "cart-service",
    "checkout-service",
    "order-service",
    "payment-service",
    "shipping-service",
    "notification-service",
    "review-service",
    "recommendation-service",
    "search-service",
    "media-service",
    "coupon-service",
    "returns-service",
    "fraud-service",
    "analytics-service",
    "batch-service",
    "admin-service",
    "websocket-chat"
)

$coveredServices = @()

foreach ($workflow in $workflows) {
    $path = ".github/workflows/$workflow"
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        foreach ($service in $allServices) {
            if ($content -match "services/$service") {
                if ($coveredServices -notcontains $service) {
                    $coveredServices += $service
                }
            }
        }
    }
}

Write-Host "`n✅ Services covered in workflows:" -ForegroundColor Green
$coveredServices | Sort-Object | ForEach-Object { Write-Host "  - $_" }

$uncovered = $allServices | Where-Object { $coveredServices -notcontains $_ }
if ($uncovered.Count -gt 0) {
    Write-Host "`n❌ Services NOT covered in workflows:" -ForegroundColor Red
    $uncovered | ForEach-Object { Write-Host "  - $_" }
} else {
    Write-Host "`n✅ All services are covered!" -ForegroundColor Green
}

Write-Host "`nTotal: $($coveredServices.Count)/$($allServices.Count) services covered" -ForegroundColor Cyan
