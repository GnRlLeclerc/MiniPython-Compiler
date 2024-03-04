#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define NONETYPE 0
#define BOOL 1
#define INT64 2
#define STRING 3
#define LIST 4

// Type combinations using tag1 << 3 + tag2
#define BOOL_BOOL 9
#define BOOL_INT64 10
#define INT64_BOOL 17
#define INT64_INT64 18
#define INT64_STRING 19
#define INT64_LIST 20
#define STRING_INT64 26
#define STRING_STRING 27
#define LIST_LIST 36
#define LIST_INT64 34

// WARNING: static inline functions are abstracted away and cannot be called by other modules, like our asm code !

// ************************************************** PRINTING ****************************************************** //

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

/** Print a list without a newline. The full object must be passed */
static inline void print_list(void *value)
{
    putchar('[');

    long size = *((long long *)(value + 1 + 8)); // Offset type + ref_count

    if (size != 0)
    {
        for (int i = 0; i < size - 1; i++)
        {
            print_dynamic(*((void **)(value + 1 + 8 + 8 + i * 8)));
            putchar(',');
            putchar(' ');
        }

        // Print the last element
        print_dynamic(*((void **)(value + 1 + 8 + 8 + (size - 1) * 8)));
    }

    putchar(']');
}

/** Print a list. The full object must be passed */
static inline void println_list(void *value)
{
    print_list(value);
    putchar('\n');
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
        print_string((char *)(value + 1 + 8 + 8)); // Offset type + ref_count + string length
        break;
    case 4:
        print_list(value);
        break;
    default:
        printf("Unknown dynamic type: %d\n", type);
        exit(1);
    }
}

