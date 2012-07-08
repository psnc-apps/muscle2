export CMAKE_CXX_FLAGS='-g -Wall '
export BOOST_INCLUDEDIR=/home/plgrid-groups/plggmuscle/deps/boost/include/
export BOOST_LIBRARYDIR=/home/plgrid-groups/plggmuscle/deps/boost/lib/

module load java
module load openmpi

cmake -DMUSCLE_INSTALL_PREFIX=/home/plgrid-groups/plggmuscle/2.0/ ..
make
make install
