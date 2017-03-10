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

//#define USE_XDR
//#define CPPMUSCLE_TRACE

#include "cppmuscle.hpp"
#include "util/exception.hpp"
#include "util/csocket.h"
#include "util/barrier.h"

#ifdef USE_XDR
#include "xdr_communicator.hpp"
#else
#include "custom_communicator.h"
#endif

#ifdef CPPMUSCLE_PERF
#include "muscle_perf.h"
#include <time.h> // TODO OSX alternative?
#include <assert.h>
#endif //CPPMUSCLE_PERF

#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <string>
#include <cstring>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/errno.h>
#include <sys/select.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <signal.h>

using namespace std;
using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;
	
bool env::is_main_processor = true;
Barrier *env::barrier_server = NULL;
BarrierClient *env::barrier_client = NULL;
static Communicator *comm = NULL;
pid_t env::muscle_pid = -1;
std::string env::kernel_name, env::tmp_path;

#ifdef CPPMUSCLE_PERF
struct muscle_perf_t env::muscle_perf_send;
struct muscle_perf_t env::muscle_perf_receive;
struct muscle_perf_t env::muscle_perf_barrier;
struct timespec env::muscle_perf_last_call_start_time = {0};
bool env::muscle_perf_is_in_call = false;
muscle_perf_counter_t env::muscle_perf_current_call_id = MUSCLE_PERF_COUNTER_LAST;
std::string env::muscle_perf_string("");
#endif //CPPMUSCLE_PERF

muscle_error_t env::init(int *argc, char ***argv)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::init() ");
#endif
	int rank = env::detect_mpi_rank();

#ifdef CPPMUSCLE_PERF
        //Initialise the perf counters
	env::reset_perf_counters();
#endif //CPPMUSCLE_PERF

	// Only execute for rank 0
	if (rank > 0) {
		is_main_processor = false;
		logger::initialize(NULL, NULL, MUSCLE_LOG_OFF, false);
		return MUSCLE_SUCCESS;
	}
	is_main_processor = true;
	
	if (atexit(muscle::env::muscle2_kill) != 0) {
		logger::severe("Will not be able to stop MUSCLE at a later point. Aborting.");
		muscle::env::muscle2_kill();
		exit(3);
	}
	
	// Initialize host and port on which MUSCLE is listening
	muscle_pid = -1;
	
	endpoint ep = env::muscle2_tcp_location(muscle_pid, NULL);
	
	// Port is not initialized, initialize MUSCLE instead.
	if (ep.port == 0)
	{
		if (argc == NULL || argv == NULL) {
			logger::severe("No arguments provided in MUSCLE initialization method so MUSCLE cannot be started.");
			exit(1);
		}
		logger::info("MUSCLE port not given. Starting new MUSCLE instance.");
        
		char *muscle_tmpfifo;
		muscle_pid = env::muscle2_spawn(argc, argv, &muscle_tmpfifo);
		env::install_sighandler();
		ep = env::muscle2_tcp_location(muscle_pid, muscle_tmpfifo);
		free(muscle_tmpfifo);

		if (ep.port == 0) {
			logger::severe("Could not contact MUSCLE: no TCP port given.");
			exit(1);
		}
	}
	
	// Start communicating with MUSCLE instance
	try
	{
		ep.resolve();
		char *reconnect_str = getenv("MUSCLE_GATEWAY_RECONNECT");
		bool reconnect = reconnect_str ? atoi(reconnect_str) > 0 : false;
#ifdef USE_XDR
		comm = new XdrCommunicator(ep, reconnect);
#else
		comm = new CustomCommunicator(ep, reconnect);
#endif
	} catch (muscle_exception& e) {
		logger::severe("Could not connect to MUSCLE2 on address tcp://%s", ep.str().c_str());
		exit(1);
	}
	
	int log_level;
	kernel_name = comm->retrieve_string(PROTO_KERNEL_NAME, NULL);
	tmp_path = comm->retrieve_string(PROTO_TMP_PATH, NULL);
	comm->execute_protocol(PROTO_LOG_LEVEL, NULL, MUSCLE_INT32, NULL, 0, &log_level, NULL);
	logger::initialize(kernel_name.c_str(), tmp_path.c_str(), log_level, is_main_processor);
	return MUSCLE_SUCCESS;
}


