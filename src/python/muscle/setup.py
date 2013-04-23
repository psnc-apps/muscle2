from distutils.core import setup, Extension

musclemodule = Extension(
        'muscle',
        sources=['pythonmuscle.cpp'],
        libraries = ['muscle2'])

setup(name = 'muscle',
      version='0.1',
      description = 'The MUSCLE 2 package',
      ext_modules=[musclemodule])
