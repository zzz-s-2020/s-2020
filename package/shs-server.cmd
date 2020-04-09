@echo off

rem  ----------------------------------------------------------------------------
rem SHS server
rem
rem Required environment variables:
rem JAVA_HOME - location of a Java installation directory.
rem  ----------------------------------------------------------------------------

setlocal

set JAVA_EXE=java.exe
set SHS_MAIN="ru.zzz.demo.sber.shs.server.SpringApp"
set SCRIPT_DIR=%~dp0

setlocal EnableDelayedExpansion

if defined JAVA_HOME (
    set JAVA_EXE_FULL_PATH="!JAVA_HOME!\bin\!JAVA_EXE!"
) else (
    for %%X in (%JAVA_EXE%) do (set FOUND_JAVA=%%~$PATH:X)
	echo "FOUND_JAVA=!FOUND_JAVA!"
    if defined FOUND_JAVA (
        set JAVA_EXE_FULL_PATH="!FOUND_JAVA!"
    ) else (
        echo.
        echo Error: JAVA_HOME environment variable is not set and Java is not found in PATH.
        echo Please set the JAVA_HOME environment variable to the location of your Java installation.
        echo.
        goto error
    )
)

setlocal DisableDelayedExpansion

set CLASSPATH="%SCRIPT_DIR%\lib\*"

%JAVA_EXE_FULL_PATH% -cp %CLASSPATH% %SHS_MAIN% %*
set ERROR_CODE=%ERRORLEVEL%
goto end

:error
rem -- If error occurred - place a flag
set ERROR_CODE=1
goto end

rem Exit
:end
if %ERRORLEVEL% neq 0 (
    if %ERROR_CODE% neq 0 (
        set ERROR_CODE=ERRORLEVEL
    )
)
cmd /C exit /B %ERROR_CODE%
