#ifndef muscle_types_H
#define muscle_types_H

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
	MUSCLE_DOUBLE,
	MUSCLE_FLOAT,
	MUSCLE_INT32,
	MUSCLE_INT64,
	MUSCLE_STRING,
	MUSCLE_BOOLEAN,
	MUSCLE_RAW,
	MUSCLE_COMPLEX
} muscle_datatype_t;


typedef enum {
	MUSCLE_LOG_OFF = 2147483647,
	MUSCLE_LOG_SEVERE = 1000,
	MUSCLE_LOG_WARNING = 900,
	MUSCLE_LOG_INFO = 800,
	MUSCLE_LOG_CONFIG = 700,
	MUSCLE_LOG_FINE = 500,
	MUSCLE_LOG_FINER = 400,
	MUSCLE_LOG_FINEST = 300,
	MUSCLE_LOG_ALL = 0
} muscle_loglevel_t; /* one to one mapping to Java logger */

typedef enum {
	MUSCLE_SUCCESS = 0,
	MUSCLE_ERR_INTERNAL,
	MUSCLE_ERR_NOMEM,
	MUSCLE_ERR_NET,
	MUSCLE_ERR_IO,
	MUSCLE_ERR_SER,
	MUSCLE_ERR_DESER
} muscle_error_t; /* error codes for initialization */

#ifdef __cplusplus
}
#endif

#endif /* muscle_types_H */
