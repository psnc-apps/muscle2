#include <Python.h>
#include <string>
#include <stdlib.h>
#include <cassert>

#include "cppmuscle.hpp"
#include "exception.hpp"
#include "pythonmuscle.hpp"

#define MUSCLE_PY_CALL(CALL) try { CALL; } catch (muscle_exception ex) { MUSCLE_PY_ERR(ex.what()) }
#define MUSCLE_PY_SET(SET, CALL) MUSCLE_PY_CALL(SET = CALL)
#define MUSCLE_PY_ERR(MSG) MUSCLE_PY_PY_ERR(MuscleError, MSG)
#define MUSCLE_PY_PY_ERR(ERR, MSG) { PyErr_SetString(ERR, MSG); return NULL; }


using namespace muscle;
using namespace std;

static PyObject *MuscleError;

static PyMethodDef MuscleMethods[] = {
    {"get_property",  muscle_get_property, METH_VARARGS,"Get a property from the CxA file."},
    {"has_property",  muscle_has_property, METH_VARARGS,"Whether a given property is specified."},
    {"will_stop",  muscle_will_stop, METH_VARARGS,"Whether all time steps have been completed."},
    {"init",  muscle_init, METH_VARARGS,"Initialize MUSCLE 2."},
    {"finalize",  muscle_finalize, METH_VARARGS,"Finalize MUSCLE 2."},
    {"kernel_name",  muscle_kernel_name, METH_VARARGS,"Get the name of the current kernel."},
    {"send",  muscle_send, METH_VARARGS,"Send a message over a given port."},
    {"receive",  muscle_receive, METH_VARARGS,"Receive a message from a given port."},
    {"tmp_path",  muscle_tmp_path, METH_VARARGS,"Get the path reserved for the current kernel."},
    {"log",  muscle_log, METH_VARARGS,"Log and output a message."},
    {"severe",  muscle_severe, METH_VARARGS,"Log and output an error."},
    {"warning",  muscle_warning, METH_VARARGS,"Log and output a warning."},
    {"fine",  muscle_fine, METH_VARARGS,"Log a debug statement."},
    {NULL, NULL, 0, NULL}        /* Sentinel */
};

namespace muscle {
    template <typename P> inline bool pyCheck(PyObject *);
    template <typename P> inline P pyConvert(PyObject *);
    template <typename T> inline void pyFree(T *, size_t);
    template <> inline void pyFree<char *>(char **, size_t);
    template <> inline void pyFree<unsigned char *>(unsigned char **, size_t);
    template <typename P> inline PyObject *pyCreate(P);
    template <typename T, typename P> T *pyListToArray(PyObject *, size_t&, string);
    // String (MUSCLE_STRING)
    template <> char *pyListToArray<char,char>(PyObject *, size_t&, string);
    // ByteArray (MUSCLE_RAW)
    template <> unsigned char *pyListToArray<unsigned char,unsigned char>(PyObject *, size_t&, string);
    template <typename T, typename P> PyObject *pyArrayToList(void *, size_t, string);
    // String (MUSCLE_STRING)
    template <> PyObject *pyArrayToList<char,char>(void *, size_t, string);
    // ByteArray (MUSCLE_RAW)
    template <> PyObject *pyArrayToList<unsigned char,unsigned char>(void *, size_t, string);
}

PyMODINIT_FUNC initmuscle()
{
    PyObject *m;

    m = Py_InitModule("muscle", MuscleMethods);
    if (m == NULL) {
        return;
	}
    char *name = strdup("muscle.error");
    MuscleError = PyErr_NewException(name, NULL, NULL);
    Py_INCREF(MuscleError);
    free(name);
    PyModule_AddObject(m, "error", MuscleError);
    PyModule_AddObject(m, "double", PyInt_FromLong(MUSCLE_DOUBLE));
    PyModule_AddObject(m, "float", PyInt_FromLong(MUSCLE_FLOAT));
    PyModule_AddObject(m, "int", PyInt_FromLong(MUSCLE_INT32));
    PyModule_AddObject(m, "long", PyInt_FromLong(MUSCLE_INT64));
    PyModule_AddObject(m, "bool", PyInt_FromLong(MUSCLE_BOOLEAN));
    PyModule_AddObject(m, "raw", PyInt_FromLong(MUSCLE_RAW));
    PyModule_AddObject(m, "string", PyInt_FromLong(MUSCLE_STRING));
}