void env::reset_perf_counters()
{
#ifdef CPPMUSCLE_PERF
	muscle_perf_send.duration = 0;
	muscle_perf_send.calls = 0;
	muscle_perf_send.size  = 0;

	muscle_perf_receive.duration = 0;
	muscle_perf_receive.calls = 0;
	muscle_perf_receive.size  = 0;

	muscle_perf_barrier.duration = 0;
	muscle_perf_barrier.calls = 0;
	muscle_perf_barrier.size  = 0;
#endif //CPPMUSCLE_PERF
}



void env::finalize(void)
{
	if (!is_main_processor) return;
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::finalize() ");
#endif
	comm->execute_protocol(PROTO_FINALIZE, NULL, MUSCLE_RAW, NULL, 0, NULL, NULL);
    delete comm;
    comm = NULL;
    
	if (muscle_pid > 0)
	{
		int status;
		waitpid(muscle_pid, &status, 0);
		muscle_pid = -1;
		if (WIFEXITED(status))
		{
			if (WEXITSTATUS(status)) logger::severe("MUSCLE execution failed with status %d.", WEXITSTATUS(status));
		}
		else if (WIFSIGNALED(status))
		{
			logger::severe("MUSCLE execution terminated by signal %s.", strsignal(WTERMSIG(status)));
		}
		else logger::severe("MUSCLE failed");
	}
    kernel_name = string();
    tmp_path = string();
    is_main_processor = true;
}

int env::detect_mpi_rank() {
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::detect_mpi_rank() ");
#endif
	const char * const possible_mpi_rank_vars[] = {
		"OMPI_COMM_WORLD_RANK",
		"MV2_COMM_WORLD_RANK",
		"MPISPAWN_ID",
		"MP_CHILD",
		"PMI_RANK",
		"X10_PLACE",
		"OMPI_MCA_orte_ess_vpid",
		"OMPI_MCA_ns_nds_vpid",
		"SLURM_PROCID",
		"LAMRANK",
		"GMPI_ID"
	};

	const size_t len = sizeof(possible_mpi_rank_vars)/sizeof(const char *);
	for (size_t i = 0; i < len; i++) {
		const char *rank = getenv(possible_mpi_rank_vars[i]);
		if (rank != NULL) {
#ifdef CPPMUSCLE_TRACE
			const int irank = atoi(rank);
			logger::finest("muscle::env::detect_mpi_rank(): %d (detected through %s)", irank, rank);
#endif
			return atoi(rank);
		}
	}
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::detect_mpi_rank() not detected");
#endif
	return 0;
}

std::string cxa::kernel_name(void)
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::cxa::kernel_name() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::cxa::kernel_name() ");
#endif
	return env::kernel_name;
}

std::string cxa::get_property(std::string name)
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::cxa::get_property() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::cxa::get_property(%s) ", name.c_str());
#endif

	std::string prop_value_str = comm->retrieve_string(PROTO_PROPERTY, &name);

#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::cxa::get_property(%s) = %s", name.c_str(), prop_value_str.c_str());
#endif
	return prop_value_str;
}

bool cxa::has_property(std::string name)
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::cxa::get_property() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::cxa::has_property(%s) ", name.c_str());
#endif
	bool has_prop = false;
	comm->execute_protocol(PROTO_HAS_PROPERTY, &name, MUSCLE_BOOLEAN, NULL, 0, &has_prop, NULL);

#ifdef CPPMUSCLE_TRACE
	const char *bool_str = has_prop ? "true" : "false";
	logger::finest("muscle::cxa::has_property(%s) = %s", name.c_str(), bool_str);
#endif
	return has_prop;
}


std::string cxa::get_properties()
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::cxa::get_properties() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::cxa::get_properties()");
#endif
	return comm->retrieve_string(PROTO_PROPERTIES, NULL);
}

