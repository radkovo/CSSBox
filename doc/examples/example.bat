@echo off

if not "%1"=="" GOTO cont
echo Usage: example.bat ExampleClassName arguments
echo See the documentation for the available examples
GOTO end

:cont
set CLASSPATH=%CLASSPATH%;..\..\CSSBox.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\antlr-runtime-3.1.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\slf4j-api-1.5.2.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\jStyleParser_SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\jTidy-090119-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\logback-classic-0.9.9.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\logback-core-0.9.9.jar

java org.fit.cssbox.demo.%1 %2 %3 %4


:end

