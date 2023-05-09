@ECHO off
SETLOCAL

SET CMD_HOME=%~dp0
SET DIR_LIB=%CMD_HOME%..\lib
SET DIR_PLUGINS=%CMD_HOME%..\plugins

SET CLASSMAIN=com.axway.yamles.utils.YamlEsUtils
SET CLASSPATH=%DIR_LIB%\*;%DIR_PLUGINS%\*

java %CLASSMAIN% %*
SET RC=%errorlevel%

ENDLOCAL & SET RC=%RC%

EXIT /B %RC%
