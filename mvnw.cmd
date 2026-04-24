@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "WRAPPER_PROPS=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties"
set "MVN_CMD=mvn"

where %MVN_CMD% >nul 2>nul
if %ERRORLEVEL%==0 (
    %MVN_CMD% %*
    exit /b %ERRORLEVEL%
)

if not exist "%WRAPPER_PROPS%" (
    echo Maven wrapper properties file not found: %WRAPPER_PROPS%
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
    if "%%A"=="distributionUrl" set "DISTRIBUTION_URL=%%B"
    if "%%A"=="distributionPath" set "DISTRIBUTION_PATH=%%B"
)

if not defined DISTRIBUTION_URL (
    echo distributionUrl is missing in %WRAPPER_PROPS%
    exit /b 1
)

if not defined DISTRIBUTION_PATH (
    set "DISTRIBUTION_PATH=.mvn\apache-maven"
)

set "MAVEN_HOME=%SCRIPT_DIR%%DISTRIBUTION_PATH%"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_ZIP=%SCRIPT_DIR%.mvn\wrapper\apache-maven-bin.zip"

if exist "%MAVEN_BIN%" (
    call "%MAVEN_BIN%" %*
    exit /b %ERRORLEVEL%
)

echo Local Maven distribution not found. Downloading Maven...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'Stop';" ^
    "$zipPath = '%MAVEN_ZIP%';" ^
    "$extractPath = '%MAVEN_HOME%';" ^
    "$url = '%DISTRIBUTION_URL%';" ^
    "Invoke-WebRequest -Uri $url -OutFile $zipPath;" ^
    "if (Test-Path $extractPath) { Remove-Item -Recurse -Force $extractPath };" ^
    "New-Item -ItemType Directory -Force -Path $extractPath | Out-Null;" ^
    "Expand-Archive -LiteralPath $zipPath -DestinationPath $extractPath -Force;" ^
    "$inner = Get-ChildItem -Path $extractPath | Select-Object -First 1;" ^
    "if ($inner -and $inner.PSIsContainer) {" ^
    "  Get-ChildItem -Path $inner.FullName -Force | ForEach-Object { Move-Item -Force $_.FullName $extractPath };" ^
    "  Remove-Item -Recurse -Force $inner.FullName;" ^
    "}" ^
    "Remove-Item -Force $zipPath"

if not exist "%MAVEN_BIN%" (
    echo Maven download completed but mvn.cmd was not found.
    exit /b 1
)

call "%MAVEN_BIN%" %*
exit /b %ERRORLEVEL%