/** Print a dynamic value with a newline */
void println_dynamic(void *value)
{
    print_dynamic(value);
    putchar('\n');
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

/** Allocate an empty list value with a given size */
static inline void *allocate_list(long long size)
{
    void *value = malloc(1 + 8 + 8 + size * 8); // 1 byte tag, 8 bytes ref_count, 8 bytes size, size bytes string
    *((char *)value) = LIST;                    // Set the tag
    *((long long *)(value + 1)) = 0;            // ref_count (0 by default for a temporary value)
    *((long long *)(value + 1 + 8)) = size;     // string size
    return value;
}

/** Extract the type value from a value */
static inline char type_value(void *value)
{
    return *((char *)value);
}

/** Compute the truthyness of a value */
static inline int is_truthy(void *value)
{
    switch (type_value(value))
    {
    case BOOL:
    case INT64:
    case STRING:
    case LIST:
        return (*((long long *)(value + 1 + 8))) != 0;
        break;
    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type for boolean valuation: '%s'\n", value_label(value));
        exit(1);
        break;
    }
}

/** Combine 2 byte types by multiplying the first by 8 and adding the second.
 * This is a fast way to switch through all possible combinations of types.
 * Because our tags do not go over 4 bits, the result fits in a byte too.
 */
static inline char combined_type(void *value1, void *value2)
{
    return (type_value(value1) << 3) + type_value(value2);
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

/** Set list[index] = value, with arguments all being dynamic */
void set_element(void *list, void *index, void *value)
{
    // 1. Get the list index
    void **index_ptr = list_index(list, index);

    // 3.
    // TODO: garbage collect the previous value

    // 4. Set the new value
    *index_ptr = value;

    // 5. Increment the reference count of the new value
    *((long long *)(value + 1)) += 1;
}

/** Get a list element and return it */
void *get_element(void *list, void *index)
{
    return *list_index(list, index);
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
    case LIST_LIST:
    {
        long long size = *((long long *)(value1 + 1 + 8)) + *((long long *)(value2 + 1 + 8));
        result = allocate_list(size);

        // Copy the first list
        for (long long i = 0; i < *((long long *)(value1 + 1 + 8)); i++)
        {
            *((void **)(result + 1 + 8 + 8 + i * 8)) = *((void **)(value1 + 1 + 8 + 8 + i * 8));
            *((long long *)(*((void **)(result + 1 + 8 + 8 + i * 8)) + 1)) += 1;
        }

        // Copy the second list
        for (long long i = 0; i < *((long long *)(value2 + 1 + 8)); i++)
        {
            *((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) = *((void **)(value2 + 1 + 8 + 8 + i * 8));
            *((long long *)(*((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) + 1)) += 1;
        }
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
        printf("TypeError: unsupported operand type(s) for comparison (>, <, <=, =>): '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Compare two dynamic values with the ">" operator. If the types are not compatible, the program will exit with an error.
 */
void *gt_dynamic(void *value1, void *value2)
{
    return lt_dynamic(value2, value1);
}

/** Compare two dynamic values with the ">=" operator. If the types are not compatible, the program will exit with an error.
 */
void *ge_dynamic(void *value1, void *value2)
{
    void *result = lt_dynamic(value1, value2);
    *((long long *)(result + 1 + 8)) = !(*((long long *)(result + 1 + 8)));
    return result;
}

/** Compare two dynamic values with the "<=" operator. If the types are not compatible, the program will exit with an error.
 */
void *le_dynamic(void *value1, void *value2)
{
    return ge_dynamic(value2, value1);
}

/** Compute the negation of a value. If the type is incompatible, the program will exit with an error
 */
void *neg_dynamic(void *value)
{
    // Compatible: int and bool -> int
    // All others are not

    void *result = allocate_int64();

    switch (type_value(value))
    {
    case INT64:
    case BOOL:
        *((long long *)(result + 1 + 8)) = -(*((long long *)(value + 1 + 8)));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type for -: '%s'\n", value_label(value));
        exit(1);
        break;
    }

    return result;
}

/** Multiply two dynamic values. If the types are not compatible, the program will exit with an error.
 */
void *mul_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
    // string & int
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
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) * *((long long *)(value2 + 1 + 8));
        break;

    case STRING_INT64:
    {
        // TODO: garbage collect the 2 operands. We allocate a new string.
        // Compute output size
        if (*((long long *)(value2 + 1 + 8)) > 0)
        {
            long long size = *((long long *)(value1 + 1 + 8)) * *((long long *)(value2 + 1 + 8));
            result = allocate_string(size);

            // Copy the first string
            strcpy((char *)(result + 1 + 8 + 8), (char *)(value1 + 1 + 8 + 8));

            for (long long i = 1; i < *((long long *)(value2 + 1 + 8)); i++)
            {
                strcat((char *)(result + 1 + 8 + 8), (char *)(value1 + 1 + 8 + 8));
            }
        }
        else
        {
            result = allocate_string(0);
        }

        break;
    }
    case INT64_STRING:
    {
        result = mul_dynamic(value2, value1);
        break;
    }
    case LIST_INT64:
    {
        // TODO: garbage collect the 2 operands. We allocate a new list.
        // Compute output size
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

        break;
    }
    case INT64_LIST:
    {
        result = mul_dynamic(value2, value1);
        break;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for x: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Divide two dynamic values. If the types are not compatible, the program will exit with an error.
 */
void *div_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
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
        if (*((long long *)(value2 + 1 + 8)) == 0)
        {
            printf("ZeroDivisionError: division by zero\n");
            exit(1);
        }
        result = allocate_int64();
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) / *((long long *)(value2 + 1 + 8));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for //: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Get the modulus of two dynamic values. If the types are not compatible, the program will exit with an error.
 */
void *mod_dynamic(void *value1, void *value2)
{
    // compatible : int & bool
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
        if (*((long long *)(value2 + 1 + 8)) == 0)
        {
            printf("ZeroDivisionError: modulo by zero\n");
            exit(1);
        }
        result = allocate_int64();
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) % *((long long *)(value2 + 1 + 8));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for \%: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}

/** Compute the truthyness of a value. If the type is incompatible, the program will exit with an error */
void *truthy_dynamic(void *value)
{
    // Compatible: all types can be coerced to a truthy or falsy value

    void *result = allocate_bool();

    *((long long *)(result + 1 + 8)) = is_truthy(value);

    return result;
}

/** Compute the not operation for a value. If the type is incompatible, the program will exit with an error */
void *not_dynamic(void *value)
{
    void *result = truthy_dynamic(value);
    *((long long *)(result + 1 + 8)) = !(*((long long *)(result + 1 + 8)));
    return result;
}

/** Compute the and operation for two values. If the types are incompatible, the program will exit with an error */
void *and_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    return result;
}

/** Compute the or operation for two values. If the types are incompatible, the program will exit with an error */
void *or_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    return result;
}

/** Compute the == operation for two values. If the types are incompatible, the program will exit with an error */
void *eq_dynamic(void *value1, void *value2)
{
    // compatible : all types

    void *result = allocate_bool();

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        *((long long *)(result + 1 + 8)) = *((long long *)(value1 + 1 + 8)) == *((long long *)(value2 + 1 + 8));
        break;

    case STRING_STRING:
    {
        *((long long *)(result + 1 + 8)) = strcmp((char *)(value1 + 1 + 8 + 8), (char *)(value2 + 1 + 8 + 8)) == 0;
        break;
    }
    case LIST_LIST:
    {
        long long size1 = *((long long *)(value1 + 1 + 8));
        long long size2 = *((long long *)(value2 + 1 + 8));
        *((long long *)(result + 1 + 8)) = 1;

        if (size1 != size2)
        {
            *((long long *)(result + 1 + 8)) = 0;
        }
        else
        {
            for (long long i = 0; i < size1; i++)
            {
                void *elem1 = *((void **)(value1 + 1 + 8 + 8 + i * 8));
                void *elem2 = *((void **)(value2 + 1 + 8 + 8 + i * 8));

                if (*((long long *)(elem1 + 1)) != *((long long *)(elem2 + 1)))
                {
                    *((long long *)(result + 1 + 8)) = 0;
                    break;
                }
            }
        }
        break;
    }

    default:
        *((long long *)(result + 1 + 8)) = 0;
        break;
    }

    return result;
}

/** Compute the != operation for two values. If the types are incompatible, the program will exit with an error */
void *neq_dynamic(void *value1, void *value2)
{
    void *result = eq_dynamic(value1, value2);
    *((long long *)(result + 1 + 8)) = !(*((long long *)(result + 1 + 8)));
    return result;
}
