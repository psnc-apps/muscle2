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
/**
 * Get the nth subsection of argv, separated by given separator.
 * When n = 0, argv before the first separator is returned. The separator is never included in the resulting array.
 * @return length of the returned array
 * @param result the resulting array
 * @param argv the original array
 * @param len length of the original array
 * @param separator the separator
 * @param n the section that will be returned
 */
int array_between_separators(char * const **result, char * const *argv, int len, const char *separator, int n);

/**
 * Copies the next part of the string, up to a given character, into a buffer.
 * The buffer is not guarded against buffer overflow. If there is no match, the entire string is copied into the buffer.
 * @return pointer to the original string after the character that has been matched, or NULL if the character is not matched
 * @param buf buffer to hold the string before the character and a nul character; ensure that it is large enough
 * @param str the original string
 * @param chr the character that will be matched
 */
const char *strnext(char *buf, const char *str, int chr);

/**
 * Detects the MPI rank using only environment variables.
 * @return the MPI rank, or -1 if cannot be determined
 */
int detect_mpi_rank();

/**
 * Detect the MPI rank on the current node
 * @param rank the global MPI rank of the process
 * @param node if non-null, stores the node number
 * @return the MPI rank on the current node
 */
int detect_local_mpi_rank(const int rank, int *node);

/**
 * Creates the arguments for a single instance executable.
 *
 * The arguments are a combination of a first given argument, which is the executable name, then global arguments,
 * node-specific arguments, and finally a null pointer. The original arguments must be in the form
 * global_arg0 global_arg1 -- local_node0_arg0 local_node0_arg1 -- local_node1_arg0 -- etc...
 * If the only local argument for a node is ---, it is ignored, if there are no more -- separators so that the nodes
 * arguments can be parsed, the node is also ignored.
 * @param result the result will be allocated and stored in the result, it must be freed by the caller
 * @param first_arg the name of the executable
 * @param argc the number of arguments given
 * @param argv the arguments given to the program, excluding the program name
 * @param node the node number 
 * @return the length of the arguments, or -1 if the node should not execute anything.
 */
int create_args(char *** result, const char *first_arg, int argc, char * const * argv, int node);
