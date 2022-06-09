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
echo Lint the example API Manager project.
echo.
pause

set LINT_RULES_DIR="lint"

java -jar %JAR% lint --project=apim -r %LINT_RULES_DIR%\db.rules.yaml -r %LINT_RULES_DIR%\kps.rules.yaml

:EXIT

ENDLOCAL
