export CMAKE_CXX_FLAGS='-g -Wall '

export BOOST_INCLUDEDIR=/home/plgrid-groups/plggmuscle/dep/boost/include/
export BOOST_LIBRARYDIR=/home/plgrid-groups/plggmuscle/dep/boost/lib/

module load openmpi
module load java

cmake -DMUSCLE_INSTALL_PREFIX=/home/plgrid-groups/plggmuscle/2.0/devel ..
make
make install
