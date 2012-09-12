#!/bin/bash

function usage {
	echo "USAGE: $0 [-h|--help|INSTALL_PREFIX]"
	echo "Builds and installs MUSCLE to INSTALL_PREFIX (default: /opt/muscle)."
	echo "If file \`hostname\`.conf is present, a RELEASE and DEBUG version are installed"
	echo "in \$INSTALL_PREFIX/devel and \$INSTALL_PREFIX/devel-debug, respectively."
	exit $1
}

if [[ $# -eq 1 && ($1 = "--help" || $1 = "-h") ]]; then
	usage 0
fi

#1. Update to the latest version
echo -n "Updating MUSCLE: "
cd ..
svn up
cd build

#2. Source local configuration
HOSTNAME=`hostname -f`
MODE="install"
if [ -f "$HOSTNAME.conf" ]; then
	. $HOSTNAME.conf
	echo "Using configuration in $HOSTNAME.conf"
	# Don't install in INSTALL_PREFIX, but in subdirectory with Debug and Release branches
	MODE="maintenance"
else
	echo "No preset configuration for $HOSTNAME is present; performing regular installation"
fi

if [ -f "$PBS_O_HOST.conf" ]
then
	. $PBS_O_HOST.conf
	echo "Using PBS configuration in $PBS_O_HOST.conf"
fi

if [ $# -eq 1 ]; then
	# If given, always use install prefix from argument
	INSTALL_PREFIX=$1
elif [ "x$INSTALL_PREFIX" = "x" ]; then
	# If no INSTALL_PREFIX is set yet, use the default
	INSTALL_PREFIX="/opt/muscle"
fi

if [ -w "$INSTALL_PREFIX" ]; then
	echo "Installing MUSCLE in $INSTALL_PREFIX"
else
	echo "Can not install MUSCLE in $INSTALL_PREFIX: permission denied; try"
	echo "setting a different INSTALL_PREFIX or running as root." 
	echo
	usage 1
        exit 1
fi

if [ "$MODE" = "install" ]; then
	#3. Install MUSCLE
	echo "========== BUILDING MUSCLE ==========="
	cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX -DCMAKE_BUILD_TYPE=Release $MUSCLE_CMAKE_OPTIONS .. \
	&& make -j 4 install
	if [ $? -eq 0 ]; then
		echo "----------- MUSCLE INSTALLED IN $INSTALL_PREFIX -----------"
	else
		echo "!!!!!!!!!! BUILDING MUSCLE FAILED !!!!!!!!!!!"
		exit 1
	fi
else
	#3. Build release
	echo "========== BUILDING RELEASE ==========="
	SUCCESS=[[ cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel -DCMAKE_BUILD_TYPE=Release $MUSCLE_CMAKE_OPTIONS .. && make -j 4 install ]]
	if [ $SUCCESS ]; then
		echo "----------- RELEASE INSTALLED IN $INSTALL_PREFIX/devel -----------"
	else
		echo "!!!!!!!!!! BUILDING RELEASE FAILED !!!!!!!!!!!"
		exit 1
	fi
	
	#4. Build with debug symbols
	echo "========== BUILDING DEBUG version ==========="
	SUCCESS=cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel-debug -DCMAKE_BUILD_TYPE=Debug $MUSCLE_CMAKE_OPTIONS .. \
		&& make -j 4 install
	if [ $SUCCESS ]; then
		echo "----------- DEBUG INSTALLED IN $INSTALL_PREFIX/devel -----------"
	else
		echo "!!!!!!!!!! BUILDING DEBUG FAILED !!!!!!!!!!!"
		exit 1
	fi
fi
