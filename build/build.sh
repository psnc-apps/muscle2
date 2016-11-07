#!/bin/bash

function usage {
	echo "USAGE: $0 [OPTIONS]"
	echo "Builds and installs MUSCLE to specific directory using provided settings"
	echo "If no options are specified MUSCLE will be tried to build using configuration"
	echo "from files with predefined names and then installed in /opt/muscle"
	echo ""
	echo "Options:"
	echo "  -c, --config=FILE         use configuration stored in FILE"
	echo "  -i, --install-prefix=DIR  install to DIR"
	echo "  -g, --use-git-tag         create a subdir in DIR with the name of last git tag"
	echo "  -p, --performance         include C++ performance counters"
	echo "  -h, --help                display this message"
	exit $1
}

#1. handle arguments
BUILD_PERF="OFF"             # don't include perf counters by default
USE_GIT_TAG="OFF"            # don't include git tag name in installation path
CONFIG=                      # use automatic configuration
INSTALL_PREFIX=/opt/muscle/  # use default configuration

OPTS=`getopt -o hpc:i:g --long help,performance,config:,install-prefix:use-git-tag -n 'parse-options' -- "$@"`

if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi

eval set -- "$OPTS"

while true; do
  case "$1" in
    -h | --help )    usage 0; shift ;;
    -p | --performance ) BUILD_PERF="ON"; echo "BUILD support for performance counters"; shift ;;
    -c | --config ) CONFIG=$2; shift; shift ;;
    -i | --install-prefix ) PROVIDED_PREFIX="$2"; shift; shift ;;
    -g | --use-git-tag ) USE_GIT_TAG="ON"; shift ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

#1. Source local configuration
HOSTNAME=`hostname -f`
if [ -f "$CONFIG" ]; then
	. "$CONFIG"
	echo "Using configuration in $CONFIG"
elif [ -f "$HOSTNAME.conf" ]; then
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

if [ "$USE_GIT_TAG" == "ON" ]; then
	INSTALL_PREFIX_GIT=`git describe --abbrev=0 --tags`
	echo "Using git tag name to define destination directory: $INSTALL_PREFIX_GIT"
fi

#2 Resolve canonical path name
if [ -n "$PROVIDED_PREFIX" ]; then
	INSTALL_PREFIX="$PROVIDED_PREFIX"
fi

if [ -n "$INSTALL_PREFIX_GIT" ]; then
	INSTALL_PREFIX="$INSTALL_PREFIX"/"$INSTALL_PREFIX_GIT"
fi

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

INSTALL_DIR=`dirname "$INSTALL_PREFIX"`
if [ -w "$INSTALL_DIR" ] || [ -w "$INSTALL_PREFIX" ]; then
	echo "Installing MUSCLE in $INSTALL_PREFIX"
else
	echo "Can not install MUSCLE in $INSTALL_PREFIX: permission denied; try"
	echo "setting a different INSTALL_PREFIX or running as root." 
	echo
	usage 1
fi

#3. BUILD & Install MUSCLE
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

