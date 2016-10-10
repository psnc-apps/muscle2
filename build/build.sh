#!/bin/bash

function usage {
	echo "USAGE: $0 [-h|--help|-p|--performance] [INSTALL_PREFIX]"
	echo "Builds and installs MUSCLE to INSTALL_PREFIX (default: /opt/muscle)."
	echo "If file \`hostname\`.conf is present, a RELEASE and DEBUG version are installed"
	echo "in \$INSTALL_PREFIX/devel and \$INSTALL_PREFIX/devel-debug, respectively."
	echo ""
	echo "Arguments:"
	echo "  -p, --performance        include C++ performance counters"
	echo "  -h, --help               display this message"
	exit $1
}

#0. handle arguments
BUILD_PERF="OFF"   # don't include perf counters by default

for arg in "$@"; do
	case "$arg" in
		"-h" | "--help")
			usage 0
		;;
		"-p" | "--performance")
			BUILD_PERF="ON"
		;;
	esac
done

#1. Update to the latest version
echo -n "Updating MUSCLE: "
cd ..
svn up
cd build

#2. Source local configuration
HOSTNAME=`hostname -f`
if [ -f "$HOSTNAME.conf" ]; then
	. "$HOSTNAME.conf"
	echo "Using configuration in $HOSTNAME.conf"
elif [ -f "$QCG_HOST.conf" ]; then
	. "$QCG_HOST.conf"
	echo "Using configuration in $QCG_HOST.conf"
elif [ -f "$PBS_O_HOST.conf" ]; then
	. "$PBS_O_HOST.conf"
	echo "Using PBS configuration in $PBS_O_HOST.conf"
else
	echo "No preset configuration for $HOSTNAME is present; performing regular installation"
fi

# get install prefix
prefix_index=1;
if [ "$BUILD_PERF" = "ON" ]; then
	prefix_index=$[$prefix_index+1]
fi
pref_arg=${!prefix_index}
 
if [ $# -ge $prefix_index ] && [ "$pref_arg" != "install" ] && [ "$pref_arg" != "maintenance" ]
then
	# If given, always use install prefix from argument
	INSTALL_PREFIX="$pref_arg"
fi
if [ "$INSTALL_PREFIX" = "" ]; then
	# If no INSTALL_PREFIX is set yet, use the default
	INSTALL_PREFIX="/opt/muscle"
else
	# Resolve canonical path name
	if [ -d "$INSTALL_PREFIX" ]; then
		cd "$INSTALL_PREFIX"
		INSTALL_PREFIX="$PWD"
		cd "$OLDPWD"
	else
		# If directory does not exist, try the parent directory
		INSTALL_DIR=`dirname "$INSTALL_PREFIX"`
		INSTALL_BASE=`basename "$INSTALL_PREFIX"`
		if [ -d $INSTALL_DIR ]; then
			cd "$INSTALL_DIR"
			INSTALL_DIR="$PWD"
			cd "$OLDPWD"
			INSTALL_PREFIX="$INSTALL_DIR/$INSTALL_BASE"
		else
			echo "Can not install MUSCLE in $INSTALL_PREFIX: parent directory does not exist."
			echo
			usage 1
		fi
	fi
fi

INSTALL_DIR=`dirname "$INSTALL_PREFIX"`
if [ -w "$INSTALL_DIR" ] || [ -w "$INSTALL_PREFIX" ]; then
	echo "Installing MUSCLE in $INSTALL_PREFIX"
else
	echo "Can not install MUSCLE in $INSTALL_PREFIX: permission denied; try"
	echo "setting a different INSTALL_PREFIX or running as root." 
	echo
	usage 1
fi

#3. Install MUSCLE
echo "========== BUILDING MUSCLE ==========="
cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX \
      -DCMAKE_BUILD_TYPE=Release $MUSCLE_CMAKE_OPTIONS \
      -DBUILD_PERF=$BUILD_PERF .. \
&& make clean && (make -j 4; make install)
if [ $? -eq 0 ]; then
	echo "----------- MUSCLE INSTALLED IN $INSTALL_PREFIX -----------"
else
	echo "!!!!!!!!!! BUILDING MUSCLE FAILED !!!!!!!!!!!"
	exit 1
fi

