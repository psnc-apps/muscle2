export CMAKE_CXX_FLAGS='-g -Wall '

export BOOST_INCLUDEDIR=/mnt/lustre/scratch/groups/plggmuscle/muscle/dep/include/
export BOOST_LIBRARYDIR=/mnt/lustre/scratch/groups/plggmuscle/muscle/dep/lib/

module load openmpi
module load java

cmake -DMUSCLE_INSTALL_PREFIX=/mnt/lustre/scratch/groups/plggmuscle/2.0/devel ..
make
make install
