JNIH = $(JAVA_HOME)/include/
JAVAH = javah

all:
	@echo " Muscle can run without OTFLogger"
	@echo -n " Compiling OTFLogger..."
	@rm -f build/intermediate/classes/muscle_utilities_OTFLogger.h 
	@cp src/java/muscle/utilities/muscle_utilities_OTFLogger.c build/intermediate/classes/ 
	@$(JAVAH) -jni -classpath build/intermediate/classes/ -d build/intermediate/classes/ muscle.utilities.OTFLogger	
	@gcc $(CXXFLAGS) -Wall -fPIC -shared -I$(JNIH) build/intermediate/classes/muscle_utilities_OTFLogger.c -o build/intermediate/classes/libmuscle_utilities_OTFLogger.so -lotf -lrt -lz 
	@mv build/intermediate/classes/libmuscle_utilities_OTFLogger.so build/
	@echo " OK"
	
clean:
	@echo " CLEAN"
	@rm -f build/intermediate/classes/muscle_utilities_OTFLogger.h 
	@rm -f build/libmuscle_utilities_OTFLogger.so
