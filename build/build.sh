#1. Update to the latest version
cd ..
svn up
cd build

#2. Source local configuration
. ${HOSTNAME:-`hostname`}.conf

#3. Build release
cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel -DCMAKE_BUILD_TYPE=Release ..
make
make install

#4. Build with debug symbols
cmake -DMUSCLE_INSTALL_PREFIX=$INSTALL_PREFIX/devel-debug -DCMAKE_BUILD_TYPE=Debug ..
make
make install


