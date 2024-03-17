#pragma once

#include "alloc_helpers.h"
#include "int_helpers.h"
#include "type_helpers.h"
#include "types.h"
#include <stdio.h>
#include <stdlib.h>

/** Get size of a dynamic value assumed to be a list or a string.*/
static inline int64 get_size(DYN_VALUE value)
{
    return *((int64 *)(value + 1 + 8)); // Type tag + refcount offset
}

/** Get capacity of a dynamic value assumed to be a string.*/
static inline int64 get_capacity(DYN_VALUE value)
{
    return *((int64 *)(value + 1 + 8 + 8));
}

static inline char *get_string_value(DYN_VALUE value)
{
    return (char *)(value + 1 + 8 + 8 + 8);
}

static inline DYN_VALUE add_string_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    // Copy the first string if we are not doing this in place
    if (result != value1)
    {
        strcpy(get_string_value(result), get_string_value(value1));
    }

    // Append the second string
    strcat(get_string_value(result), get_string_value(value2));

    return result;
}

static inline DYN_VALUE add_list_helper(DYN_VALUE value1, DYN_VALUE value2)
{
    int64 size1 = get_size(value1);
    int64 size2 = get_size(value2);
    int64 size = size1 + size2;
    DYN_VALUE result = allocate_list(size);

    // Copy the first list
    for (int64 i = 0; i < size1; i++)
    {
        *((DYN_ARRAY)(result + 1 + 8 + 8 + i * 8)) = *((DYN_ARRAY)(value1 + 1 + 8 + 8 + i * 8));
        *((int64 *)(*((DYN_ARRAY)(result + 1 + 8 + 8 + i * 8)) + 1)) += 1;
    }

    // Copy the second list
    for (int64 i = 0; i < size2; i++)
    {
        *((DYN_ARRAY)(result + 1 + 8 + 8 + (*((int64 *)(value1 + 1 + 8)) + i) * 8)) = *((DYN_ARRAY)(value2 + 1 + 8 + 8 + i * 8));
        *((int64 *)(*((DYN_ARRAY)(result + 1 + 8 + 8 + (*((int64 *)(value1 + 1 + 8)) + i) * 8)) + 1)) += 1;
    }

    return result;
}

/** Computes the list address corresponding to the index.
 * Does the type and boundary checks
 */
static inline DYN_ARRAY list_index(DYN_ARRAY list, DYN_VALUE index)
{
    // 1. Check that the index has the right type
    if (type_value(index) != INT64 && type_value(index) != BOOL)
    {
        printf("TypeError: list indices must be integers, not %s\n", value_label(index));
        exit(1);
    }

    // 2. Check that the index is within the list bounds
    int64 list_size = *((int64 *)(list + 1 + 8));
    int64 index_value = get_int_value(index);

    if (index_value < 0 || index_value >= list_size)
    {
        printf("IndexError: list assignment index %lld out of range for size %lld\n", index_value, list_size);
        exit(1);
    }

    return list + 1 + 8 + 8 + index_value * 8;
}

static inline void mul_string_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    int64 value2_int = get_int_value(value2);
    if (value2_int > 0)
    {
        char *value1_string = get_string_value(value1);
        char *result_string = get_string_value(result);

        // Copy the first string
        strcpy(result_string, value1_string);

        for (int64 i = 1; i < value2_int; i++)
        {
            strcat(result_string, value1_string);
        }
    }

    // If the second value is 0, the result is an empty string
    else
    {
        set_int_value(result, 0);
        *((char *)(result + 1 + 8 + 8 + 8)) = '\0'; // Null-terminated string
    }
}

static inline DYN_VALUE mul_list_int_helper(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result;
    if (get_int_value(value2) > 0)
    {
        int64 size = get_int_value(value1) * get_int_value(value2);
        result = allocate_list(size);

        for (int64 i = 0; i < get_int_value(value2); i++)
        {
            for (int64 j = 0; j < *((int64 *)(value1 + 1 + 8)); j++)
            {
                *((DYN_ARRAY)(result + 1 + 8 + 8 + i * get_int_value(value1) * 8 + j * 8)) = *((DYN_ARRAY)(value1 + 1 + 8 + 8 + j * 8));
                *((int64 *)(*((DYN_ARRAY)(result + 1 + 8 + 8 + i * get_int_value(value1) * 8 + j * 8)) + 1)) += 1;
            }
        }
    }
    else
    {
        result = allocate_list(0);
    }
    return result;
}
