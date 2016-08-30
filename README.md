# MUSCLE 2

<img alt="MUSCLE overview" align="right" src="https://github.com/psnc-apps/muscle2/wiki/images/muscle_overview_vertical.png" />

MUSCLE 2 - The Multiscale Coupling Library and Environment is a portable framework to do multiscale modeling and simulation on distributed computing resources. The generic coupling mechanism of MUSCLE is suitable for many types of multiscale applications, notably for multiscale models as defined by the [MAPPER project](http://www.mapper-project.eu/). Submodels can be implemented from scratch, but legacy code can also be used with only minor adjustments. The runtime environment solves common problems in distributed computing and couples submodels of a multiscale model, whether they are built for high-performance supercomputers or for local execution. MUSCLE supports Java, C, C++, Fortran, Python, MATLAB and Scala code, using MPI, OpenMP, or threads.

MUSCLE is being enhanced and adapted to the Grid infrastructures. Parts of the reservation and management interacts with the [QCG-Computing middleware stack](http://www.qoscosgrid.org/), intended for job management on e-Infrastructure.

## Getting started

Most information is in the wiki, see
* [Installation](wiki/Installation)
* [Documentation](wiki)
* [Tutorial](wiki/Tutorial)

## How to cite

Publications resulting from simulations with MUSCLE 2 are invited to cite the following:
J. Borgdorff, M. Mamonski, B. Bosak, K. Kurowski, M. Ben Belgacem, B. Chopard, D. Groen, P. V. Coveney, and A. G. Hoekstra, “Distributed Multiscale Computing with MUSCLE 2, the Multiscale Coupling Library and Environment,” Journal of Computational Science. 5 (2014) 719–731. doi:[10.1016/j.jocs.2014.04.004](http://dx.doi.org/10.1016/j.jocs.2014.04.004)

Other publications and applications using MUSCLE are listed on the [Publication page](wiki/publications).

## Contact

For any questions or bugs, please use the issue tracker.

## License

MUSCLE 2 is open source software under the [LGPL version 3 license](https://www.gnu.org/licenses/lgpl.html).