static PyObject *muscle_get_property(PyObject *self, PyObject *args)
{
	const char *prop;
    
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("Argument to get_property should be a string");
	string propstr(prop);
	string result;
	MUSCLE_PY_SET(result, cxa::get_property(propstr));
	return Py_BuildValue("s", result.c_str());
}
static PyObject *muscle_has_property(PyObject *self, PyObject *args)
{
	const char *prop;
    
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("Argument to has_property should be a string");
	string propstr(prop);
	bool result;
	MUSCLE_PY_SET(result, cxa::has_property(propstr));
    return Py_BuildValue("i", result);
}

static PyObject *muscle_will_stop(PyObject *self, PyObject *args)
{
	int result;
	MUSCLE_PY_SET(result, env::will_stop());
    return Py_BuildValue("i", result);
}
static PyObject *muscle_init(PyObject *self, PyObject *args)
{
	PyObject *mylist;
	PyArg_ParseTuple(args, "O", &mylist);
	if (!PySequence_Check(mylist))
        MUSCLE_PY_PY_ERR(PyExc_TypeError, "Argument to muscle_init should be sys.argv");

	size_t len;
	char **argv;
    argv = pyListToArray<char *,char *>(mylist, len, "strings sys.argv");
    if (argv == NULL)
        return NULL;
    
    int argc = (int)len;
	MUSCLE_PY_CALL(muscle::env::init(&argc, &argv));
    pyFree<char *>(argv, len);
    
	Py_RETURN_NONE;
}
static PyObject *muscle_finalize(PyObject *self, PyObject *args)
{
	MUSCLE_PY_CALL(muscle::env::finalize());
    Py_RETURN_NONE;
}
static PyObject *muscle_kernel_name(PyObject *self, PyObject *args)
{
	string result;
	MUSCLE_PY_SET(result, cxa::kernel_name());
    return Py_BuildValue("s", result.c_str());
}
static PyObject *muscle_send(PyObject *self, PyObject *args)
{
	const char *port;
	PyObject *data;
	muscle_datatype_t type;
	if (!PyArg_ParseTuple(args, "Osi", &data, &port, &type))
		MUSCLE_PY_ERR("send takes data, a port name, and a MUSCLE datatype");
	
    Py_INCREF(data);
    
	void *result;
	size_t len;
	switch (type) {
		case MUSCLE_STRING:
        {
            result = pyListToArray<char,char>(data, len, "string");
            int sz = (int)strlen((const char *)result);
            int rsz = (int)len;
            logger::info("%d - %d", sz, rsz);
			break;
        }
		case MUSCLE_INT32:
			result = pyListToArray<int,long>(data, len, "integers");
			break;
		case MUSCLE_INT64:
			result = pyListToArray<long,long>(data, len, "long");
			break;
		case MUSCLE_DOUBLE:
			result = pyListToArray<double,double>(data, len, "double");
			break;
		case MUSCLE_FLOAT:
			result = pyListToArray<float,double>(data, len, "float");
			break;
		case MUSCLE_RAW:
			result = pyListToArray<unsigned char,unsigned char>(data, len, "unsigned char");
			break;
		case MUSCLE_BOOLEAN:
			result = pyListToArray<bool,int>(data, len, "boolean");
			break;
		default:
			MUSCLE_PY_ERR("expected one of the MUSCLE types");
	}
    
    if (result == NULL)
        return NULL;
    
	string portStr(port);
	MUSCLE_PY_CALL(muscle::env::send(portStr, result, len, type));
    
    delete [] (char *)result;
    
    Py_DECREF(data);
    
	Py_RETURN_NONE;
}
static PyObject *muscle_receive(PyObject *self, PyObject *args)
{
	const char *port;
	muscle_datatype_t type;
	if (!PyArg_ParseTuple(args, "si", &port, &type))
		MUSCLE_PY_ERR("receive takes a port name and a MUSCLE datatype");
	
	string portStr(port);
	void *data;
    PyObject *result;
	size_t len;
	MUSCLE_PY_SET(data, muscle::env::receive(portStr, (void *)0, len, type));

	switch(type) {
		case MUSCLE_INT32:
            result = pyArrayToList<int,long>(data, len, "int");
            break;
		case MUSCLE_INT64:
            result = pyArrayToList<long,long>(data, len, "long");
            break;
		case MUSCLE_FLOAT:
            result = pyArrayToList<float,double>(data, len, "float");
            break;
		case MUSCLE_DOUBLE:
            result = pyArrayToList<double,double>(data, len, "double");
            break;
		case MUSCLE_RAW:
            result = pyArrayToList<unsigned char,unsigned char>(data, len, "bytearray");
            break;
		case MUSCLE_STRING:
            result = pyArrayToList<char,char>(data, len, "string");
            break;
		case MUSCLE_BOOLEAN:
            result = pyArrayToList<bool,int>(data, len, "boolean");
            break;
		default:
			MUSCLE_PY_ERR("Datatype not recognized");
	}
	MUSCLE_PY_CALL(muscle::env::free_data(data, type));
	
	return result;
}

