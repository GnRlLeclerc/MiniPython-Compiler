#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "types.h"
#include "int_helpers.h"
#include "print_helpers.h"
#include "type_helpers.h"
#include "alloc_helpers.h"
#include "string_list_helpers.h"

// WARNING: static inline functions are abstracted away and cannot be called by other modules, like our asm code !
// Only the functions declared in this file can be called by our asm code.

// ************************************************** PRINTING ****************************************************** //

/** Print a dynamic value with a newline */
void println_dynamic(void *value)
{
    print_dynamic(value);
    putchar('\n');
}

// ************************************************ COMPUTATION ***************************************************** //

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

/** Compute the equality of two values. If the types are not compatible, the program will exit with an error.
 */
static inline int is_equal(void *value1, void *value2)
{
    // compatible : all types

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        return *((long long *)(value1 + 1 + 8)) == *((long long *)(value2 + 1 + 8));
    case STRING_STRING:
    {
        return strcmp(get_string_value(value1), get_string_value(value2)) == 0;
    }
    case LIST_LIST:
    {
        long long size1 = *((long long *)(value1 + 1 + 8));
        long long size2 = *((long long *)(value2 + 1 + 8));
        int result = 1;

        if (size1 != size2)
        {
            result = 0;
        }
        else
        {
            for (long long i = 0; i < size1; i++)
            {
                void *elem1 = *((void **)(value1 + 1 + 8 + 8 + i * 8));
                void *elem2 = *((void **)(value2 + 1 + 8 + 8 + i * 8));

                if (!is_equal(elem1, elem2))
                {
                    result = 0;
                    break;
                }
            }
        }
        return result;
    }

    case NONE_NONE:
        return 1;

    default:
        return 0;
    }
}

/** Compute the "<" operation for two values. If the types are not compatible, the program will exit with an error.
 */
