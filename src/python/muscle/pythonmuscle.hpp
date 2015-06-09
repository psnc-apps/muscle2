/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#ifndef MUSCLE_PYTHON_HPP
#define MUSCLE_PYTHON_HPP
#include <Python.h>

#ifdef __cplusplus
extern "C" {
#endif

static PyObject *muscle_get_property(PyObject *self, PyObject *args);
static PyObject *muscle_has_property(PyObject *self, PyObject *args);
static PyObject *muscle_will_stop(PyObject *self, PyObject *args);
static PyObject *muscle_init(PyObject *self, PyObject *args);
static PyObject *muscle_finalize(PyObject *self, PyObject *args);
static PyObject *muscle_kernel_name(PyObject *self, PyObject *args);
static PyObject *muscle_has_next(PyObject *self, PyObject *args);
static PyObject *muscle_send(PyObject *self, PyObject *args);
static PyObject *muscle_receive(PyObject *self, PyObject *args);
static PyObject *muscle_tmp_path(PyObject *self, PyObject *args);

static PyObject *muscle_log(PyObject *self, PyObject *args);
static PyObject *muscle_severe(PyObject *self, PyObject *args);
static PyObject *muscle_warning(PyObject *self, PyObject *args);
static PyObject *muscle_fine(PyObject *self, PyObject *args);

#ifdef __cplusplus
}
#endif

#endif
