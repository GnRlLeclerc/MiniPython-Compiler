#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "types.h"

#ifndef INCLUDE_PRINT_HELPERS
#define INCLUDE_PRINT_HELPERS

/** Allocate an empty bool value */
static inline void *allocate_bool()
{
    void *value = malloc(1 + 8 + 8); // 1 byte tag, 8 bytes ref_count, 8 bytes value
    *((char *)value) = BOOL;
    *((long long *)(value + 1)) = 0; // ref_count (0 by default for a temporary value)
    return value;
}

/** Allocate an empty int64 value */
static inline void *allocate_int64()
{
    void *value = malloc(1 + 8 + 8); // 1 byte tag, 8 bytes ref_count, 8 bytes value
    *((char *)value) = INT64;
    *((long long *)(value + 1)) = 0; // ref_count (0 by default for a temporary value)
    return value;
}

/** Allocate an empty string value with a given size */
static inline void *allocate_string(long long size)
{
    long long capacity = size * 2 + 7;
    void *value = malloc(1 + 8 + 8 + 8 + capacity + 1); // 1 byte tag, 8 bytes ref_count, 8 bytes size, 8 bytes capacity, size bytes string
    *((char *)value) = STRING;                  // Set the tag
    *((long long *)(value + 1)) = 0;            // ref_count (0 by default for a temporary value)
    *((long long *)(value + 1 + 8)) = size;     // string size
    *((long long *)(value + 1 + 8 + 8)) = capacity;     // string size
    // null terminate the string
    *((char *)(value + 1 + 8 + 8 + 8 + size)) = '\0';
    return value;
}

/** Allocate an empty list value with a given size */
static inline void *allocate_list(long long size)
{
    void *value = malloc(1 + 8 + 8 + size * 8); // 1 byte tag, 8 bytes ref_count, 8 bytes size, size bytes string
    *((char *)value) = LIST;                    // Set the tag
    *((long long *)(value + 1)) = 0;            // ref_count (0 by default for a temporary value)
    *((long long *)(value + 1 + 8)) = size;     // string size
    return value;
}

#endif
