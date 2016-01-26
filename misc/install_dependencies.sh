#! /bin/sh
set -ev

TMP=~/tmp
TESTING=$TMP/CSSBoxTesting
PARSER=$TMP/jStyleParser

mkdir $TMP
mkdir $TESTING
mkdir $PARSER

git clone https://github.com/radkovo/CSSBoxTesting.git $TESTING
git clone https://github.com/radkovo/jStyleParser.git $PARSER
(cd $PARSER; mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V)
