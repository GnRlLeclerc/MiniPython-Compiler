#pragma once

#include "types.h"

/** Get a string representation of an integer type for printing to the console */
static inline const char *type_label(char type)
{
    switch (type)
    {
    case 0:
        return "NoneType";
    case 1:
        return "bool";
    case 2:
        return "int64";
    case 3:
        return "string";
    case 4:
        return "list";
    default:
        return "unknown";
    }
}

/** Extract the type value from a value */
static inline char type_value(DYN_VALUE value)
{
    return *((char *)value);
}

/** Get a string representation of a value's type for printing to the console */
static inline const char *value_label(DYN_VALUE value)
{
    return type_label(type_value(value));
}

/** Combine 2 byte types by multiplying the first by 8 and adding the second.
 * This is a fast way to switch through all possible combinations of types.
 * Because our tags do not go over 4 bits, the result fits in a byte too.
 */
static inline char combined_type(DYN_VALUE value1, DYN_VALUE value2)
{
    return (type_value(value1) << 3) + type_value(value2);
}
