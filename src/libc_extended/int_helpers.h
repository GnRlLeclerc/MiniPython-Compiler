#pragma once

#include "types.h"
#include <stdio.h>
#include <stdlib.h>

/** Get the integer value of a dynamic value that is assumed to be an integer.*/
static inline int64 get_int_value(DYN_VALUE value)
{
    return *((int64 *)(value + 1 + 8)); // Type tag + refcount offset
}

/** Set the integer value of a dynamic value that is assumed to be an integer..*/
static inline void set_int_value(DYN_VALUE value, int new_value)
{
    *((int64 *)(value + 1 + 8)) = new_value;
}

static inline void add_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    set_int_value(result, get_int_value(value1) + get_int_value(value2));
}

static inline void sub_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    set_int_value(result, get_int_value(value1) - get_int_value(value2));
}

static inline void mul_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    set_int_value(result, get_int_value(value1) * get_int_value(value2));
}

static inline void div_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    if (get_int_value(value2) == 0)
    {
        printf("ZeroDivisionError: division by zero\n");
        exit(1);
    }
    set_int_value(result, get_int_value(value1) / get_int_value(value2));
}

static inline void mod_int_helper(DYN_VALUE value1, DYN_VALUE value2, DYN_VALUE result)
{
    if (get_int_value(value2) == 0)
    {
        printf("ZeroDivisionError: modulo by zero\n");
        exit(1);
    }
    set_int_value(result, get_int_value(value1) % get_int_value(value2));
}
