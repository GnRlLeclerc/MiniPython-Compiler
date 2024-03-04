#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define NONETYPE 0
#define BOOL 1
#define INT64 2
#define STRING 3
#define LIST 4

// Type combinations using tag1 * 3 + tag2
#define BOOL_BOOL 9
#define BOOL_INT64 10
#define INT64_BOOL 17
#define INT64_INT64 18

#define STRING_STRING 27

// WARNING: static inline functions are abstracted away and cannot be called by other modules, like our asm code !

// ************************************************** PRINTING ****************************************************** //

/** Print the input value (int64 representation of a boolean) in Python boolean format */
static inline void println_bool(long long value)
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
static inline void println_int64(long long value)
{
    printf("%lld\n", value);
}

/** Print a string value */
static inline void println_string(const char *value)
{
    printf("%s\n", value);
}

/** Print None */
static inline void println_none()
{
    printf("None\n");
}

static inline char *type_label(char type)
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

static inline char *value_label(void *value)
{
    return type_label(*((char *)value));
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

// ************************************************* ALLOCATION ***************************************************** //

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

// ************************************************ COMPUTATION ***************************************************** //

/** Allocate an empty string value with a given size */
static inline void *allocate_string(long long size)
{
    void *value = malloc(1 + 8 + 8 + size); // 1 byte tag, 8 bytes ref_count, 8 bytes size, size bytes string
    *((char *)value) = STRING;              // Set the tag
    *((long long *)(value + 1)) = 0;        // ref_count (0 by default for a temporary value)
    *((long long *)(value + 1 + 8)) = size; // string size
    return value;
}

/** Combine 2 byte types by multiplying the first by 8 and adding the second.
 * This is a fast way to switch through all possible combinations of types.
 * Because our tags do not go over 4 bits, the result fits in a byte too.
 */
static inline char combined_type(void *value1, void *value2)
{
    return (*((char *)value1) << 3) + *((char *)value2);
}

/** Add two dynamic values. If the types are not compatible, the program will exit with an error.
 */
void *add_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    void *result = NULL;

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        // TODO: detect if one of the input values is a temporary one, and reuse it
        // TODO: garbage collect the other input value if both are temporary
        result = allocate_int64();
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) + *((long long *)(value2 + 1 + 8));
        break;

    case STRING_STRING:
    {
        // TODO: garbage collect the 2 operands. We allocate a new string.
        // Compute output size
        long long size = *((long long *)(value1 + 1 + 8)) + *((long long *)(value2 + 1 + 8));
        result = allocate_string(size);

        // Copy the first string
        strcpy((char *)(result + 1 + 8 + 8), (char *)(value1 + 1 + 8 + 8));
        // Append the second string
        strcat((char *)(result + 1 + 8 + 8), (char *)(value2 + 1 + 8 + 8));
        break;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for +: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Subtract two dynamic values. If the types are not compatible, the program will exit with an error.
 */
void *sub_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
    // all other types are not compatible

    void *result = NULL;

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        // TODO: detect if one of the input values is a temporary one, and reuse it
        // TODO: garbage collect the other input value if both are temporary
        result = allocate_int64();
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) - *((long long *)(value2 + 1 + 8));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for -: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Compare two dynamic values with the "<" operator. If the types are not compatible, the program will exit with an error.
 */
void *lt_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    void *result = allocate_bool();

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:

        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) < *((long long *)(value2 + 1 + 8));
        break;

    case STRING_STRING:

        *((long long *)(result + 1 + 8)) = strcmp((char *)(value1 + 1 + 8 + 8), (char *)(value2 + 1 + 8 + 8)) < 0;
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for <: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}