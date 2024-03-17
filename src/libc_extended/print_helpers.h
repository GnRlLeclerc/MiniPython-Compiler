#pragma once

#include "int_helpers.h"
#include "type_helpers.h"
#include "types.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Forward declaration to allow println_list to call println_dynamic
void print_dynamic(DYN_VALUE);

/** Print a bool without newline */
static inline void print_bool(int64 value)
{
    if (value == 0)
    {
        printf("False");
    }
    else
    {
        printf("True");
    }
}

/** Print the input value (int64 representation of a boolean) in Python boolean format */
static inline void println_bool(int64 value)
{
    print_bool(value);
    putchar('\n');
}

/** Print an integer value without newline */
static inline void print_int64(int64 value)
{
    printf("%lld", value);
}

/** Print an integer value */
static inline void println_int64(int64 value)
{
    print_int64(value);
    putchar('\n');
}

/** Print a string value without newline */
static inline void print_string(const char *value)
{
    printf("%s", value);
}

/** Print a string value */
static inline void println_string(const char *value)
{
    print_string(value);
    putchar('\n');
}

/** Print None without a newline */
static inline void print_none()
{
    printf("None");
}

/** Print None */
static inline void println_none()
{
    print_none();
    putchar('\n');
}

/** Wrapper for print inline adding quotes around strings in lists. */
static inline void print_dynamic_list(DYN_VALUE value)
{
    char type = type_value(value);
    if (type == STRING)
    {
        putchar('\'');
        print_string(value + 1 + 8 + 8 + 8);
        putchar('\'');
    }
    else
    {
        print_dynamic(value);
    }
}

/** Print a list without a newline. The full object must be passed */
static inline void print_list(DYN_VALUE value)
{
    putchar('[');

    int64 size = *(int64 *)(value + 1 + 8);

    if (size != 0)
    {
        for (int i = 0; i < size - 1; i++)
        {
            print_dynamic_list(*((DYN_ARRAY)(value + 1 + 8 + 8 + i * 8)));
            putchar(',');
            putchar(' ');
        }

        // Print the last element
        print_dynamic_list(*((DYN_ARRAY)(value + 1 + 8 + 8 + (size - 1) * 8)));
    }

    putchar(']');
}

/** Print a list. The full object must be passed */
static inline void println_list(DYN_VALUE value)
{
    print_list(value);
    putchar('\n');
}

/** Print a dynamic value without a newline. This function reads the 1st byte of the value and decides how to print it.
 */
inline void print_dynamic(DYN_VALUE value)
{
    char type = type_value(value);
    switch (type)
    {
    case 0:
        print_none();
        break;
    case 1:
        print_bool(get_int_value(value)); // Offset type + ref_count
        break;
    case 2:
        print_int64(get_int_value(value)); // Offset type + ref_count
        break;
    case 3:
        print_string(value + 1 + 8 + 8 + 8); // Offset type + ref_count + string length
        break;
    case 4:
        print_list(value);
        break;
    default:
        printf("Unknown dynamic type: %d\n", type);
        exit(1);
    }
}
