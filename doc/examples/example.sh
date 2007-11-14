#! /bin/sh
if [ $# -lt 1 ]; then
	echo "Usage: example.sh <ExampleClassName> <arguments>"
	echo "See the documentation for the available examples"
	exit
fi

ROOT="../.."

export CLASSPATH=$CLASSPATH:$ROOT/CSSBox.jar:$ROOT/lib/CSSParser_SNAPSHOT.jar:$ROOT/lib/jtidy-r8-SNAPSHOT.jar
java org.fit.cssbox.demo.$1 $2 $3 $4
