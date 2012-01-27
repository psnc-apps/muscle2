#!/bin/sh

export MUSCLE_DIR=muscle-`cat VERSION`

rm $MUSCLE_DIR.zip
rm -rf $MUSCLE_DIR

./build.rb clean
./build.rb java
mkdir -p  $MUSCLE_DIR/build
cp build/muscle.jar $MUSCLE_DIR/build
cp build.rb $MUSCLE_DIR/
cp -r src $MUSCLE_DIR/
cp -r scripts $MUSCLE_DIR/
cp -r thirdparty $MUSCLE_DIR/
cp -r doc $MUSCLE_DIR/
cp VERSION $MUSCLE_DIR/
cp CMakeLists.txt $MUSCLE_DIR/
find $MUSCLE_DIR -name .svn -exec rm -rf {} \;
zip -r $MUSCLE_DIR.zip $MUSCLE_DIR

