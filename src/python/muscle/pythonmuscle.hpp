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
