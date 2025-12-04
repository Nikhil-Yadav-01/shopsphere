@echo off
REM ShopSphere Build & Test Script for Windows
REM Builds individual services and performs local testing

setlocal enabledelayedexpansion

set "COLOR_GREEN=[92m"
set "COLOR_BLUE=[94m"
set "COLOR_YELLOW=[93m"
set "COLOR_RED=[91m"
set "NC=[0m"

REM Configuration
set "SERVICES=auth-service user-service catalog-service cart-service inventory-service order-service payment-service checkout-service notification-service shipping-service review-service fraud-service pricing-service media-service search-service recommendation-service admin-service batch-service analytics-service coupon-service returns-service"

set "INFRA_SERVICES=discovery config-server api-gateway"

REM Functions
:print_header
echo.
echo %COLOR_BLUE%================================================================================
echo %1
echo ================================================================================%NC%
exit /b 0

:print_success
echo %COLOR_GREEN%[OK] %1%NC%
exit /b 0

:print_error
echo %COLOR_RED%[ERROR] %1%NC%
exit /b 0

:print_warning
echo %COLOR_YELLOW%[WARNING] %1%NC%
exit /b 0

REM Check prerequisites
:check_prerequisites
call :print_header "Checking Prerequisites"

docker --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker is not installed"
    exit /b 1
)
for /f "tokens=*" %%i in ('docker --version') do call :print_success "Docker is installed: %%i"

docker-compose --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker Compose is not installed"
    exit /b 1
)
for /f "tokens=*" %%i in ('docker-compose --version') do call :print_success "Docker Compose is installed: %%i"

mvn --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Maven is not installed"
    exit /b 1
)
for /f "tokens=*" %%i in ('mvn --version') do (
    call :print_success "Maven is installed: %%i"
    goto :end_mvn_check
)
:end_mvn_check
exit /b 0

REM Local Maven build
:build_locally
call :print_header "Local Maven Build (Compilation Check)"

echo Building common modules...
mvn -B clean package -pl common-models,common-utils,common-security,common-kafka,common-db -am -DskipTests
if errorlevel 1 (
    call :print_error "Common modules build failed"
    exit /b 1
)
call :print_success "Common modules built"

REM Build each service
for %%S in (%SERVICES%) do (
    echo Building %%S...
    mvn -B clean package -pl services\%%S -am -DskipTests > temp\%%S-build.log 2>&1
    if errorlevel 1 (
        call :print_error "%%S build failed - see temp\%%S-build.log"
        exit /b 1
    )
    call :print_success "%%S built successfully"
)

REM Build infrastructure services
for %%S in (%INFRA_SERVICES%) do (
    echo Building %%S...
    mvn -B clean package -pl services\%%S -am -DskipTests > temp\%%S-build.log 2>&1
    if errorlevel 1 (
        call :print_error "%%S build failed - see temp\%%S-build.log"
        exit /b 1
    )
    call :print_success "%%S built successfully"
)

exit /b 0

REM Docker build
:docker_build
call :print_header "Building Docker Images"

REM Build infrastructure services first
for %%S in (%INFRA_SERVICES%) do (
    echo Building Docker image for %%S...
    docker build -f services\%%S\Dockerfile -t shopsphere-%%S:latest . > temp\%%S-docker.log 2>&1
    if errorlevel 1 (
        call :print_error "Docker image build failed for %%S - see temp\%%S-docker.log"
        exit /b 1
    )
    call :print_success "Docker image built: shopsphere-%%S:latest"
)

REM Build microservices
for %%S in (%SERVICES%) do (
    echo Building Docker image for %%S...
    docker build -f services\%%S\Dockerfile -t shopsphere-%%S:latest . > temp\%%S-docker.log 2>&1
    if errorlevel 1 (
        call :print_error "Docker image build failed for %%S - see temp\%%S-docker.log"
        exit /b 1
    )
    call :print_success "Docker image built: shopsphere-%%S:latest"
)

exit /b 0

REM Start Docker Compose
:docker_start
call :print_header "Starting Docker Compose Stack"

docker-compose -f docker-compose-full.yml up -d
if errorlevel 1 (
    call :print_error "Failed to start Docker Compose"
    exit /b 1
)

echo Waiting for services to be healthy...
timeout /t 10 /nobreak

call :print_success "Docker Compose stack started"
echo Checking service health...
docker-compose -f docker-compose-full.yml ps

exit /b 0

REM Health checks
:health_checks
call :print_header "Performing Health Checks"

echo Checking Eureka Service Discovery...
powershell -Command "try { $null = (curl -f http://localhost:8761/eureka/status -ErrorAction Stop); write-host '(OK)' } catch { write-host '(Not responding yet)' }"

echo Checking Auth Service...
powershell -Command "try { $null = (curl -f http://localhost:8001/actuator/health -ErrorAction Stop); write-host '(OK)' } catch { write-host '(Not responding yet)' }"

echo Checking Order Service...
powershell -Command "try { $null = (curl -f http://localhost:8005/actuator/health -ErrorAction Stop); write-host '(OK)' } catch { write-host '(Not responding yet)' }"

echo Checking API Gateway...
powershell -Command "try { $null = (curl -f http://localhost:8080/actuator/health -ErrorAction Stop); write-host '(OK)' } catch { write-host '(Not responding yet)' }"

exit /b 0

REM Cleanup
:cleanup
call :print_header "Cleanup & Shutdown"

set /p CONFIRM="Do you want to stop and remove all containers? (y/n) "
if /i "%CONFIRM%"=="y" (
    docker-compose -f docker-compose-full.yml down
    call :print_success "All containers stopped and removed"
) else (
    call :print_warning "Containers still running. Use 'docker-compose -f docker-compose-full.yml down' to stop"
)

exit /b 0

REM Main
:main
call :print_header "ShopSphere Build & Test Suite"

call :check_prerequisites
if errorlevel 1 exit /b 1

if "%1"=="local" (
    call :build_locally
    exit /b !errorlevel!
) else if "%1"=="docker" (
    call :docker_build
    exit /b !errorlevel!
) else if "%1"=="start" (
    call :docker_start
    if errorlevel 1 exit /b 1
    timeout /t 10 /nobreak
    call :health_checks
    exit /b 0
) else if "%1"=="health" (
    call :health_checks
    exit /b 0
) else if "%1"=="stop" (
    call :cleanup
    exit /b 0
) else if "%1"=="full" (
    call :build_locally
    if errorlevel 1 exit /b 1
    call :docker_build
    if errorlevel 1 exit /b 1
    call :docker_start
    if errorlevel 1 exit /b 1
    timeout /t 20 /nobreak
    call :health_checks
    exit /b 0
) else (
    echo.
    echo %COLOR_YELLOW%Usage:%NC%
    echo   build-and-test.bat local       - Local Maven build only
    echo   build-and-test.bat docker      - Build Docker images
    echo   build-and-test.bat start       - Start Docker Compose stack
    echo   build-and-test.bat health      - Run health checks
    echo   build-and-test.bat stop        - Stop containers
    echo   build-and-test.bat full        - Full build, Docker, and startup
    echo.
    echo %COLOR_YELLOW%Examples:%NC%
    echo   REM Full workflow: build locally ^> Docker images ^> start services
    echo   build-and-test.bat full
    echo.
    echo   REM Build only (no Docker^)
    echo   build-and-test.bat local
    echo.
    echo   REM Start services (assumes images exist^)
    echo   build-and-test.bat start
    exit /b 0
)

:end
endlocal
