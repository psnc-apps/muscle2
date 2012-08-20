#1. Update to the latest version
cd ..
svn up
cd build

#2. Source local configuration
. ${HOSTNAME:-`hostname`}.conf

if [ -f "$PBS_O_HOST.conf" ]
then
	. $PBS_O_HOST.conf
fi

#3. Build release
cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel -DCMAKE_BUILD_TYPE=Release ..
make
make install

#4. Build with debug symbols
cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel-debug -DCMAKE_BUILD_TYPE=Debug ..
make
make install