bool env::has_next(std::string port)
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::env::has_next() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");

	bool is_has_next;

#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::has_next(%s)", port.c_str());
#endif
	comm->execute_protocol(PROTO_HAS_NEXT, &port, MUSCLE_BOOLEAN, NULL, 0, &is_has_next, NULL);
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::has_next -> %d", is_has_next);
#endif
  
  return is_has_next;
}

std::string env::get_tmp_path()
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::env::get_tmp_path() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::get_tmp_path()");
#endif
	return tmp_path;
}

bool env::will_stop(void)
{
	if (!env::is_main_processor) throw muscle_exception("can only call muscle::env::will_stop() from main MPI processor (MPI rank 0)");
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");

	bool is_will_stop = false;

#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::will_stop()");
#endif
	comm->execute_protocol(PROTO_WILL_STOP, NULL, MUSCLE_BOOLEAN, NULL, 0, &is_will_stop, NULL);
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::will_stop -> %d", is_will_stop);
#endif

	return is_will_stop;
}

void env::send(std::string entrance_name, const void *data, size_t count, muscle_datatype_t type)
{

	// No error: simply ignore send in all MPI processes except 0.
	if (!env::is_main_processor) return;
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");

#ifdef CPPMUSCLE_PERF
	muscle_perf_is_in_call = true;
	muscle_perf_current_call_id = MUSCLE_PERF_COUNTER_SEND_DURATION;
	struct timespec time_start = {0, 0}, time_end = {0, 0};
	clock_gettime(CLOCK_MONOTONIC, &time_start); // start timing
	muscle_perf_last_call_start_time = time_start;
#endif //CPPMUSCLE_PERF
#ifdef CPPMUSCLE_TRACE
	{
	const char *entrance_str = entrance_name.c_str();
	logger::finest("muscle::env::send(%s, len(%zu))", entrance_str, count);
	}
#endif

	comm->execute_protocol(PROTO_SEND, &entrance_name, type, data, count, NULL, NULL);

#ifdef CPPMUSCLE_PERF
	clock_gettime(CLOCK_MONOTONIC, &time_end); // finish timing
	muscle_perf_send.duration += duration_ns(time_start, time_end);
	muscle_perf_send.calls++;
	muscle_perf_send.size     += count;
	muscle_perf_is_in_call     = false;
#endif //CPPMUSCLE_PERF
}

void env::sendDoubleVector(std::string entrance_name, const std::vector<double>& data)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::send()");
#endif

	env::send(entrance_name, &data[0], data.size(), MUSCLE_DOUBLE);
}

void* env::receive(std::string exit_name, void *data, size_t& count,  muscle_datatype_t type)
{
	// No error: simply ignore receive in all MPI processes except 0.
	if (!env::is_main_processor) return (void *)0;
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");


#ifdef CPPMUSCLE_PERF
	muscle_perf_is_in_call = true;
	muscle_perf_current_call_id = MUSCLE_PERF_COUNTER_RECEIVE_DURATION;
	struct timespec time_start = {0, 0}, time_end = {0, 0};
	clock_gettime(CLOCK_MONOTONIC, &time_start); // start timing
	muscle_perf_last_call_start_time = time_start;
#endif //CPPMUSCLE_PERF


#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::receive()");
#endif

	comm->execute_protocol(PROTO_RECEIVE, &exit_name, type, NULL, 0, &data, &count);

#ifdef CPPMUSCLE_PERF
	clock_gettime(CLOCK_MONOTONIC, &time_end); // finish timing
	muscle_perf_receive.duration += duration_ns(time_start, time_end);
	muscle_perf_receive.size  += count;
	muscle_perf_receive.calls++;
	muscle_perf_is_in_call = false;
#endif //CPPMUSCLE_PERF

	return data;
}