static PyObject *muscle_tmp_path(PyObject *self, PyObject *args)
{
	string result;
	MUSCLE_PY_SET(result, env::get_tmp_path());
    return Py_BuildValue("s", result.c_str());
}

static PyObject *muscle_log(PyObject *self, PyObject *args)
{
	const char *prop;
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("log expects a log string");

    MUSCLE_PY_CALL(muscle::logger::info(prop));
	Py_RETURN_NONE;
}
static PyObject *muscle_severe(PyObject *self, PyObject *args)
{
	const char *prop;
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("log expects a log string");

	MUSCLE_PY_CALL(muscle::logger::severe(prop));
	Py_RETURN_NONE;
}
static PyObject *muscle_warning(PyObject *self, PyObject *args)
{
	const char *prop;
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("log expects a log string");

	MUSCLE_PY_CALL(muscle::logger::warning(prop));
	Py_RETURN_NONE;
}
static PyObject *muscle_fine(PyObject *self, PyObject *args)
{
	const char *prop;
    if (!PyArg_ParseTuple(args, "s", &prop))
        MUSCLE_PY_ERR("log expects a log string");

	MUSCLE_PY_CALL(muscle::logger::fine(prop));
	Py_RETURN_NONE;
}

/* Converter Functions */

namespace muscle {
    template <typename P>
    inline bool pyCheck(PyObject *obj)
    {   // Must be specialized
        assert(false); return false;
    }
    template <> inline bool pyCheck<long>           (PyObject *obj) { return PyInt_Check(obj);       }
    template <> inline bool pyCheck<double>         (PyObject *obj) { return PyFloat_Check(obj);     }
    template <> inline bool pyCheck<char *>         (PyObject *obj) { return PyString_Check(obj); }
    template <> inline bool pyCheck<unsigned char *>(PyObject *obj) { return PyByteArray_Check(obj);    }
    // Long is used for real ints, so int means bool
    template <> inline bool pyCheck<int>            (PyObject *obj) { return PyBool_Check(obj);      }
    
    template <typename P>
    inline PyObject *pyCreate(P val)
    {   // Must be specialized
        assert(false); return NULL;
    }
    template <> inline PyObject *pyCreate<long>  (long val)   { return PyInt_FromLong(val); }
    template <> inline PyObject *pyCreate<double>(double val) { return PyFloat_FromDouble(val); }
    // Long is used for real ints, so int means bool
    template <> inline PyObject *pyCreate<int>   (int val)    { return PyBool_FromLong(val); }
    
