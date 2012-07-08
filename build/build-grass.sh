export CMAKE_CXX_FLAGS='-g -Wall '
export BOOST_INCLUDEDIR=/usr/local/boost/include/
export BOOST_LIBRARYDIR=/usr/local/boost/lib/

cmake -DMUSCLE_INSTALL_PREFIX=/opt/app/muscle/2.0/devel ..
make
make install