std::vector<double> env::receiveDoubleVector(std::string exit_name)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::receive()");
#endif
	size_t sz;
	double *ddata = (double *)env::receive(exit_name, (void *)0, sz, MUSCLE_DOUBLE);
	if (ddata)
	{
		// data received or in main processor
		std::vector<double> data(ddata, ddata + sz);
		env::free_data(ddata, MUSCLE_DOUBLE);
		return data;
	} else
	{
		std::vector<double> data;
		return data;
	}
}

/**
 * Gets the listening port of the created MUSCLE instance.
 * This implementation always returns the fixed port 50210 after a sleep of 10 seconds.
 * @param pid of the created MUSCLE instance
 * @return port number
 */
endpoint env::muscle2_tcp_location(const pid_t pid, const char *muscle_tmpfifo)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::muscle2_tcp_location()");
#endif
	
	if (pid < 0)
	{
		char *port_str = getenv("MUSCLE_GATEWAY_PORT");
		unsigned short port = port_str ? (unsigned short)atoi(port_str) : 0;
        
		const char *host = getenv("MUSCLE_GATEWAY_HOST");
        if (!host)
            host = "localhost";

        return endpoint(string(host), port);
	}
	else
	{
		char host[272];

		logger::fine("Reading Java MUSCLE contact info from FIFO %s", muscle_tmpfifo);
		
		int fd = open(muscle_tmpfifo, O_RDONLY|O_NONBLOCK);
        bool succeeded = false;
        struct timeval timeout;
        
        while (!succeeded) {
            timeout.tv_sec = 0;
            timeout.tv_usec = 200000; // 0.2 sec

            fd_set fd_read, fd_err;
            FD_ZERO(&fd_read); FD_ZERO(&fd_err);
            FD_SET(fd, &fd_read); FD_SET(fd, &fd_err);
            int res = select(fd+1, &fd_read, (fd_set *)0, &fd_err, &timeout);
            if (res == -1 || FD_ISSET(fd, &fd_err)) {
                logger::severe("Could not read temporary file %s.", muscle_tmpfifo);
                break;
            }
            if (res && FD_ISSET(fd, &fd_read)) {
                FILE *fp = fdopen(fd, "r");
                if(fp == NULL || ferror(fp))
                {
                    logger::severe("Could not open temporary file %s", muscle_tmpfifo);
                    break;
                }
                char *cs = fgets(host, 272, fp);
                if (cs == NULL) {
                    logger::severe("Could not read temporary file %s", muscle_tmpfifo);
                    fclose(fp);
                    fd = -1;
                    break;
                } else if (fgetc(fp) != EOF) {
                    logger::severe("Specified host name '%272s...' is too long", host);
                    fclose(fp);
                    fd = -1;
                    break;
                }
                succeeded = true;
            } else {
                int status;
                int pid = waitpid(muscle_pid, &status, WNOHANG);
                if (pid == muscle_pid) {
                    if (WIFEXITED(status)) {
                        if (WEXITSTATUS(status)) logger::severe("MUSCLE execution failed with status %d.", WEXITSTATUS(status));
                    } else if (WIFSIGNALED(status)) {
                        logger::severe("MUSCLE execution terminated by signal %s.", strsignal(WTERMSIG(status)));
                    }
                    else logger::severe("MUSCLE failed");
                    muscle_pid = -1;
                    break;
                }
            }
        }
        if (fd != -1) close(fd);

        if (!succeeded)
            return endpoint();
		
		char *indexColon = strrchr(host, ':');
		if (indexColon == NULL) {			
			logger::severe("Host name should be specified as hostName:port, not like '%s'", host);
			return endpoint();
		} else if (indexColon - host >= 256) {
			logger::severe("Specified host name '%255s...' is too long", host);
			return endpoint();
		}
		*indexColon++ = '\0';
        unsigned short port;
		if (sscanf(indexColon, "%hu", &port) != 1) {
			logger::severe("Port is not correctly specified, it should be a number less than 65536, not like '%s'", indexColon);
			return endpoint();
		}
        
        endpoint ep(string(host), port);
  
		logger::config("Will communicate with Java MUSCLE on %s:%hu", ep.str().c_str());
        
        return ep;
	}
}

