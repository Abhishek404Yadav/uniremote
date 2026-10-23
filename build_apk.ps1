$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$SdkDir = "$env:LOCALAPPDATA\Android\Sdk"
Write-Host ">>> Target Android SDK directory: $SdkDir"

if (-not (Test-Path $SdkDir)) {
    New-Item -ItemType Directory -Force -Path $SdkDir | Out-Null
}

$CmdlineToolsZip = "$env:TEMP\commandlinetools-win.zip"
$CmdlineToolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

$LatestDir = "$SdkDir\cmdline-tools\latest"

if (-not (Test-Path "$LatestDir\bin\sdkmanager.bat")) {
    Write-Host ">>> Downloading Android Command-Line Tools (~150MB)..."
    Invoke-WebRequest -Uri $CmdlineToolsUrl -OutFile $CmdlineToolsZip -UseBasicParsing

    Write-Host ">>> Extracting Command-Line Tools..."
    $ExtractTemp = "$env:TEMP\cmdline-tools-extract"
    if (Test-Path $ExtractTemp) { Remove-Item -Recurse -Force $ExtractTemp }
    Expand-Archive -Path $CmdlineToolsZip -DestinationPath $ExtractTemp -Force

    New-Item -ItemType Directory -Force -Path "$SdkDir\cmdline-tools" | Out-Null
    if (Test-Path $LatestDir) { Remove-Item -Recurse -Force $LatestDir }
    Move-Item -Path "$ExtractTemp\cmdline-tools" -Destination $LatestDir -Force

    Remove-Item -Force $CmdlineToolsZip -ErrorAction SilentlyContinue
    Remove-Item -Recurse -Force $ExtractTemp -ErrorAction SilentlyContinue
}

Write-Host ">>> Android Command-Line Tools ready at $LatestDir."

$SdkManager = "$LatestDir\bin\sdkmanager.bat"

Write-Host ">>> Writing Android SDK license hashes..."
$LicenseDir = "$SdkDir\licenses"
New-Item -ItemType Directory -Force -Path $LicenseDir | Out-Null

$SdkLicenseContent = @"
24333f8a63b6825ea9c5514f83c2829b004d1fee
d56f5187479451eabf01fb78af6dfcb131a6481e
84831b9409646a294ae7635694851dd2292f7e71
"@
Set-Content -Path "$LicenseDir\android-sdk-license" -Value $SdkLicenseContent -Encoding ASCII

$PreviewLicenseContent = @"
84831b9409646a294ae7635694851dd2292f7e71
"@
Set-Content -Path "$LicenseDir\android-sdk-preview-license" -Value $PreviewLicenseContent -Encoding ASCII

Write-Host ">>> Installing Android 34 Platform and Build-Tools..."
& "$SdkManager" --sdk_root="$SdkDir" "platforms;android-34" "build-tools;34.0.0"

# Write local.properties for Gradle
$LocalPropsPath = "$PSScriptRoot\local.properties"
$ForwardSlashSdkDir = $SdkDir -replace '\\', '/'
"sdk.dir=$ForwardSlashSdkDir" | Set-Content -Path $LocalPropsPath
Write-Host ">>> Created local.properties pointing to $SdkDir"

# Run Gradle build
Write-Host ">>> Compiling Sony Bravia Remote APK via Gradle..."
& "$PSScriptRoot\gradlew.bat" assembleDebug

$BuiltApk = "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"
$FinalApk = "$PSScriptRoot\Sony_Bravia_Remote.apk"

if (Test-Path $BuiltApk) {
    Copy-Item -Path $BuiltApk -Destination $FinalApk -Force
    Write-Host "`n========================================================"
    Write-Host "🎉 SUCCESS! APK compiled successfully:"
    Write-Host "File: $FinalApk"
    Write-Host "Size: $((Get-Item $FinalApk).Length / 1MB) MB"
    Write-Host "========================================================`n"
} else {
    Write-Error "APK file was not found at $BuiltApk"
}
