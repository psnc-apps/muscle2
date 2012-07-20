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
	MUSCLE_LOG_SEVERE,
	MUSCLE_LOG_WARNING,
	MUSCLE_LOG_INFO,
	MUSCLE_LOG_CONFIG,
	MUSCLE_LOG_FINE,
	MUSCLE_LOG_FINER,
	MUSCLE_LOG_FINEST
} muscle_loglevel_t; /* one to one mapping to Java logger */

typedef enum {
	MUSCLE_SUCCESS = 0,
	MUSCLE_ERR_INTERNAL,
	MUSCLE_ERR_NOMEM,
	MUSCLE_ERR_NET,
	MUSCLE_ERR_IO,
	MUSCLE_ERR_SER,
	MUSCLE_ERR_DESER,
} muscle_error_t; /* one to one mapping to Java logger */

#ifdef __cplusplus
}
#endif

#endif /* muscle_types_H */