int env::barrier_init(char **barrier, size_t *len, const int num_procs)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::barrier_init()");
#endif
	if (num_procs < 2) {
		logger::warning("Will not create barrier with less than 2 processes");
		return -1;
	}
	
	*len = Barrier::createBuffer(barrier);
	
	if (env::is_main_processor) {
		const bool logFine = logger::isLoggable(MUSCLE_LOG_FINE);
		if (logFine) logger::fine("Creating MUSCLE barrier");
		// Don't count the server as a connecting process
		barrier_server = new Barrier(num_procs - 1);
		if (logFine) logger::finer("Retrieving MUSCLE barrier address");
		barrier_server->fillBuffer(*barrier);
		if (logFine) {
			endpoint ep(*barrier);
			ep.resolve();
			logger::fine("MUSCLE barrier address is %s", ep.str().c_str());
		}
	}
	return 0;
}

int env::barrier(const char * const barrier)
{
#ifdef CPPMUSCLE_PERF
	muscle_perf_is_in_call = true;
	muscle_perf_current_call_id = MUSCLE_PERF_COUNTER_BARRIER_DURATION;
#endif //CPPMUSCLE_PERF
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::barrier()");
#endif //CPPMUSCLE_TRACE
#ifdef CPPMUSCLE_PERF
	struct timespec time_start = {0, 0}, time_end = {0, 0};
	clock_gettime(CLOCK_MONOTONIC, &time_start); // start timing
	muscle_perf_last_call_start_time = time_start;
#endif //CPPMUSCLE_PERF

	if (env::is_main_processor) {
		if (barrier_server) {
			barrier_server->signal();
			logger::fine("Passed MUSCLE barrier");
		} else {
			logger::warning("Will pass uninitialized MUSCLE barrier.");
		}
	} else {
		if (barrier_client == NULL) barrier_client = new BarrierClient(barrier);

		barrier_client->wait();
	}

#ifdef CPPMUSCLE_PERF
	clock_gettime(CLOCK_MONOTONIC, &time_end); // finish timing
	muscle_perf_barrier.duration += duration_ns(time_start, time_end);
	muscle_perf_barrier.calls++;
	muscle_perf_is_in_call = false;
#endif //CPPMUSCLE_PERF
	return 0;
}

void env::barrier_destroy(char * const barrier)
{
	if (barrier_client) {
		logger::finer("Destroying MUSCLE barrier client");
		delete barrier_client;
		barrier_client = NULL;
	} else if (barrier_server) {
		logger::fine("Destroying MUSCLE barrier");
		delete barrier_server;
		barrier_server = NULL;
	}
	delete [] barrier;
}

char *env::create_tmpfifo()
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::create_tmpfifo()");
#endif

	char *tmppath;
	while ((tmppath = tempnam(NULL, NULL)))
	{
		if (mknod(tmppath, S_IFIFO|0600, 0) == 0)
			break;
		if (errno != EEXIST)
		{
			logger::severe("Cannot create temporary file; error %s", strerror(errno));
			return NULL;
		}
	}
	if (!tmppath)
	{
		logger::severe("Cannot create temporary file.");
		return NULL;
	}
	
	return tmppath;
}

/**
 * Spawn a new Java MUSCLE, using all arguments after "--" in the programs arguments.
 * For this to work, muscle2 must be in the PATH. All MUSCLE arguments, including "--"
 * will be removed from the arguments of the caller.
 * @param argc pointer to the number of arguments of the main function.
 * @param argv pointer to the arguments of the main function.
 * @return pid of the created MUSCLE instance.
 */
