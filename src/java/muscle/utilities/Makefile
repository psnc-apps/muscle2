JNIH1 = $(JAVA_HOME)/include
JNIH2 = $(JAVA_HOME)/include/linux
JAVAH = javah

SOURCES = $(MUSCLE_DIR)/src/java/muscle/utilities/muscle_utilities_OTFLogger.c
TARGET_DIR = $(MUSCLE_DIR)/build/intermediate/otf

all: $(SOURCES) javah
	@echo " Muscle can run without OTFLogger"
	@echo -n " Compiling OTFLogger..."
	@gcc $(CXXFLAGS) -Wall -fPIC -shared -I$(JNIH1) -I$(JNIH2) -I$(OTF_HOME)/include -I$(TARGET_DIR) $(SOURCES) -o $(TARGET_DIR)/libmuscle_utilities_OTFLogger.so -L$(OTF_HOME)/lib -lotf -lrt -lz 
	@echo " OK"

javah: 
	@$(JAVAH) -jni -classpath $(MUSCLE_DIR)/build/intermediate/classes/ -d $(TARGET_DIR) muscle.utilities.OTFLogger	

install: all
	@cp $(TARGET_DIR)/libmuscle_utilities_OTFLogger.so $(MUSCLE_DIR)/build/
	
clean:
	@echo " CLEAN"
	@rm -rf $(TARGET_DIR) 
	@rm -f $(MUSCLE_DIR)/build/libmuscle_utilities_OTFLogger.so