    template <typename P>
    inline P pyConvert(PyObject *obj)
    {   // Must be specialized
        assert(false); return (P)0;
    }
    template <> inline long   pyConvert<long>  (PyObject *obj) { return PyInt_AsLong(obj);      }
    template <> inline double pyConvert<double>(PyObject *obj) { return PyFloat_AsDouble(obj);  }
    // Long is used for real ints, so int means bool
    template <> inline int    pyConvert<int>   (PyObject *obj) { return PyObject_IsTrue(obj);   }
    
    template <> inline char * pyConvert<char *>(PyObject *obj)
    {
        const char *str = PyString_AsString(obj);
        size_t len = strlen(str);
        char *ret = new char[len+1];
        memcpy(ret, str, len);
        ret[len] = '\0';
        return ret;
    }
    template <> inline unsigned char *pyConvert<unsigned char *>(PyObject *obj)
    {
        Py_ssize_t len = PyByteArray_Size(obj);
        unsigned char *ret = new unsigned char[len];
        if (!ret) return NULL;
        memcpy(ret, PyByteArray_AsString(obj), len*sizeof(unsigned char));
        return ret;
    }
    
    template <typename T, typename P>
    T *pyListToArray(PyObject *list, size_t& len, string typestr)
    {
        len = PyObject_Length(list);
        PyObject *item;
        T *arr = new T[len];
        
        for (int i = 0; i < len; i++) {
            item = PySequence_GetItem(list, i);
            if (!pyCheck<P>(item)) {
                pyFree<T>(arr, i);
                Py_DECREF(item);
                string err = "expected sequence of " + typestr;
                MUSCLE_PY_PY_ERR(PyExc_TypeError, err.c_str());
            }
            arr[i] = (T)pyConvert<P>(item);
            Py_DECREF(item);
        }
        return arr;
    }
    template <>
    inline char *pyListToArray<char,char>(PyObject *str, size_t& len, string typestr)
    {
        if (!pyCheck<char *>(str))
            MUSCLE_PY_PY_ERR(PyExc_TypeError, "MUSCLE_STRING expects a str() object");
            
        len = PyString_Size(str);
        return pyConvert<char *>(str);
    }
    template<>
    inline unsigned char *pyListToArray<unsigned char,unsigned char>(PyObject *str, size_t& len, string typestr)
    {
        if (!pyCheck<unsigned char *>(str))
            MUSCLE_PY_PY_ERR(PyExc_TypeError, "MUSCLE_RAW expects a bytearray() object");
            
        len = PyByteArray_Size(str);
        return pyConvert<unsigned char *>(str);
    }
    
    template <typename T, typename P>
    PyObject *pyArrayToList(void *data, size_t len, string typestr)
    {
        PyObject *list;
        if (!(list = PyList_New(len)))
            return NULL;
        
        ssize_t i;
        PyObject *item;
        T *arr = (T *)data;
        for (i = 0; i < len; i++) {
            if (!(item = pyCreate<P>(arr[i])))
                return NULL;
            if (PyList_SetItem(list, i, item) == -1)
                return NULL;
        }
        return list;
    }
    
    template <>
    PyObject *pyArrayToList<char,char>(void *data, size_t len, string typestr)
    {                                                      // Remove the nul byte
        return PyString_FromStringAndSize((const char *)data, len-1);
    }
    template <>
    PyObject *pyArrayToList<unsigned char,unsigned char>(void *data, size_t len, string typestr)
    {
        return PyByteArray_FromStringAndSize((const char *)data, len);
    }
    
    template <typename T>
    inline void pyFree(T *val, size_t len)
    {
        delete [] val;
    }
    template <> inline void pyFree<char *>(char **val, size_t len)
    {
        for (size_t i = 0; i < len; i++) {
            delete [] val[i];
        }
        delete [] val;
    }
    template <> inline void pyFree<unsigned char *>(unsigned char **val, size_t len)
    {
        for (size_t i = 0; i < len; i++) {
            delete [] val[i];
        }
        delete [] val;
    }
}