pid_t env::muscle2_spawn(int* argc, char ***argv, char **muscle_tmpfifo)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::muscle2_spawn()");
#endif

	pid_t pid = -2;
	const char *term_str = "--";
	int term = -1;
	
	// Find terminator
	for (int i = 0; i < *argc; i++) {
		if (strcmp(term_str, (*argv)[i]) == 0) {
			term = i;
			break;
		}
	}
	
	if (term == -1) {
		logger::severe("Could not instantiate MUSCLE: no command line arguments given.");			
		exit(1);
	}
	
	*muscle_tmpfifo = env::create_tmpfifo();
	
	// Size:                           1            | i - 1                     | 1    | *argc - (i + 1)          | 3
	// Number of arguments for muscle: muscle2      +                                    all arguments after "--" + tmparg + tmpdir + null terminator
	// Number of arguments given:      muscleKernel + all arguments before "--" + "--" + all arguments after "--"
	// Number of arguments returned:   muscleKernel + all arguments before "--"
	int new_args = 3;
	int argc_new = *argc + new_args - term;
	
	const char ** argv_new = (const char **)malloc(argc_new*sizeof(char *));
	argv_new[0] = "muscle2";
	// Copy all arguments after "--"
	memcpy(&argv_new[1], &(*argv)[term+1], (argc_new - (new_args + 1))*sizeof(char *));
	argv_new[argc_new - 3] = "--native-tmp-file";
	argv_new[argc_new - 2] = *muscle_tmpfifo;
	argv_new[argc_new - 1] = NULL;

	// Spawn Java MUSCLE
	pid = env::spawn((char * const *)argv_new);
	free(argv_new);
	
	// Make MUSCLE arguments unavailable to the calling program.
	*argc = term;
	
	return pid;
}

/** Kills the current invocation of MUSCLE, if any. */
void env::muscle2_kill(void)
{
	int status, pid, i = 0;
	
	// Just an if, with a simplified break
	while (muscle_pid != -1) {		
		// Correctly quit
		if (muscle_pid > 0)
		{
			if ((pid = waitpid(muscle_pid, &status, WNOHANG)) > 0) {
				muscle_pid = -1;
				break;
			}
		} else {
			muscle_pid = -muscle_pid;
		}
		logger::info("Stopping Java MUSCLE (pid=%u).", muscle_pid);

		if (muscle_pid < 0) {
			logger::warning("MUSCLE did not quit correctly.");
			muscle_pid = -1;
			break;
		}
		
		if (kill(muscle_pid, SIGINT) != 0) {
			logger::warning("Java MUSCLE could not be stopped.");
			muscle_pid = -1;
			break;
		}
		
		// Wait for MUSCLE until a) MUSCLE has exit or b) 30 seconds have passed.
		while ((pid = waitpid(muscle_pid, &status, WNOHANG)) == 0 && i < 150) {
			// 200.000 us = 0.2 s
			usleep(200000);
			if (++i % 25 == 0) {
				printf("#");
				fflush(stdout);
			}
		}
		printf("\n");
		
		// Correctly quit
		if (pid > 0) {
			logger::info("Java MUSCLE stopped.");
			muscle_pid = -1;
			break;
		}
		if (pid < 0) {
			logger::warning("MUSCLE did not quit correctly.");
			muscle_pid = -1;
			break;
		}
		
		logger::info("Forcing Java MUSCLE to stop (pid=%u).", muscle_pid);
		// Second call to SIGINT will cause Ruby to force kill java.
		kill(muscle_pid, SIGINT);
		muscle_pid = -1;
	}
	
	logger::info("Program finished.");
	logger::finalize();
}

void env::muscle2_sighandler(int signal) {
	logger::severe("Signal received: %s", strsignal(signal));
	// SIGINT gets forwarded, ignore it the first time
	if (signal == SIGINT) {
		muscle_pid = -muscle_pid;
	}
	env::muscle2_kill();
	
	struct sigaction act;
	act.sa_handler = SIG_DFL;
	sigemptyset (&act.sa_mask);
	act.sa_flags = 0;
	sigaction(signal, &act, NULL);
	kill(getpid(),signal);
}