static inline int is_lt(void *value1, void *value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        return *((long long *)(value1 + 1 + 8)) < *((long long *)(value2 + 1 + 8));

    case STRING_STRING:
        return strcmp(get_string_value(value1), get_string_value(value2)) < 0;

    case LIST_LIST:
    {
        long long size1 = *((long long *)(value1 + 1 + 8));
        long long size2 = *((long long *)(value2 + 1 + 8));
        long long min_size = size1 < size2 ? size1 : size2;
        int result = 0;

        for (long long i = 0; i < min_size; i++)
        {
            void *elem1 = *((void **)(value1 + 1 + 8 + 8 + i * 8));
            void *elem2 = *((void **)(value2 + 1 + 8 + 8 + i * 8));

            if (is_equal(elem1, elem2))
            {
                continue;
            }
            else if (is_lt(elem1, elem2))
            {
                result = 1;
                break;
            }
            else
            {
                result = 0;
                break;
            }
        }

        if (result == 0)
        {
            return size1 < size2;
        }

        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for comparison (>, <, <=, =>): '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
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
        result = allocate_int64();
        add_int_helper(value1, value2, result);
        break;

    case STRING_STRING:
    {
        // Compute output size
        long long size = get_size(value1) + get_size(value2);
        // Since the new string is bigger, we always allocate a new one
        result = allocate_string(size);
        add_string_helper(value1, value2, result);
        break;
    }
    case LIST_LIST:
    {
        result = add_list_helper(value1, value2);
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
void *add_dynamic_temp_1(void *value1, void *value2)
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
        add_int_helper(value1, value2, value1);
        return value1;

    case STRING_STRING:
    {
        // Compute output size
        long long size = get_size(value1) + get_size(value2);
        long long capacity1 = get_capacity(value1);
        if (capacity1 >= size)
        {
            result = value1;
            strcat(get_string_value(result), get_string_value(value2));
            *((long long *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            add_string_helper(value1, value2, result);
            free(value1);
            free(value2);
        }
        return result;
    }
    case LIST_LIST:
    {
        result = add_list_helper(value1, value2);
        free(value1);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for +: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *add_dynamic_temp_2(void *value1, void *value2)
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
        add_int_helper(value1, value2, value2);
        return value2;

    case STRING_STRING:
    {
        // We don't do anything different as the second string being temporary doesn't
        // really help
        // Compute output size
        long long size = get_size(value1) + get_size(value2);
        // Since the new string is bigger, we always allocate a new one
        result = allocate_string(size);
        add_string_helper(value1, value2, result);
        free(value2);
        return result;
    }
    case LIST_LIST:
    {
        result = add_list_helper(value1, value2);
        free(value2);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for +: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *add_dynamic_temp_3(void *value1, void *value2)
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
        add_int_helper(value1, value2, value1);
        return value1;

    case STRING_STRING:
    {
        // Compute output size
        long long size = get_size(value1) + get_size(value2);
        long long capacity1 = get_capacity(value1);
        if (capacity1 >= size)
        {
            result = value1;
            strcat(get_string_value(result), get_string_value(value2));
            *((long long *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            add_string_helper(value1, value2, result);
            free(value1);
            free(value2);
        }
        return result;
    }
    case LIST_LIST:
    {
        result = add_list_helper(value1, value2);
        free(value1);
        free(value2);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for +: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
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
        result = allocate_int64();
        sub_int_helper(value1, value2, result);
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for -: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}
void *sub_dynamic_temp_1(void *value1, void *value2)
{
    // compatible : int & bool
    // all other types are not compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        sub_int_helper(value1, value2, value1);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for -: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *sub_dynamic_temp_2(void *value1, void *value2)
{
    // compatible : int & bool
    // all other types are not compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        sub_int_helper(value1, value2, value2);
        return value2;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for -: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *sub_dynamic_temp_3(void *value1, void *value2)
{
    // compatible : int & bool
    // all other types are not compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        sub_int_helper(value1, value2, value1);
        free(value2);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for -: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}

/** Compare two dynamic values with the "<" operator. If the types are not compatible, the program will exit with an error.
 */
void *lt_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_lt(value1, value2);
    return result;
}
void *lt_dynamic_temp_1(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
void *lt_dynamic_temp_2(void *value1, void *value2)
{
    *((long long *)(value2 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
void *lt_dynamic_temp_3(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compare two dynamic values with the ">" operator. If the types are not compatible, the program will exit with an error.
 */
void *gt_dynamic(void *value1, void *value2)
{
    return lt_dynamic(value2, value1);
}
void *gt_dynamic_temp_1(void *value1, void *value2)
{
    return lt_dynamic_temp_2(value2, value1);
}
void *gt_dynamic_temp_2(void *value1, void *value2)
{
    return lt_dynamic_temp_1(value2, value1);
}
void *gt_dynamic_temp_3(void *value1, void *value2)
{
    return lt_dynamic_temp_3(value2, value1);
}

/** Compare two dynamic values with the ">=" operator. If the types are not compatible, the program will exit with an error.
 */
void *ge_dynamic(void *value1, void *value2)
{
    void *result = lt_dynamic(value1, value2);
    *((long long *)(result + 1 + 8)) = !(*((long long *)(result + 1 + 8)));
    return result;
}
void *ge_dynamic_temp_1(void *value1, void *value2)
{
    lt_dynamic_temp_1(value1, value2);
    *((long long *)(value1 + 1 + 8)) = !(*((long long *)(value1 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
void *ge_dynamic_temp_2(void *value1, void *value2)
{
    lt_dynamic_temp_2(value1, value2);
    *((long long *)(value2 + 1 + 8)) = !(*((long long *)(value2 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
void *ge_dynamic_temp_3(void *value1, void *value2)
{
    lt_dynamic_temp_3(value1, value2);
    *((long long *)(value1 + 1 + 8)) = !(*((long long *)(value1 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}

/** Compare two dynamic values with the "<=" operator. If the types are not compatible, the program will exit with an error.
 */
void *le_dynamic(void *value1, void *value2)
{
    return ge_dynamic(value2, value1);
}
void *le_dynamic_temp_1(void *value1, void *value2)
{
    return ge_dynamic_temp_2(value2, value1);
}
void *le_dynamic_temp_2(void *value1, void *value2)
{
    return ge_dynamic_temp_1(value2, value1);
}
void *le_dynamic_temp_3(void *value1, void *value2)
{
    return ge_dynamic_temp_3(value2, value1);
}

/** Compute the negation of a value. If the type is incompatible, the program will exit with an error
 */
static inline void *neg_dynamic_helper(void *value, void *result)
{
    // Compatible: int and bool -> int
    // All others are not

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
}

void *neg_dynamic(void *value)
{
    void *result;
    result = allocate_int64();
    neg_dynamic_helper(value, result);
    return result;
}
void *neg_dynamic_temp(void *value)
{
    neg_dynamic_helper(value, value);
    return value;
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
        result = allocate_int64();
        mul_int_helper(value1, value2, result);
        break;

    case STRING_INT64:
    {
        mul_string_int_helper(value1, value2, result);
        break;
    }
    case INT64_STRING:
    {
        mul_string_int_helper(value2, value1, result);
        break;
    }
    case LIST_INT64:
    {
        mul_list_int_helper(value1, value2, result);
        break;
    }
    case INT64_LIST:
    {
        mul_list_int_helper(value2, value1, result);
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
void *mul_dynamic_temp_1(void *value1, void *value2)
{
    // compatible : int & bool
    // string & int
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mul_int_helper(value1, value2, value1);
        return value1;

    case STRING_INT64:
    {
        void *result = NULL;
        mul_string_int_helper(value1, value2, result);
        free(value1);
        return result;
    }
    case INT64_STRING:
    {
        void *result = NULL;
        mul_string_int_helper(value2, value1, result);
        free(value1);
        return result;
    }
    case LIST_INT64:
    {
        void *result = NULL;
        mul_list_int_helper(value1, value2, result);
        free(value1);
        return result;
    }
    case INT64_LIST:
    {
        void *result = NULL;
        mul_list_int_helper(value2, value1, result);
        free(value1);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for x: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *mul_dynamic_temp_2(void *value1, void *value2)
{
    // compatible : int & bool
    // string & int
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mul_int_helper(value1, value2, value2);
        return value2;

    case STRING_INT64:
    {
        void *result = NULL;
        mul_string_int_helper(value1, value2, result);
        free(value2);
        return result;
    }
    case INT64_STRING:
    {
        void *result = NULL;
        mul_string_int_helper(value2, value1, result);
        free(value2);
        return result;
    }
    case LIST_INT64:
    {
        void *result = NULL;
        mul_list_int_helper(value1, value2, result);
        free(value2);
        return result;
    }
    case INT64_LIST:
    {
        void *result = NULL;
        mul_list_int_helper(value2, value1, result);
        free(value2);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for x: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *mul_dynamic_temp_3(void *value1, void *value2)
{
    // compatible : int & bool
    // string & int
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mul_int_helper(value1, value2, value1);
        return value1;

    case STRING_INT64:
    {
        void *result = NULL;
        mul_string_int_helper(value1, value2, result);
        free(value1);
        free(value2);
        return result;
    }
    case INT64_STRING:
    {
        void *result = NULL;
        mul_string_int_helper(value2, value1, result);
        free(value1);
        free(value2);
        return result;
    }
    case LIST_INT64:
    {
        void *result = NULL;
        mul_list_int_helper(value1, value2, result);
        free(value1);
        free(value2);
        return result;
    }
    case INT64_LIST:
    {
        void *result = NULL;
        mul_list_int_helper(value2, value1, result);
        free(value1);
        free(value2);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for x: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
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
        result = allocate_int64();
        div_int_helper(value1, value2, result);
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for //: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}
void *div_dynamic_temp_1(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        div_int_helper(value1, value2, value1);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for //: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *div_dynamic_temp_2(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        div_int_helper(value1, value2, value2);
        return value2;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for //: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *div_dynamic_temp_3(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        div_int_helper(value1, value2, value1);
        free(value2);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for //: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
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
        result = allocate_int64();
        mod_int_helper(value1, value2, result);
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for %%: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }

    return result;
}
void *mod_dynamic_temp_1(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mod_int_helper(value1, value2, value1);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for %%: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *mod_dynamic_temp_2(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mod_int_helper(value1, value2, value2);
        return value2;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for %%: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
void *mod_dynamic_temp_3(void *value1, void *value2)
{
    // compatible : int & bool
    // none is never compatible

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        mod_int_helper(value1, value2, value1);
        free(value2);
        return value1;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for %%: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}

/** Compute the truthyness of a value. If the type is incompatible, the program will exit with an error */
void *truthy_dynamic(void *value)
{
    // Compatible: all types can be coerced to a truthy or falsy value
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_truthy(value);
    return result;
}
void *truthy_dynamic_temp(void *value)
{
    // Compatible: all types can be coerced to a truthy or falsy value
    *((long long *)(value + 1 + 8)) = is_truthy(value);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value) = BOOL;
    return value;
}

/** Compute the not operation for a value. If the type is incompatible, the program will exit with an error */
static inline void *not_dynamic_helper(void *value, void *result)
{
    *((long long *)(result + 1 + 8)) = !is_truthy(value);
    return result;
}

void *not_dynamic(void *value)
{
    void *result = allocate_bool();
    not_dynamic_helper(value, result);
    return result;
}

void *not_dynamic_temp(void *value)
{
    not_dynamic_helper(value, value);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value) = BOOL;
    return value;
}

/** Compute the and operation for two values. If the types are incompatible, the program will exit with an error */
void *and_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    return result;
}
void *and_dynamic_temp_1(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
void *and_dynamic_temp_2(void *value1, void *value2)
{
    *((long long *)(value2 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
void *and_dynamic_temp_3(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the or operation for two values. If the types are incompatible, the program will exit with an error */
void *or_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    return result;
}
void *or_dynamic_temp_1(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
void *or_dynamic_temp_2(void *value1, void *value2)
{
    *((long long *)(value2 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
void *or_dynamic_temp_3(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the == operation for two values. If the types are incompatible, the program will exit with an error */
void *eq_dynamic(void *value1, void *value2)
{
    void *result = allocate_bool();
    *((long long *)(result + 1 + 8)) = is_equal(value1, value2);
    return result;
}
void *eq_dynamic_temp_1(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
void *eq_dynamic_temp_2(void *value1, void *value2)
{
    *((long long *)(value2 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
void *eq_dynamic_temp_3(void *value1, void *value2)
{
    *((long long *)(value1 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the != operation for two values. If the types are incompatible, the program will exit with an error */
void *neq_dynamic(void *value1, void *value2)
{
    void *result = eq_dynamic(value1, value2);
    *((long long *)(result + 1 + 8)) = !(*((long long *)(result + 1 + 8)));
    return result;
}
void *neq_dynamic_temp_1(void *value1, void *value2)
{
    eq_dynamic_temp_1(value1, value2);
    *((long long *)(value1 + 1 + 8)) = !(*((long long *)(value1 + 1 + 8)));
    return value1;
}
void *neq_dynamic_temp_2(void *value1, void *value2)
{
    eq_dynamic_temp_2(value1, value2);
    *((long long *)(value2 + 1 + 8)) = !(*((long long *)(value2 + 1 + 8)));
    return value2;
}
void *neq_dynamic_temp_3(void *value1, void *value2)
{
    eq_dynamic_temp_3(value1, value2);
    *((long long *)(value1 + 1 + 8)) = !(*((long long *)(value1 + 1 + 8)));
    return value1;
}

/** Compute the length of a dynamic value. Only works for strings and lists */
void *len_dynamic(void *value)
{
    void *result = allocate_int64();

    switch (type_value(value))
    {
    case STRING:
    case LIST:
        *((long long *)(result + 1 + 8)) = *((long long *)(value + 1 + 8));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: object of type '%s' has no len()\n", value_label(value));
        exit(1);
        break;
    }

    return result;
}

void *range_list(void *value)
{
    void *result = NULL;

    switch (type_value(value))
    {
    case INT64:
    case BOOL:
    {
        long long size = *((long long *)(value + 1 + 8));
        result = allocate_list(size);

        for (long long i = 0; i < size; i++)
        {
            void *elem = allocate_int64();
            *((long long *)(elem + 1 + 8)) = i;
            *((void **)(result + 1 + 8 + 8 + i * 8)) = elem;
        }
        break;
    }

    default:
        // Default: unsupported types
        printf("TypeError: range() argument must be int, not %s\n", value_label(value));
        exit(1);
        break;
    }

    return result;
}
