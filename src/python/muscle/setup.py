# Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
#
# GNU Lesser General Public License
# 
# This file is part of MUSCLE (Multiscale Coupling Library and Environment).
# 
# MUSCLE is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# MUSCLE is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
#
from distutils.core import setup, Extension

musclemodule = Extension(
        'muscle',
        sources=['pythonmuscle.cpp'],
        libraries = ['muscle2'])

setup(name = 'muscle',
      version='0.1',
      description = 'The MUSCLE 2 package',
      ext_modules=[musclemodule])