void env::install_sighandler(void)
{
	int fatalsigs[] = {SIGHUP, SIGINT, SIGQUIT, SIGILL, SIGABRT, SIGFPE, SIGBUS, SIGSEGV, SIGSYS, SIGPIPE, SIGALRM, SIGTERM, SIGTTIN, SIGTTOU, SIGXCPU, SIGXFSZ, SIGPROF, SIGUSR1, SIGUSR2};
	struct sigaction act;
	act.sa_handler = muscle::env::muscle2_sighandler;
	sigemptyset (&act.sa_mask);
	act.sa_flags = 0;

	for (int i = 0; i < sizeof(fatalsigs)/4; i++) {
		if (sigaction(fatalsigs[i],&act,NULL) != 0) {
			logger::severe("Will not be able to stop MUSCLE when a signal arrives. Aborting.");
			exit(4);
		}
	}
}

/**
 * Spawn a new process with arguments argv. argv[0] is the process name.
 * @return the pid of the created process.
 */
pid_t env::spawn(char * const *argv)
{
	int pipefd[2];
	int res = pipe(pipefd);
	if (res == -1)
	{
		logger::severe("Could not start new Java MUSCLE instance: pipe failed. Aborting.");
		exit(1);
	}
	pid_t pid;
	
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::spawn(%s)", argv[0]);
#endif
	pid = fork();
	if (pid == -1)
	{
		logger::severe("Could not start new Java MUSCLE instance: fork failed. Aborting.");
		exit(1);
	}
	
	// Child process: execute
	if (pid == 0)
	{
		// We're not going to read
		close (pipefd[0]);
		// And the writing end should be closed if the exec succeeds
		fcntl(pipefd[1], F_SETFD, FD_CLOEXEC);

		execvp(argv[0], argv);
		// execvp should not return; if it does, then MUSCLE was not found
		logger::severe("Executable muscle2 not started: %s", strerror( errno ));
		const char c = 1;
		// No use checking result; if we can't communicate, neither _exit nor exit is going to do anything
		if (write(pipefd[1], &c, 1) < 1) {
			logger::severe("Could not propagate error to native code; it will stall.\n\nIt is safe to kill me now.");
		}
		close(pipefd[1]);
		_exit(1);
	} 
	// Parent process: check if the execute succeeded
	else
	{
		// We're not going to write
		close(pipefd[1]);
		char buffer[1];
		if (read(pipefd[0],buffer,1)<=0) {
			// Could not write to pipe, so the exec succeeded
			close(pipefd[0]);
			return pid;
		} else {
			// We've aborted
			logger::severe("Aborting.");
			exit(1);
		}
	}
}

#ifdef CPPMUSCLE_PERF
/** Writes a performance metric value denoted by id to the location pointed to by value. 
 *  @param [in]  id enum denoting a counter
 *  @param [out] value pointer to 64-bit int to write to
 *  @return 0 on success, -1 on failure. */
int env::get_perf_counter(muscle_perf_counter_t id, uint64_t *value)
{
	const int SUCCESS = 0;
	const int FAILURE = -1;
	switch (id) {
	case MUSCLE_PERF_COUNTER_SEND_CALLS: 
		*value = env::muscle_perf_send.calls;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_SEND_DURATION:
		*value = env::muscle_perf_send.duration;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_SEND_SIZE:
		*value = env::muscle_perf_send.size;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_RECEIVE_CALLS: 
		*value = env::muscle_perf_receive.calls;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_RECEIVE_DURATION:
		*value = env::muscle_perf_receive.duration;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_RECEIVE_SIZE:
		*value = env::muscle_perf_receive.size;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_BARRIER_CALLS:
		*value = env::muscle_perf_barrier.calls;
		return SUCCESS;
	case MUSCLE_PERF_COUNTER_BARRIER_DURATION:
		*value = env::muscle_perf_barrier.duration;
		return SUCCESS;
	default:
		assert(false);
		*value = -1;
		return FAILURE;
	}
}

