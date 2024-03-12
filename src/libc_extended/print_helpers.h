#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "string_list_helpers.h"

// Forward declaration to allow println_list to call println_dynamic
void print_dynamic(void *);

/** Print a bool without newline */
static inline void print_bool(long long value)
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
static inline void println_bool(long long value)
{
    print_bool(value);
    putchar('\n');
}

/** Print an integer value without newline */
static inline void print_int64(long long value)
{
    printf("%lld", value);
}

/** Print an integer value */
static inline void println_int64(long long value)
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
static inline void print_dynamic_list(void *value)
{
    char type = *((char *)value);
    if (type == 3)
    {
        putchar('\'');
        print_string(get_string_value(value));
        putchar('\'');
    }
    else
    {
        print_dynamic(value);
    }
}

/** Print a list without a newline. The full object must be passed */
static inline void print_list(void *value)
{
    putchar('[');

    long size = *((long long *)(value + 1 + 8)); // Offset type + ref_count

    if (size != 0)
    {
        for (int i = 0; i < size - 1; i++)
        {
            print_dynamic_list(*((void **)(value + 1 + 8 + 8 + i * 8)));
            putchar(',');
            putchar(' ');
        }

        // Print the last element
        print_dynamic_list(*((void **)(value + 1 + 8 + 8 + (size - 1) * 8)));
    }

    putchar(']');
}

/** Print a list. The full object must be passed */
static inline void println_list(void *value)
{
    print_list(value);
    putchar('\n');
}

/** Print a dynamic value without a newline. This function reads the 1st byte of the value and decides how to print it.
 */
inline void print_dynamic(void *value)
{
    char type = *((char *)value);
    switch (type)
    {
    case 0:
        print_none();
        break;
    case 1:
        print_bool(*((long long *)(value + 1 + 8))); // Offset type + ref_count
        break;
    case 2:
        print_int64(*((long long *)(value + 1 + 8))); // Offset type + ref_count
        break;
    case 3:
        print_string(get_string_value(value)); // Offset type + ref_count + string length
        break;
    case 4:
        print_list(value);
        break;
    default:
        printf("Unknown dynamic type: %d\n", type);
        exit(1);
    }
}
