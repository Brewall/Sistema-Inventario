@echo off
REM Script de ayuda para probar el Sistema de Inventario
REM Ejecutar como: test.bat

setlocal enabledelayedexpansion

echo.
echo ============================================
echo  Sistema de Inventario API - Testing Helper
echo ============================================
echo.

:menu
echo.
echo Selecciona una opcion:
echo 1 - Compilar y ejecutar aplicacion (bootRun)
echo 2 - Compilar y generar JAR (build)
echo 3 - Ejecutar tests unitarios (test)
echo 4 - Ejecutar JAR compilado
echo 5 - Ver logs en tiempo real
echo 6 - Limpiar build (clean)
echo 7 - Abrir Swagger UI en navegador
echo 8 - Abrir H2 Console en navegador
echo 9 - Salir
echo.

set /p opcion="Ingresa tu opcion (1-9): "

if "%opcion%"=="1" goto bootRun
if "%opcion%"=="2" goto build
if "%opcion%"=="3" goto test
if "%opcion%"=="4" goto executeJar
if "%opcion%"=="5" goto logs
if "%opcion%"=="6" goto clean
if "%opcion%"=="7" goto swagger
if "%opcion%"=="8" goto h2
if "%opcion%"=="9" goto end

echo Opcion invalida!
goto menu

:bootRun
echo.
echo ========================================
echo Ejecutando: gradlew.bat bootRun
echo ========================================
echo.
echo Esperando... La app estara disponible en:
echo   http://localhost:8080/swagger-ui.html
echo.
call gradlew.bat bootRun
pause
goto menu

:build
echo.
echo ========================================
echo Compilando proyecto...
echo ========================================
echo.
call gradlew.bat build
echo.
echo Build completado!
echo JAR disponible en: build\libs\sistema-inventario-1.0.0-SNAPSHOT.jar
pause
goto menu

:test
echo.
echo ========================================
echo Ejecutando tests...
echo ========================================
echo.
call gradlew.bat test
pause
goto menu

:executeJar
echo.
echo ========================================
echo Buscando JAR compilado...
echo ========================================
echo.
if exist build\libs\sistema-inventario-1.0.0-SNAPSHOT.jar (
    echo Ejecutando JAR...
    java -jar build\libs\sistema-inventario-1.0.0-SNAPSHOT.jar
) else (
    echo JAR no encontrado. Debes ejecutar la opcion 2 primero (build)
    pause
)
goto menu

:logs
echo.
echo ========================================
echo Ejecutando con logs detallados...
echo ========================================
echo.
set SPRING_PROFILES_ACTIVE=dev
call gradlew.bat bootRun --args='--logging.level.root=DEBUG'
pause
goto menu

:clean
echo.
echo ========================================
echo Limpiando build anterior...
echo ========================================
echo.
call gradlew.bat clean
echo Clean completado!
pause
goto menu

:swagger
echo.
echo Abriendo Swagger UI en navegador...
echo.
start http://localhost:8080/swagger-ui.html
timeout /t 2
goto menu

:h2
echo.
echo Abriendo H2 Console en navegador...
echo.
start http://localhost:8080/h2-console
timeout /t 2
goto menu

:end
echo.
echo Bye!
echo.
endlocal
exit /b 0
