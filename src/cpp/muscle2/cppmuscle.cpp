
#include "cppmuscle.hpp"
#include "communicator.hpp"
#include "logger.hpp"
#include "xdr_communicator.hpp"

#include <stdlib.h>
#include <rpc/types.h>
#include <rpc/xdr.h>

#include <unistd.h>
#include <cstdio>
#include <string>
#include <boost/asio.hpp>
#include <boost/lexical_cast.hpp>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/errno.h>
#include <stdlib.h>
#ifdef CPPMUSCLE_TRACE
#include <iostream>
#endif

using boost::asio::ip::tcp;
using namespace std;


namespace muscle {

Communicator *comm;
pid_t muscle_pid;
const char *tmpfifo;

muscle_error_t env::init(int *argc, char ***argv)
{
	using boost::lexical_cast;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::init() " << endl;
#endif
	int rank = env::detect_mpi_rank();
	
	// Only execute for rank 0
	if (rank > 0) return MUSCLE_SUCCESS;
	
	// Initialize host and port on which MUSCLE is listening
	unsigned short port = 0;
	char host_str[16];
	host_str[0] = '\0';
	boost::asio::ip::address_v4 host;	
	muscle_pid = -1;
	
	env::muscle2_tcp_location(muscle_pid, host_str, &port);
	
	// Port is not initialized, initialize MUSCLE instead.
	if (port == 0)
	{
		logger::info("MUSCLE port not given. Starting new MUSCLE instance.");
		muscle_pid = env::muscle2_spawn(argc, argv);
		
		env::muscle2_tcp_location(muscle_pid, host_str, &port);
		if (port == 0)
		{
			logger::severe("Could not contact MUSCLE: no TCP port given.");
			exit(1);
		}
	}
	if (host_str[0] == '\0')
	{
		host = boost::asio::ip::address_v4::loopback();
		strncpy(host_str, "localhost", 10);
	}
	else host = boost::asio::ip::address_v4::from_string(host_str);
	
	// Start communicating with MUSCLE instance
	try
	{
		comm = new XdrCommunicator(host, port);
	} catch (std::exception& e) {
		logger::severe("Could not connect to MUSCLE2 on address tcp://%s:%hu: %s", host_str, port, e.what());
		exit(1);
	}
	return MUSCLE_SUCCESS;
}

void env::finalize(void)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::finalize() " << endl;
#endif
	comm->execute_protocol(PROTO_FINALIZE, NULL, MUSCLE_RAW, NULL, 0, NULL, NULL);
	delete comm;

	if (muscle_pid > 0)
	{
		int status;
		waitpid(muscle_pid, &status, 0);
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
}

int env::detect_mpi_rank() {
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::detect_mpi_rank() " << endl;
#endif
	const std::string possible_mpi_rank_vars[]={"OMPI_MCA_orte_ess_vpid",
							"OMPI_MCA_ns_nds_vpid",
	                        "PMI_RANK",
	                        "MP_CHILD",
	                        "SLURM_PROCID",
	                        "X10_PLACE",
							"MP_CHILD"};
	int irank = 0;
	for (int i = 0; i < 7; i++) {
		char *rank = getenv(possible_mpi_rank_vars[i].c_str());
		if (rank != NULL) {
			irank = atoi(rank);
		}
	}
	return irank;
}

std::string cxa::kernel_name(void)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::kernel_name() " << endl;
#endif
	return comm->retrieve_string(PROTO_KERNEL_NAME, NULL);
}

std::string cxa::get_property(std::string name)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_property(" << name << ") " << endl;
#endif

	std::string prop_value_str = comm->retrieve_string(PROTO_PROPERTY, &name);

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_property[" << name << "] -> " << prop_value_str << endl;
#endif
	return prop_value_str;
}

std::string cxa::get_properties()
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_properties()" << endl;
#endif
	return comm->retrieve_string(PROTO_PROPERTIES, NULL);
}

std::string env::get_tmp_path()
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::get_tmp_path()" << endl;
#endif
	return comm->retrieve_string(PROTO_TMP_PATH, NULL);
}