/** Returns the name of the performance counter. */
const string& env::get_perf_label(muscle_perf_counter_t counter_id)
{
	switch (counter_id) {
	case MUSCLE_PERF_COUNTER_SEND_CALLS: 
		static const string send_calls("number of send calls");
		return send_calls;
	case MUSCLE_PERF_COUNTER_SEND_DURATION:
		static const string send_duration("time spent in send calls (ns)");
		return send_duration;
	case MUSCLE_PERF_COUNTER_SEND_SIZE:
		static const string send_size("total number of bytes sent");
		return send_size;
	case MUSCLE_PERF_COUNTER_RECEIVE_CALLS: 
		static const string receive_calls("number of receive calls");
		return receive_calls;
	case MUSCLE_PERF_COUNTER_RECEIVE_DURATION:
		static const string receive_duration("time spent in receive calls (ns)");
		return receive_duration;
	case MUSCLE_PERF_COUNTER_RECEIVE_SIZE:
		static const string receive_size("total number of bytes received");
		return receive_size;
	case MUSCLE_PERF_COUNTER_BARRIER_CALLS:
		static const string barrier_calls("number of barrier calls");
		return barrier_calls;
	case MUSCLE_PERF_COUNTER_BARRIER_DURATION:
		static const string barrier_duration("time spent in barrier calls (ns)");
		return barrier_duration;
	default:
		assert(false);
		static const string invalid("invalid counter id!");
		return invalid;
	}
}

/* Returns a statically allocated char *, big enough to hold a pretty print of all the performance counters. 
 * It is only valid until the next call to this function. */
const char * env::get_perf_string(void)
{
	env::muscle_perf_string.clear();
	for (int i = 0; i < MUSCLE_PERF_COUNTER_LAST; i++) {
		muscle_perf_counter_t counter_id = static_cast<muscle_perf_counter_t>(i);
		const string& label = env::get_perf_label(counter_id);
		uint64_t value;
		bool result = env::get_perf_counter(counter_id, &value);
		if (result != 0) {
			return "ERROR: failed to get counter value";
		}
		env::muscle_perf_string += label;
		env::muscle_perf_string += ": ";
		ostringstream o; // pre C++11 way of converting uint64_t to std::string
		o << value;
		env::muscle_perf_string += o.str();
		env::muscle_perf_string += "\n";
	}
	return env::muscle_perf_string.c_str();
}

/** Writes the timestamp of the start of the last send/receive/barrier call to start_time, and the duration counter
 *  id of the call to @a id (e.g. MUSCLE_PERF_COUNTER_BARRIER_DURATION, if we're in a barrier call). 
 *  If MUSCLE2 is not in an API call, sets both @a start_time and @a id to NULL.
 *  \param [out] start_time   location to contain the start of the last API call. Ignored if NULL
 *  \param [out] id           location to contain the duration id of the current API call. Ignored if NULL
 *  \return true if MUSCLE is inside an API call (send, receive, barrier) */
bool env::is_in_call(struct timespec *start_time, muscle_perf_counter_t *id)
{
	if (env::muscle_perf_is_in_call) {
		*start_time = env::muscle_perf_last_call_start_time;

		if (start_time) {
			*start_time = env::muscle_perf_last_call_start_time;
		}
		if (id) {
			assert(muscle_perf_current_call_id < MUSCLE_PERF_COUNTER_LAST);
			*id = env::muscle_perf_current_call_id;
		}

		return true;
	}
	start_time = NULL;
	id = NULL;
	return false;
}

/** Helper function to get the time difference between two <time.h> timespec structs in nanoseconds */
inline uint64_t env::duration_ns(const struct timespec &start, const struct timespec &end)
{
	const uint64_t SEC_TO_NS = 1000000000;
	uint64_t start_nanosec   = start.tv_sec * SEC_TO_NS + start.tv_nsec;
	uint64_t end_nanosec     = end.tv_sec * SEC_TO_NS + end.tv_nsec;
	return (end_nanosec - start_nanosec);
}
#endif //CPPMUSCLE_PERF

void env::free_data(void *ptr, muscle_datatype_t type)
{
	// No error: simply ignore send in all MPI processes except 0
	// we did not create any data in other ranks to free.
	if (!env::is_main_processor) return;
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");

	comm->free_data(ptr, type);
}
