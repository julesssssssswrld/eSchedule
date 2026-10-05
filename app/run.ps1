$emu  = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
$adb  = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$avd  = "Pixel_8"
$pkg  = "com.example.eschedule"

# ── Helpers ───────────────────────────────────────────────────────────────────

function Get-OnlineDevice {
    # Returns the serial of the first fully-online ADB device, or $null.
    # Strict pattern: only lines that end with <TAB>device — never offline/unauthorized.
    & $adb devices 2>$null |
        Select-String "^(emulator-\d+|[A-Za-z0-9:._-]+)`tdevice$" |
        Select-Object -First 1 |
        ForEach-Object { $_.Matches[0].Groups[1].Value }
}

function Wait-ForOnline {
    param([int]$TimeoutSec = 120)
    Write-Host "  Waiting for device to come online..." -ForegroundColor Yellow
    $elapsed = 0
    while (-not (Get-OnlineDevice)) {
        if ($elapsed -ge $TimeoutSec) {
            Write-Host "  ERROR: Device did not come online within ${TimeoutSec}s." -ForegroundColor Red
            exit 1
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
}

function Wait-ForBoot {
    param([string]$Serial, [int]$TimeoutSec = 120)
    Write-Host "  Waiting for Android to finish booting..." -ForegroundColor Yellow
    $elapsed = 0
    while ((& $adb -s $Serial shell getprop sys.boot_completed 2>$null).Trim() -ne "1") {
        if ($elapsed -ge $TimeoutSec) {
            Write-Host "  ERROR: Boot did not complete within ${TimeoutSec}s." -ForegroundColor Red
            exit 1
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
}

# ── 1. Ensure emulator is running and online ──────────────────────────────────

$serial = Get-OnlineDevice

if ($serial) {
    Write-Host "Device already online: $serial" -ForegroundColor Green
} else {
    # Check if an emulator process is visible (offline / still booting)
    $emuLine = & $adb devices 2>$null | Select-String "emulator-"
    if (-not $emuLine) {
        Write-Host "Starting emulator ($avd)..." -ForegroundColor Cyan
        Start-Process $emu -ArgumentList "-avd $avd"
    } else {
        Write-Host "Emulator detected (offline/booting), waiting..." -ForegroundColor Yellow
    }

    Wait-ForOnline
    $serial = Get-OnlineDevice
    Wait-ForBoot -Serial $serial
    Write-Host "Device ready: $serial" -ForegroundColor Green
}

# ── 2. Build & install ────────────────────────────────────────────────────────

Write-Host ""
Write-Host "Building and installing..." -ForegroundColor Cyan
.\gradlew installDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build/install failed (exit $LASTEXITCODE)." -ForegroundColor Red
    exit $LASTEXITCODE
}

# ── 3. Launch ─────────────────────────────────────────────────────────────────

Write-Host "Launching $pkg..." -ForegroundColor Cyan
& $adb -s $serial shell monkey -p $pkg -c android.intent.category.LAUNCHER 1 | Out-Null
Write-Host "Done." -ForegroundColor Green