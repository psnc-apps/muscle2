//
//  main.c
//  MuscleMPIStarter
//
//  Created by Joris Borgdorff on 2/26/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <assert.h>

/**
 * Get the nth subsection of argv separated by given separator.
 * When n = 0, argv before the first separator is returned. The separator is never included in the resulting array.
 * @return length of the returned array
 * @param result the resulting array
 * @param argv the original array
 * @param len length of the original array
 * @param separator the separator
 * @param n the section that will be returned
 */
int array_between_separators(char * const **result, char * const *argv, const int len, const char *separator, const int n)
{
    *result = NULL;
    int resultlen = -1, i, found = 0, found_idx = 0;
    for (i = 0; i < len; i++) {
        if (strcmp(argv[i], separator) == 0) {
            if (n == found) {
                *result = &argv[found_idx];
                resultlen = i - found_idx;
                break;
            }
            found++;
            found_idx = i + 1;
        }
    }
    // Belongs to last separator
    if (*result == NULL && n == found) {
        *result = &argv[found_idx];
        resultlen = len - found_idx;
    }
    return resultlen;
}

/**
 * Copies the next part of the string, up to a given character, into a buffer.
 * The buffer is not guarded against buffer overflow. If there is no match, the entire string is copied into the buffer.
 * @return pointer to the original string after the character that has been matched, or NULL if the character is not matched
 * @param buf buffer to hold the string before the character and a nul character; ensure that it is large enough
 * @param str the original string
 * @param chr the character that will be matched
 */
const char *strnext(char *buf, const char *str, int chr)
{
    const char *newstr = strchr(str, chr);
    if (newstr) {
        size_t len = newstr - str;
        memcpy(buf, str, len);
        buf[len] = '\0';
        return newstr + 1;
    } else {
        strcpy(buf, str);
        return NULL;
    }
}

/**
 * Detects the MPI rank using only environment variables.
 * @return the MPI rank, or -1 if can not be determined
 */
int detect_mpi_rank() {
	const char *possible_mpi_rank_vars[]={
        "OMPI_MCA_orte_ess_vpid",
        "OMPI_MCA_ns_nds_vpid",
        "OMPI_COMM_WORLD_RANK",
        "MV2_COMM_WORLD_RANK",
        "MPISPAWN_ID",
        "PMI_RANK",
        "MP_CHILD",
        "SLURM_PROCID",
        "X10_PLACE"};
	int irank = -1;
    size_t i;
    const size_t len = sizeof(possible_mpi_rank_vars)/sizeof(const char *);
    for (i = 0; i < len; i++) {
		const char *rank = getenv(possible_mpi_rank_vars[i]);
		if (rank != NULL) {
			irank = atoi(rank);
            break;
		}
	}
	return irank;
}


int detect_local_mpi_rank(const int rank, int *node) {
    int ilrank = -1, ilsize;
    const char *lrank, *lsize;
    lrank = getenv("OMPI_COMM_WORLD_LOCAL_RANK");
    lsize = getenv("OMPI_COMM_WORLD_LOCAL_SIZE");
    if (lrank == NULL || lsize == NULL)
    {
        lrank = getenv("MV2_COMM_WORLD_LOCAL_RANK");
        lsize = getenv("MV2_COMM_WORLD_LOCAL_SIZE");
    }
    if (lsize == NULL) {
        lsize = getenv("MPISPAWN_LOCAL_NPROCS");
    }
    
    if (lsize != NULL)
    {
        ilsize = atoi(lsize);
        ilrank = lrank == NULL ? rank % ilsize : atoi(lrank);
    }
    else
    {
        lsize = getenv("MP_COMMON_TASKS");
        if (lsize != NULL)
        {
            char number[11];
            int i;
            const char *rank_ptr = strnext(number, lsize, ':');
            ilsize = atoi(number) + 1;
            ilrank = 0;

            for (i = 1; i < ilsize && rank_ptr != NULL; i++)
            {
                rank_ptr = strnext(number, rank_ptr, ':');
                const int otherrank = atoi(number);
                if (otherrank < rank) ilrank++;
            }
        }
    }
    if (node != NULL && ilrank != -1) *node = rank / ilsize;
    
    return ilrank;
}

int create_args(char ***result, const char *first_arg, int argc, char * const *argv, int node)
{
    char * const *local_args;
    char * const *global_args;

    int global_len = array_between_separators(&global_args, argv+1, argc-1, "--", 0);
    assert(global_len >= 0);
    int local_len = array_between_separators(&local_args, argv+1, argc-1, "--", node + 1);

    if (local_len <= 0)
    {
        printf("Node %d does not have any work\n", node);
        return -1;
    }
    else if (local_len == 1 && strncmp("---", local_args[0], 3) == 0)
    {
        local_len = 0;
    }
    
    int len = global_len + local_len + 2;
    assert(len >= 2);

    *result = malloc(sizeof(char *)*len);
    if (*result == NULL)
    {
        fprintf(stderr, "Could not allocate variable");
        free(*result);
        return -1;
    }
    
    **result = strdup(first_arg);
    memcpy(*result+1, global_args, global_len*sizeof(char *));
    if (local_len > 0)
        memcpy(*result+1+global_len, local_args, local_len*sizeof(char *));
    (*result)[len - 1] = NULL;
    
    return len;
}

int main(int argc, char * argv[])
{
    int node;
    int rank = detect_mpi_rank();
    if (rank == -1) {
        fprintf(stderr, "Can not determine MPI rank, is MPI being used?\n");
        return 1;
    }
    int lrank = detect_local_mpi_rank(rank, &node);
    if (lrank == -1) {
        fprintf(stderr, "Can not determine local MPI rank, please send an email with your MPI distribution to info@mapper-project.eu.\n");
        return 1;
    }
    printf("%d: local rank: %d (node: %d)\n", rank, lrank, node);
    if (lrank == 0) {
        int i;
        char **args;
        
        int len = create_args(&args, "muscle2", argc, argv, node);
        if (len == -1)
            return 0;

        printf("%d: node parameters: ", rank);
        for (i = 0; i < len - 1; i++) {
            printf("%s ", args[i]);
        }
        printf("\n");
        fflush(stdout);

        execvp("muscle2", args);

        // We are still here after exec, so it failed.
        const char *reason = strerror(errno);
        fprintf(stderr, "Calling MUSCLE failed: %s\n", reason);
        free(args[0]);
        free(args);
    }
    fflush(stdout);
}
