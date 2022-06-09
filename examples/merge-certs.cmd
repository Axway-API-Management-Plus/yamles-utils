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
echo Merge certificates into the API Manager project.
echo.
echo It will generate three certificates in the API Manager project. Check the
echo 'Environment Configuration\Certificate Store' folder of the example project.
echo.
pause

set CERTS_DIR="merge\certs"
set PASSPHRASE=changeme



java -jar %JAR% merge certs --project=apim --config=%CERTS_DIR%\certificates.yaml --lookup-yaml=%CERTS_DIR%\lookup-values.yaml

:EXIT

ENDLOCAL
