# find JNI libraries and header files
# adding to the the FindJNI.cmake of cmake version 2.4-patch 7 because it does not find the jni.h on some boxes
# author: Jan Hegewald (only the custom search paths, see the unindented lines)
										
										# - Find JNI java libraries.
										# This module finds if Java is installed and determines where the
										# include files and libraries are. It also determines what the name of
										# the library is. This code sets the following variables:
										#   
										#  JAVA_AWT_LIB_PATH     = the path to the jawt library
										#  JAVA_JVM_LIB_PATH     = the path to the jvm library
										#  JAVA_INCLUDE_PATH     = the include path to jni.h
										#  JAVA_INCLUDE_PATH2    = the include path to jni_md.h
										#  JAVA_AWT_INCLUDE_PATH = the include path to jawt.h
										# 
					
										SET(JAVA_AWT_LIBRARY_DIRECTORIES
										  "[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.4;JavaHome]/lib"
										  "[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.3;JavaHome]/lib"
										  $ENV{JAVA_HOME}/jre/lib/i386
										  $ENV{JAVA_HOME}/jre/lib/amd64
										  $ENV{JAVA_HOME}/jre/lib/ppc64
										  /usr/lib
										  /usr/local/lib
										  /usr/lib/java/jre/lib/i386
										  /usr/local/lib/java/jre/lib/i386
										  /usr/local/share/java/jre/lib/i386
										  /usr/lib/j2sdk1.4-sun/jre/lib/i386
										  /usr/lib/j2sdk1.5-sun/jre/lib/i386
										  /opt/sun-jdk-1.5.0.04/jre/lib/amd64
										  /usr/lib/java/jre/lib/amd64
										  /usr/local/lib/java/jre/lib/amd64
										  /usr/local/share/java/jre/lib/amd64
										  /usr/lib/j2sdk1.4-sun/jre/lib/amd64
										  /usr/lib/j2sdk1.5-sun/jre/lib/amd64
										  )
										
										SET(JAVA_JVM_LIBRARY_DIRECTORIES)
										FOREACH(dir ${JAVA_AWT_LIBRARY_DIRECTORIES})
										  SET(JAVA_JVM_LIBRARY_DIRECTORIES
											 ${JAVA_JVM_LIBRARY_DIRECTORIES}
											 "${dir}"
											 "${dir}/default"
											 "${dir}/client"
											 "${dir}/server"
											 )
										ENDFOREACH(dir)
					

SET(JAVA_AWT_INCLUDE_DIRECTORIES
/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Headers
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\2.0;JavaHome]/include"
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.9;JavaHome]/include"
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.8;JavaHome]/include"
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.7;JavaHome]/include"
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.6;JavaHome]/include"
"[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.5;JavaHome]/include"
										  "[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.4;JavaHome]/include"
										  "[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\1.3;JavaHome]/include"
										  $ENV{JAVA_HOME}/include
										  /usr/include 
										  /usr/local/include
										  /usr/lib/java/include
										  /usr/local/lib/java/include
										  /usr/local/share/java/include
										  /usr/lib/j2sdk1.4-sun/include
										  /usr/lib/j2sdk1.5-sun/include
										  /opt/sun-jdk-1.5.0.04/include
