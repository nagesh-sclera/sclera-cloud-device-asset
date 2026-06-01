# start-local.ps1
# Starts all 3 Sclara services locally with Dapr sidecars using your existing MySQL on localhost:3306
#
# Prerequisites:
#   - Java 17 in PATH  (java -version)
#   - Maven in PATH    (mvn -version)
#   - Docker running   (for Redis)
#   - dapr.exe present in this directory
#
# MySQL credentials — edit these if your local MySQL differs:
$DbUrl  = "jdbc:mysql://localhost:3306/vdms"
$DbUser = "root"
$DbPass = "mypass123"

$ErrorActionPreference = "Stop"
$Root  = $PSScriptRoot
$Comps = "$Root\dapr\components\local"

# Resolve dapr CLI: prefer PATH, fall back to local dapr.exe
$DaprCmd = Get-Command dapr -ErrorAction SilentlyContinue
$Dapr = if ($DaprCmd) { "dapr" } elseif (Test-Path "$Root\dapr.exe") { "$Root\dapr.exe" } else { $null }

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Sclara 2.0 — Local Startup with Dapr" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Prerequisites ─────────────────────────────────────────────────────────
Write-Host "[1/6] Checking prerequisites..." -ForegroundColor Yellow

if (-not $Dapr) {
    Write-Host ""
    Write-Host "  [ERROR] Dapr CLI not found. Install it with:" -ForegroundColor Red
    Write-Host "    winget install Dapr.CLI" -ForegroundColor Yellow
    Write-Host "  Then re-run this script." -ForegroundColor Yellow
    exit 1
}
Write-Host "  [OK] dapr CLI: $Dapr"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "java not found in PATH"
    exit 1
}
Write-Host "  [OK] java"

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "mvn not found in PATH"
    exit 1
}
Write-Host "  [OK] mvn"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "docker not found in PATH (needed to run Redis)"
    exit 1
}
Write-Host "  [OK] docker"

# ── 2. Redis ─────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[2/6] Starting Redis on localhost:6379..." -ForegroundColor Yellow

$running = docker ps --filter "name=sclera-redis" --format "{{.Names}}" 2>$null
if ($running -eq "sclera-redis") {
    Write-Host "  [OK] Redis already running"
} else {
    docker rm sclera-redis -f 2>$null | Out-Null
    docker run -d --name sclera-redis -p 6379:6379 redis:7-alpine | Out-Null
    Write-Host "  [OK] Redis started"
    Start-Sleep -Seconds 2
}

# ── 3. Dapr runtime ──────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[3/6] Checking Dapr runtime..." -ForegroundColor Yellow

$daprd = "$env:USERPROFILE\.dapr\bin\daprd.exe"
if (-not (Test-Path $daprd)) {
    Write-Host "  Dapr runtime not installed. Running: dapr init --slim"
    Write-Host "  (--slim skips Docker dependencies — we manage Redis ourselves)"
    & $Dapr init --slim
    Write-Host "  [OK] Dapr initialized"
} else {
    Write-Host "  [OK] Dapr runtime found at $daprd"
}

Write-Host "  Components path: $Comps"

# ── 4. Build ─────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[4/6] Building services (tests skipped)..." -ForegroundColor Yellow

Push-Location "$Root\sclera-vdms-service"
Write-Host "  Building sclera-vdms-service..."
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) { Pop-Location; Write-Error "vdms-service build failed"; exit 1 }
$VdmsJar = (Get-Item "target\*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1).FullName
Pop-Location
Write-Host "  [OK] $VdmsJar"

Push-Location "$Root\sclera-cloud-device-asset"
Write-Host "  Building sclera-cloud-device-asset..."
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) { Pop-Location; Write-Error "device-asset build failed"; exit 1 }
$AssetJar = (Get-Item "target\*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1).FullName
Pop-Location
Write-Host "  [OK] $AssetJar"

Push-Location "$Root\sclera-api-gateway"
Write-Host "  Building sclera-api-gateway..."
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) { Pop-Location; Write-Error "api-gateway build failed"; exit 1 }
$GwJar = (Get-Item "target\*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1).FullName
Pop-Location
Write-Host "  [OK] $GwJar"

# ── 5. Launch services in separate windows ───────────────────────────────────
Write-Host ""
Write-Host "[5/6] Launching services (each opens in its own window)..." -ForegroundColor Yellow

# vdms-service  — Dapr HTTP 3501 (separate port from device-asset)
$vdmsArgs = @(
    "& '$Dapr' run",
    "--app-id vdms-service",
    "--app-port 8089",
    "--dapr-http-port 3501",
    "--dapr-grpc-port 50002",
    "--components-path '$Comps'",
    "--",
    "java -jar '$VdmsJar'",
    "--spring.profiles.active=local",
    "--spring.datasource.url='$DbUrl'",
    "--spring.datasource.username='$DbUser'",
    "--spring.datasource.password='$DbPass'"
) -join " "
Start-Process powershell -ArgumentList "-NoExit", "-Command", $vdmsArgs -WindowStyle Normal
Write-Host "  [STARTED] vdms-service        (port 8089, Dapr HTTP 3501)"

Start-Sleep -Seconds 4

# device-asset  — Dapr HTTP 3500 (default, VdmsClient reads this)
$assetArgs = @(
    "& '$Dapr' run",
    "--app-id sclera-cloud-device-asset",
    "--app-port 8085",
    "--dapr-http-port 3500",
    "--dapr-grpc-port 50001",
    "--components-path '$Comps'",
    "--",
    "java -jar '$AssetJar'",
    "--spring.profiles.active=local",
    "--spring.datasource.url='$DbUrl'",
    "--spring.datasource.username='$DbUser'",
    "--spring.datasource.password='$DbPass'"
) -join " "
Start-Process powershell -ArgumentList "-NoExit", "-Command", $assetArgs -WindowStyle Normal
Write-Host "  [STARTED] device-asset        (port 8085, Dapr HTTP 3500)"

Start-Sleep -Seconds 4

# api-gateway  — no Dapr, local profile routes to localhost
$gwArgs = "java -jar '$GwJar' --spring.profiles.active=local"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $gwArgs -WindowStyle Normal
Write-Host "  [STARTED] api-gateway         (port 8080)"

# ── 6. Summary ───────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[6/6] All services launched!" -ForegroundColor Green
Write-Host ""
Write-Host "  URLs:" -ForegroundColor Cyan
Write-Host "    http://localhost:8080          <- API Gateway (use this for testing)"
Write-Host "    http://localhost:8085          <- device-asset (direct)"
Write-Host "    http://localhost:8089          <- vdms-service  (direct)"
Write-Host ""
Write-Host "  Dapr sidecar logs to watch:" -ForegroundColor Cyan
Write-Host "    [Dapr sidecar ->] invoke ...  <- device-asset calling vdms-service via Dapr"
Write-Host "    [Dapr sidecar ->] publish ... <- device-asset publishing to Redis topic"
Write-Host "    [Audit] Logged device.*       <- vdms-service received and persisted event"
Write-Host ""
Write-Host "  Test UI:" -ForegroundColor Cyan
Write-Host "    Open test-ui\index.html in your browser"
Write-Host ""
Write-Host "  Allow ~30s for Spring Boot services to start before testing." -ForegroundColor Yellow
Write-Host "  Watch for 'Started ... in X seconds' in each terminal window."
Write-Host ""
