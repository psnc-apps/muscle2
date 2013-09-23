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

#include "cppmuscle.hpp"
#include "xdr_communicator.hpp"
#include "util/exception.hpp"
#include "util/csocket.h"

#include <stdlib.h>
#include <rpc/types.h>
#include <rpc/xdr.h>
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

namespace muscle {

bool env::is_main_processor = true;
ServerSocket *env::barrier_ssock = NULL;
static Communicator *comm = NULL;
pid_t env::muscle_pid = -1;
std::string env::kernel_name, env::tmp_path;
ServerSocket *env::barrier_ssock = NULL;

muscle_error_t env::init(int *argc, char ***argv)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::init() ");
#endif
	int rank = env::detect_mpi_rank();
	
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
		logger::info("MUSCLE port not given. Starting new MUSCLE instance.");
        
        char *muscle_tmpfifo;
		muscle_pid = env::muscle2_spawn(argc, argv, &muscle_tmpfifo);
		env::install_sighandler();
		ep = env::muscle2_tcp_location(muscle_pid, muscle_tmpfifo);
        free(muscle_tmpfifo);
        
		if (ep.port == 0)
		{
			logger::severe("Could not contact MUSCLE: no TCP port given.");
			exit(1);
		}
	}
	
	// Start communicating with MUSCLE instance
	try
	{
		ep.resolve();
		comm = new XdrCommunicator(ep);
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
			"SLURM_PROCID" };

	int irank = 0;
	const size_t len = sizeof(possible_mpi_rank_vars)/sizeof(const char *);
	for (size_t i = 0; i < len; i++) {
		const char *rank = getenv(possible_mpi_rank_vars[i]);
		if (rank != NULL) {
			irank = atoi(rank);
			break;
		}
	}
	return irank;
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

	bool_t is_will_stop = false;
	
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

#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::send()");
#endif

	comm->execute_protocol(PROTO_SEND, &entrance_name, type, data, count, NULL, NULL);
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

#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::receive()");
#endif

	comm->execute_protocol(PROTO_RECEIVE, &exit_name, type, NULL, 0, &data, &count);

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
	*len = endpoint::getSize() + sizeof(int);
	char *buf = new char[*len];
	int *buf_intptr = (int *)buf;
	*buf_intptr = num_procs;
	if (env::is_main_processor) {
		socket_opts opts(num_procs);
		// Bind to any port
		try {
			endpoint ep((uint16_t)0);
			ep.resolve();
			barrier_ssock = new muscle::CServerSocket(ep, NULL, opts);
			barrier_ssock->setBlocking(true);
			barrier_ssock->getAddress().serialize(buf + sizeof(int));
		} catch (const muscle_exception& ex) {
			const char *msg = ex.what();
			logger::severe("Could not initialize MUSCLE barrier: %s", msg);
			if (barrier_ssock) {
				delete barrier_ssock;
				barrier_ssock = NULL;
			}
			return -1;
		}
	}
	*barrier = buf;
	
	return 0;
}

int env::barrier(const char * const barrier)
{
#ifdef CPPMUSCLE_TRACE
	logger::finest("muscle::env::barrier()");
#endif
	const int num_procs = *(int*)barrier;
	if (num_procs < 1) {
		logger::warning("Will not do barrier with less than 2 processes");
		return 0;
	}
	
	const char *msg = NULL;
	socket_opts opts;
	
	if (env::is_main_processor) {
		// start at 1: the main processor is already counted
		try {
			const char data = 1;
			for (int i = 1; i < num_procs; i++) {
				ClientSocket *sock = barrier_ssock->accept(opts);
				sock->setBlocking(true);
				if (sock == NULL) {
					msg = "failed to accept socket";
				} else {
					const int hasErr = sock->hasError();
					if (hasErr) {
						msg = strerror(hasErr);
					} else {
						sock->send(&data, 1);
					}
				}
				delete sock;
			}
		} catch (muscle::muscle_exception& ex) {
			msg = ex.what();
		}
		if (msg)
			logger::severe("muscle::env::barrier failed to connect socket: %s", msg);
	} else {
		endpoint address(barrier + sizeof(int));
		address.resolve();
		const char * const addr_str = address.str().c_str();
		char data;
		try {
			opts.blocking_connect = true;
			muscle::CClientSocket sock(address, NULL, opts);
			sock.setBlocking(true);
			const int hasErr = sock.hasError();
			if (hasErr) {
				msg = strerror(hasErr);
			} else {
				sock.recv(&data, 1);
			}
		} catch (muscle::muscle_exception& ex) {
			msg = ex.what();
		}
		if (msg)
			printf("ERROR: muscle::env::barrier failed to connect socket: %s\n", msg);
	}
	
	return msg == NULL ? 0 : -1;
}

void env::barrier_destroy(char * const barrier)
{
	if (env::is_main_processor) {
		delete barrier_ssock;
		barrier_ssock = NULL;
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
			logger::severe("Can not create temporary file; error %s", strerror(errno));
			return NULL;
		}
	}
	if (!tmppath)
	{
		logger::severe("Can not create temporary file.");
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
	
	if (term == -1)
	{
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

void env::free_data(void *ptr, muscle_datatype_t type)
{
	// No error: simply ignore send in all MPI processes except 0
	// we did not create any data in other ranks to free.
	if (!env::is_main_processor) return;
	if (comm == NULL) throw muscle_exception("cannot call MUSCLE functions without initializing MUSCLE");
	
	comm->free_data(ptr, type);
}


} // EO namespace muscle