/usr/lib/jvm/java-6-sun/include
/usr/lib/jvm/java-6-openjdk/include
)
					
											FOREACH(JAVA_PROG "${JAVA_RUNTIME}" "${JAVA_COMPILE}" "${JAVA_ARCHIVE}")
											  GET_FILENAME_COMPONENT(jpath "${JAVA_PROG}" PATH)
											  FOREACH(JAVA_INC_PATH ../include ../java/include ../share/java/include)
												 IF(EXISTS ${jpath}/${JAVA_INC_PATH})
													SET(JAVA_AWT_INCLUDE_DIRECTORIES ${JAVA_AWT_INCLUDE_DIRECTORIES} "${jpath}/${JAVA_INC_PATH}")
												 ENDIF(EXISTS ${jpath}/${JAVA_INC_PATH})
											  ENDFOREACH(JAVA_INC_PATH)
											  FOREACH(JAVA_LIB_PATH 
												 ../lib ../jre/lib ../jre/lib/i386 ../jre/lib/ppc64
												 ../java/lib ../java/jre/lib ../java/jre/lib/i386  ../java/jre/lib/ppc64
												 ../share/java/lib ../share/java/jre/lib ../share/java/jre/lib/i386 ../share/java/jre/lib/ppc64)
												 IF(EXISTS ${jpath}/${JAVA_LIB_PATH})
													SET(JAVA_AWT_LIBRARY_DIRECTORIES ${JAVA_AWT_LIBRARY_DIRECTORIES} "${jpath}/${JAVA_LIB_PATH}")
												 ENDIF(EXISTS ${jpath}/${JAVA_LIB_PATH})
											  ENDFOREACH(JAVA_LIB_PATH)
											ENDFOREACH(JAVA_PROG)
											
											IF(APPLE)
											  IF(EXISTS ~/Library/Frameworks/JavaVM.framework)
												 SET(JAVA_HAVE_FRAMEWORK 1)
											  ENDIF(EXISTS ~/Library/Frameworks/JavaVM.framework)
											  IF(EXISTS /Library/Frameworks/JavaVM.framework)
												 SET(JAVA_HAVE_FRAMEWORK 1)
											  ENDIF(EXISTS /Library/Frameworks/JavaVM.framework)
											  IF(JAVA_HAVE_FRAMEWORK)
												 IF(NOT JAVA_AWT_LIBRARY)
													SET (JAVA_AWT_LIBRARY "-framework JavaVM" CACHE FILEPATH "Java Frameworks" FORCE)
												 ENDIF(NOT JAVA_AWT_LIBRARY)
												 IF(NOT JAVA_JVM_LIBRARY)
													SET (JAVA_JVM_LIBRARY "-framework JavaVM" CACHE FILEPATH "Java Frameworks" FORCE)
												 ENDIF(NOT JAVA_JVM_LIBRARY)
												 SET(JAVA_AWT_INCLUDE_DIRECTORIES ${JAVA_AWT_INCLUDE_DIRECTORIES}
													~/Library/Frameworks/JavaVM.framework/Headers
													/Library/Frameworks/JavaVM.framework/Headers
													/System/Library/Frameworks/JavaVM.framework/Headers
													)
											  ENDIF(JAVA_HAVE_FRAMEWORK)
											ELSE(APPLE)
											  FIND_LIBRARY(JAVA_AWT_LIBRARY jawt 
												 PATHS ${JAVA_AWT_LIBRARY_DIRECTORIES}
											  )
											  FIND_LIBRARY(JAVA_JVM_LIBRARY NAMES jvm JavaVM
												 PATHS ${JAVA_JVM_LIBRARY_DIRECTORIES}
											  )
											ENDIF(APPLE)
					
# add in the include path
# first seach our given paths only
# then include default paths if nothing is found
FIND_PATH(JAVA_INCLUDE_PATH jni.h 
${JAVA_AWT_INCLUDE_DIRECTORIES}
NO_DEFAULT_PATH
)
IF(NOT JAVA_INCLUDE_PATH-NOTFOUND)
FIND_PATH(JAVA_INCLUDE_PATH jni.h 
)
ENDIF(NOT JAVA_INCLUDE_PATH-NOTFOUND)

FIND_PATH(JAVA_INCLUDE_PATH2 jni_md.h 
${JAVA_AWT_INCLUDE_DIRECTORIES}
${JAVA_INCLUDE_PATH}/win32
${JAVA_INCLUDE_PATH}/linux
/usr/lib64/gcc/powerpc64-suse-linux/4.3/include
NO_DEFAULT_PATH
)
IF(NOT JAVA_INCLUDE_PATH2-NOTFOUND)
FIND_PATH(JAVA_INCLUDE_PATH2 jni_md.h 
)
ENDIF(NOT JAVA_INCLUDE_PATH2-NOTFOUND)
										
										
												
												FIND_PATH(JAVA_AWT_INCLUDE_PATH jawt.h 
															 ${JAVA_AWT_INCLUDE_DIRECTORIES} ${JAVA_INCLUDE_PATH} )
												
												MARK_AS_ADVANCED(
												  JAVA_AWT_LIBRARY
												  JAVA_JVM_LIBRARY
												  JAVA_AWT_INCLUDE_PATH
												  JAVA_INCLUDE_PATH
												  JAVA_INCLUDE_PATH2
												)