bool env::will_stop(void)
{
	bool_t is_will_stop = false;
	
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::will_stop()" << endl;
#endif
	comm->execute_protocol(PROTO_WILL_STOP, NULL, MUSCLE_BOOLEAN, NULL, 0, &is_will_stop, NULL);
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::will_stop -> " << is_will_stop << endl;
#endif

	return is_will_stop;
}

void env::send(std::string entrance_name, void *data, size_t count, muscle_datatype_t type)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::send()" << endl;
#endif

	comm->execute_protocol(PROTO_SEND, &entrance_name, type, data, count, NULL, NULL);
}


void* env::receive(std::string exit_name, void *data, size_t& count,  muscle_datatype_t type)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::receive()" << endl;
#endif

	comm->execute_protocol(PROTO_RECEIVE, &exit_name, type, NULL, 0, &data, &count);

	return data;
}

/**
 * Gets the listening port of the created MUSCLE instance.
 * This implementation always returns the fixed port 50210 after a sleep of 10 seconds.
 * @param pid of the created MUSCLE instance
 * @return port number
 * @todo implement some means of retrieving the MUSCLE port number.
 */
void env::muscle2_tcp_location(pid_t pid, char *host, unsigned short *port)
{
	*port = 0;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::muscle2_tcp_location()" << endl;
#endif
	
	if (pid < 0)
	{
		char *port_str = getenv("MUSCLE_GATEWAY_PORT");
		if (port_str != NULL) {
			*port = boost::lexical_cast<unsigned short>(port_str);
			char *host_str = getenv("MUSCLE_GATEWAY_HOST");
			if (host_str != NULL) {
				cout << "Host: " << host_str << endl;
				strncpy(host, host_str, 16);
			}
		}
	}
	else
	{
		FILE *fp = fopen(tmpfifo, "r");
		if(fp == 0 || ferror(fp))
		{	
			logger::severe("Could not open temporary file %s", tmpfifo);
			return;
		}
		while (fscanf(fp, "%15[^:]:%hu", host, port) == EOF) {
			sleep(1);
		}
		fclose(fp);
		// Null terminated
		//assert( strlen(host, 16) < 16 );
  
		logger::info("Will communicate with Java MUSCLE on %s:%d", host, *port);
	}
}

char * env::create_tmpfifo()
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::create_tmpfifo()" << endl;
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
pid_t env::muscle2_spawn(int* argc, char ***argv)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::muscle2_spawn()" << endl;
#endif

	pid_t pid = -2;
	string term_str = "--";
	int term = -1;
	
	// Find terminator
	for (int i = 1; i < *argc; i++) {
		if (term_str.compare((*argv)[i]) == 0) {
			term = i;
			break;
		}
	}
	
	if (term == -1)
	{
		logger::severe("Could not instantiate MUSCLE: no command line arguments given.");			
		exit(1);
	}
	
	tmpfifo = env::create_tmpfifo();
	
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
	argv_new[argc_new - 2] = tmpfifo;
	argv_new[argc_new - 1] = NULL;

	// Spawn Java MUSCLE
	pid = env::spawn((char * const *)argv_new);
	free(argv_new);
	
	// Make MUSCLE arguments unavailable to the calling program.
	*argc = term;
	
	return pid;
}
		

/**
 * Spawn a new process with arguments argv. argv[0] is the process name.
 * @return the pid of the created process.
 */
pid_t env::spawn(char * const *argv)
{
	pid_t pid;
	
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::spawn(" << argv[0] << ")" << endl;
#endif
	pid = fork();
	if (pid == -1)
	{
		logger::severe("Could not start new Java MUSCLE instance: fork failed. Aborting.");
		exit(1);
	}
	
	// Child process: execute
	if (pid == 0) {
		int rc = execvp(argv[0], argv);
		if (rc == -1)
		{
			logger::severe("Executable muscle2 not found in the PATH. Aborting.");
			_exit(1);
		}
	}
	return pid;
}

void env::free_data(void *ptr, muscle_datatype_t type)
{
	comm->free_data(ptr, type);
}


} // EO namespace muscle
