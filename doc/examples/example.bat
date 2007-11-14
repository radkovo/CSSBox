@echo off

if not "%1"=="" GOTO cont
echo Usage: example.bat ExampleClassName arguments
echo See the documentation for the available examples
GOTO end

:cont
set CLASSPATH=%CLASSPATH%;..\..\CSSBox.jar;..\..\lib\CSSParser_SNAPSHOT.jar;..\..\lib\jtidy-r8-SNAPSHOT.jar
java org.fit.cssbox.demo.%1 %2 %3 %4


:end

