#include <stdio.h>
#include <stdlib.h>

// WARNING: inline functions are abstracted away and cannot be called by other modules, like our asm code !

/** Print the input value (int64 representation of a boolean) in Python boolean format */
inline void println_bool(long long value)
{
    if (value == 0)
    {
        printf("False\n");
    }
    else
    {
        printf("True\n");
    }
}

/** Print an integer value */
inline void println_int64(long long value)
{
    printf("%lld\n", value);
}

/** Print a string value */
inline void println_string(const char *value)
{
    printf("%s\n", value);
}

/** Print None */
inline void println_none()
{
    printf("None\n");
}

/** Print a dynamic value. This function reads the 1st byte of the value and decides how to print it.
 */
void println_dynamic(void *value)
{
    char type = *((char *)value);
    switch (type)
    {
    case 0:
        println_none();
        break;
    case 1:
        println_bool(*((long long *)(value + 1 + 8))); // Offset type + ref_count
        break;
    case 2:
        println_int64(*((long long *)(value + 1 + 8))); // Offset type + ref_count
        break;
    case 3:
        println_string((char *)(value + 1 + 8 + 8)); // Offset type + ref_count + string length
        break;
    case 4:
        printf("TODO: print list");
        break;
    default:
        printf("Unknown dynamic type: %d\n", type);
        exit(1);
    }
}
