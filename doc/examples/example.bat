@echo off
setlocal enabledelayedexpansion
if not "%1"=="" GOTO cont
echo Usage: example.bat ExampleClassName arguments
echo See the README for the available examples
GOTO end

:cont
set CLASSPATH=
for /r %%I in (..\..\cssbox*.jar) do set CLASSPATH=%CLASSPATH%;%%I
for /r %%I in (..\..\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%I
echo %CLASSPATH%

java org.fit.cssbox.demo.%1 %2 %3 %4

:end

