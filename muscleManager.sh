#!/bin/sh
java -Duser.language=en -server -Xms64m -Xmx128m -Xss1m -Djava.util.logging.config.file=loggingManager.properties -Dmuscle.Env=file:/var/folders/vf/8gm39lvs7591j5gy6r8xh91r0000gn/T/bobby_20120322141037_86404/muscle.Env -classpath /opt/muscle/share/muscle/java/muscle.jar:/opt/muscle/share/muscle/java/muscle_java.jar:/opt/muscle/share/muscle/java/thirdparty/collections-generic-4.01.jar:/opt/muscle/share/muscle/java/thirdparty/colt-1.2.0.jar:/opt/muscle/share/muscle/java/thirdparty/JadeLeap.jar:/opt/muscle/share/muscle/java/thirdparty/jcommander-1.17.jar:/opt/muscle/share/muscle/java/thirdparty/jna.jar:/opt/muscle/share/muscle/java/thirdparty/json_simple-1.1.jar:/opt/muscle/share/muscle/java/thirdparty/jsr-275-1.0-beta-2.jar:/opt/muscle/share/muscle/java/thirdparty/oncrpc.jar:/opt/muscle/share/muscle/java/thirdparty/platform.jar:/opt/muscle/share/muscle/java/thirdparty/wstx-asl-3.2.7.jar:/opt/muscle/share/muscle/java/thirdparty/xstream-1.2.2.jar -Djava.io.tmpdir=/var/folders/vf/8gm39lvs7591j5gy6r8xh91r0000gn/T/bobby_20120322141037_86404 muscle.manager.SimulationManager Ping Pong
