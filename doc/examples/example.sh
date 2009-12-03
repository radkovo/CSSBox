#! /bin/sh
if [ $# -lt 1 ]; then
	echo "Usage: example.sh <ExampleClassName> <arguments>"
	echo "See the documentation for the available examples"
	exit
fi

ROOT="../.."

DEPS=$ROOT/CSSBox.jar
DEPS=$DEPS:$ROOT/lib/antlr-runtime-3.1.jar
DEPS=$DEPS:$ROOT/lib/slf4j-api-1.5.2.jar
DEPS=$DEPS:$ROOT/lib/jStyleParser_SNAPSHOT.jar
DEPS=$DEPS:$ROOT/lib/nekohtml.jar
DEPS=$DEPS:$ROOT/lib/xercesImpl.jar
DEPS=$DEPS:$ROOT/lib/xml-apis.jar
DEPS=$DEPS:$ROOT/lib/logback-classic-0.9.9.jar
DEPS=$DEPS:$ROOT/lib/logback-core-0.9.9.jar

export CLASSPATH=$CLASSPATH:$DEPS
java org.fit.cssbox.demo.$1 $2 $3 $4
