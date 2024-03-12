#include <string.h>
#include "alloc_helpers.h"
#include "type_helpers.h"
#include "int_helpers.h"

#ifndef INCLUDE_STRING_HELPERS
#define INCLUDE_STRING_HELPERS

/** Get size of a dynamic value assumed to be a list or a string.*/
static inline long long get_size(void *value)
{
    return *((long long *)(value + 1 + 8));
}

/** Get capacity of a dynamic value assumed to be a string.*/
static inline long long get_capacity(void *value)
{
    return *((long long *)(value + 1 + 8 + 8));
}

static inline char *get_string_value(void *value)
{
    return (char *)(value + 1 + 8 + 8 + 8);
}

static inline void *add_string_helper(void *value1, void *value2, void *result)
{
    // Copy the first string
    strcpy(get_string_value(result), get_string_value(value1));
    // Append the second string
    strcat(get_string_value(result), get_string_value(value2));

    return result;
}

static inline void *add_list_helper(void *value1, void *value2)
{
    long long size1 = get_size(value1);
    long long size2 = get_size(value2);
    long long size = size1 + size2;
    void *result = allocate_list(size);

    // Copy the first list
    for (long long i = 0; i < size1; i++)
    {
        *((void **)(result + 1 + 8 + 8 + i * 8)) = *((void **)(value1 + 1 + 8 + 8 + i * 8));
        *((long long *)(*((void **)(result + 1 + 8 + 8 + i * 8)) + 1)) += 1;
    }

    // Copy the second list
    for (long long i = 0; i < size2; i++)
    {
        *((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) = *((void **)(value2 + 1 + 8 + 8 + i * 8));
        *((long long *)(*((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) + 1)) += 1;
    }

    return result;
}

/** Computes the list address corresponding to the index.
 * Does the type and boundary checks
 */
static inline void **list_index(void *list, void *index)
{
    // 1. Check that the index has the right type
    if (type_value(index) != INT64 && type_value(index) != BOOL)
    {
        printf("TypeError: list indices must be integers, not %s\n", value_label(index));
        exit(1);
    }

    // 2. Check that the index is within the list bounds
    long long list_size = *((long long *)(list + 1 + 8));
    long long index_value = *((long long *)(index + 1 + 8));

    if (index_value < 0 || index_value >= list_size)
    {
        printf("IndexError: list assignment index %lld out of range for size %lld\n", index_value, list_size);
        exit(1);
    }

    return list + 1 + 8 + 8 + index_value * 8;
}

static inline void mul_string_int_helper(void *value1, void *value2, void *result)
{
    long long value2_int = get_int_value(value2);
    if (value2_int > 0)
    {
        void *value1_string = get_string_value(value1);
        void *result_string = get_string_value(result);

        // Copy the first string
        strcpy(result_string, value1_string);

        for (long long i = 1; i < value2_int; i++)
        {
            strcat(result_string, value1_string);
        }
    }
}

static inline void *mul_list_int_helper(void *value1, void *value2)
{
    void *result;
    if (*((long long *)(value2 + 1 + 8)) > 0)
    {
        long long size = *((long long *)(value1 + 1 + 8)) * *((long long *)(value2 + 1 + 8));
        result = allocate_list(size);

        for (long long i = 0; i < *((long long *)(value2 + 1 + 8)); i++)
        {
            for (long long j = 0; j < *((long long *)(value1 + 1 + 8)); j++)
            {
                *((void **)(result + 1 + 8 + 8 + i * *((long long *)(value1 + 1 + 8)) * 8 + j * 8)) = *((void **)(value1 + 1 + 8 + 8 + j * 8));
                *((long long *)(*((void **)(result + 1 + 8 + 8 + i * *((long long *)(value1 + 1 + 8)) * 8 + j * 8)) + 1)) += 1;
            }
        }
    }
    else
    {
        result = allocate_list(0);
    }
    return result;
}

#endif
