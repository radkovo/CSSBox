#! /bin/sh
if [ $# -lt 1 ]; then
	echo "Usage: example.sh <ExampleClassName> <arguments>"
	echo "See the README for the available examples"
	exit
fi

ROOT="../.."

DEPS=""
for I in $ROOT/cssbox*.jar; do
  DEPS=$I
done
for I in $ROOT/lib/*.jar; do
  DEPS=$DEPS:$I
done

export CLASSPATH=$CLASSPATH:$DEPS
java org.fit.cssbox.demo.$1 $2 $3 $4
