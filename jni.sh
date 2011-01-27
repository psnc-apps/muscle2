#!/bin/bash
rm -f build/intermediate/classes/muscle_utilities_OTFLogger.h 
cp src/java/muscle/utilities/muscle_utilities_OTFLogger.c build/intermediate/classes && cd build/intermediate/classes && javah -jni muscle.utilities.OTFLogger && gcc -fPIC -shared -I/usr/lib/jvm/java-6-openjdk/include/ muscle_utilities_OTFLogger.c -o libmuscle_utilities_OTFLogger.so -lotf -lrt -lz && mv libmuscle_utilities_OTFLogger.so ../..
cd -
