#include <stdio.h>
#include <stdlib.h>

/** Print the input value (int64 representation of a boolean) in Python boolean format */
void println_bool(long long value)
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
void println_int64(long long value)
{
    printf("%lld\n", value);
}

/** Print a string value */
void println_string(const char *value)
{
    printf("%s\n", value);
}

/** Print None */
void println_none()
{
    printf("None\n");
}

/** Allocate the given size or exit with an error.
 * We will use this function in order to avoid working with NULL pointers.
 */
void *alloc_or_panic(size_t size)
{
    void *ptr = malloc(size);

    if (ptr != NULL)
    {
        return ptr;
    }

    printf("Failed allocation error\n");
    exit(1);
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
        println_bool(*((long long *)(value + 1)));
        break;
    case 2:
        println_int64(*((long long *)(value + 1)));
        break;
    case 3:
        println_string((char *)(value + 1));
        break;
    case 4:
        printf("TODO: print list");
        break;
    default:
        printf("Unknown dynamic type: %d\n", type);
        exit(1);
    }
}
