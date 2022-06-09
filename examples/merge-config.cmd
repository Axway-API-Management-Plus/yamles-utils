@ECHO off
SETLOCAL
SET CMD_HOME=%~dp0
CD /d "%CMD_HOME%"

for /r %%j in (..\target\yamles-utils-*-jar-with-dependencies.jar) do set JAR=%%j 

if not defined JAR (
  echo "No JAR found"
  goto EXIT
)

echo. 
echo Merge values from various sources and print the resuöt to the console.
echo.
pause


set CONFIG_DIR="merge\config"

java -jar %JAR% merge config -d %CONFIG_DIR%\common -d %CONFIG_DIR%\developers -d %CONFIG_DIR%\operators --output -

:EXIT

ENDLOCAL
