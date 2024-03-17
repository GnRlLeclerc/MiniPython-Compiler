/**
 * Allocation helper functions.
 *
 * These inline functions are used to allocate memory on the heap for minipython's
 * different dynamic types, and pre-fills the type tag and reference count fields.
 */

#pragma once

#include "types.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/** Allocate an empty bool value */
static inline DYN_VALUE allocate_bool()
{
    DYN_VALUE value = (DYN_VALUE)malloc(1 + 8 + 8); // 1 byte tag, 8 bytes ref_count, 8 bytes value
    *((char *)value) = BOOL;
    *((int64 *)(value + 1)) = 0; // ref_count (0 by default for a temporary value)
    return value;
}

/** Allocate an empty int64 value */
static inline DYN_VALUE allocate_int64()
{
    DYN_VALUE value = (DYN_VALUE)malloc(1 + 8 + 8); // 1 byte tag, 8 bytes ref_count, 8 bytes value
    *((char *)value) = INT64;
    *((int64 *)(value + 1)) = 0; // ref_count (0 by default for a temporary value)
    return value;
}

/** Allocate an empty string value with a given size */
static inline DYN_VALUE allocate_string(int64 size)
{
    int64 capacity = size * 2 + 7; // Allocate a bit more than the size to avoid reallocating too often

    DYN_VALUE value = (DYN_VALUE)malloc(1 + 8 + 8 + 8 + capacity + 1); // 1 byte tag, 8 bytes ref_count, 8 bytes size, 8 bytes capacity, size bytes string
    *((char *)value) = STRING;                                         // Set the tag
    *((int64 *)(value + 1)) = 0;                                       // ref_count (0 by default for a temporary value)
    *((int64 *)(value + 1 + 8)) = size;                                // string size
    *((int64 *)(value + 1 + 8 + 8)) = capacity;                        // string capacity (allocated size)

    // null terminate the string
    *((char *)(value + 1 + 8 + 8 + 8 + size)) = '\0';
    return value;
}

/** Allocate an empty list value with a given size */
static inline DYN_VALUE allocate_list(int64 size)
{
    DYN_VALUE value = (DYN_VALUE)malloc(1 + 8 + 8 + size * 8); // 1 byte tag, 8 bytes ref_count, 8 bytes size, size bytes string
    *((char *)value) = LIST;                                   // Set the tag
    *((int64 *)(value + 1)) = 0;                               // ref_count (0 by default for a temporary value)
    *((int64 *)(value + 1 + 8)) = size;                        // list size
    return value;
}
