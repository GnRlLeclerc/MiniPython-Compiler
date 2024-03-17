#include "alloc_helpers.h"
#include "int_helpers.h"
#include "print_helpers.h"
#include "string_list_helpers.h"
#include "type_helpers.h"
#include "types.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// WARNING: static inline functions are abstracted away and cannot be called by other modules, like our asm code !
// Only the functions declared in this file can be called by our asm code.

// ************************************************** PRINTING ****************************************************** //

/** Print a dynamic value with a newline */
void println_dynamic(DYN_VALUE value)
{
    print_dynamic(value);
    putchar('\n');
}

// ************************************************ COMPUTATION ***************************************************** //

/** Compute the truthyness of a value */
static inline int is_truthy(DYN_VALUE value)
{
    switch (type_value(value))
    {
    case BOOL:
    case INT64:
    case STRING:
    case LIST:
        return (*((int64 *)(value + 1 + 8))) != 0;
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
static inline int is_equal(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : all types

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        return *((int64 *)(value1 + 1 + 8)) == *((int64 *)(value2 + 1 + 8));
    case STRING_STRING:
    {
        return strcmp(get_string_value(value1), get_string_value(value2)) == 0;
    }
    case LIST_LIST:
    {
        int64 size1 = *((int64 *)(value1 + 1 + 8));
        int64 size2 = *((int64 *)(value2 + 1 + 8));
        int result = 1;

        if (size1 != size2)
        {
            result = 0;
        }
        else
        {
            for (int64 i = 0; i < size1; i++)
            {
                DYN_VALUE elem1 = *((DYN_ARRAY)(value1 + 1 + 8 + 8 + i * 8));
                DYN_VALUE elem2 = *((DYN_ARRAY)(value2 + 1 + 8 + 8 + i * 8));

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
static inline int is_lt(DYN_VALUE value1, DYN_VALUE value2)
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
        return *((int64 *)(value1 + 1 + 8)) < *((int64 *)(value2 + 1 + 8));

    case STRING_STRING:
        return strcmp(get_string_value(value1), get_string_value(value2)) < 0;

    case LIST_LIST:
    {
        int64 size1 = *((int64 *)(value1 + 1 + 8));
        int64 size2 = *((int64 *)(value2 + 1 + 8));
        int64 min_size = size1 < size2 ? size1 : size2;
        int result = 0;

        for (int64 i = 0; i < min_size; i++)
        {
            DYN_VALUE elem1 = *((DYN_ARRAY)(value1 + 1 + 8 + 8 + i * 8));
            DYN_VALUE elem2 = *((DYN_ARRAY)(value2 + 1 + 8 + 8 + i * 8));

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
void set_element(DYN_VALUE list, DYN_VALUE index, DYN_VALUE value)
{
    // 1. Get the list index
    DYN_ARRAY index_ptr = list_index(list, index);

    // 3.
    // TODO: garbage collect the previous value

    // 4. Set the new value
    *index_ptr = value;

    // 5. Increment the reference count of the new value
    *((int64 *)(value + 1)) += 1;
}

/** Get a list element and return it */
DYN_VALUE get_element(DYN_VALUE list, DYN_VALUE index)
{
    return *list_index(list, index);
}

/** Add two dynamic values. If the types are not compatible, the program will exit with an error.
 */
DYN_VALUE add_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    DYN_VALUE result = NULL;

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
        int64 size = get_size(value1) + get_size(value2);
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
DYN_VALUE add_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    DYN_VALUE result = NULL;

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
        int64 size = get_size(value1) + get_size(value2);
        int64 capacity1 = get_capacity(value1);
        if (capacity1 >= size)
        {
            result = value1;
            strcat(get_string_value(result), get_string_value(value2));
            *((int64 *)(result + 1 + 8)) = size;
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
DYN_VALUE add_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    DYN_VALUE result = NULL;

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
        int64 size = get_size(value1) + get_size(value2);
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
DYN_VALUE add_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // string & string
    // none is never compatible

    DYN_VALUE result = NULL;

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
        int64 size = get_size(value1) + get_size(value2);
        int64 capacity1 = get_capacity(value1);
        if (capacity1 >= size)
        {
            result = value1;
            strcat(get_string_value(result), get_string_value(value2));
            *((int64 *)(result + 1 + 8)) = size;
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
DYN_VALUE sub_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // all other types are not compatible

    DYN_VALUE result = NULL;

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
DYN_VALUE sub_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE sub_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE sub_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE lt_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = allocate_bool();
    *((int64 *)(result + 1 + 8)) = is_lt(value1, value2);
    return result;
}
DYN_VALUE lt_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
DYN_VALUE lt_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value2 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
DYN_VALUE lt_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_lt(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compare two dynamic values with the ">" operator. If the types are not compatible, the program will exit with an error.
 */
DYN_VALUE gt_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    return lt_dynamic(value2, value1);
}
DYN_VALUE gt_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    return lt_dynamic_temp_2(value2, value1);
}
DYN_VALUE gt_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    return lt_dynamic_temp_1(value2, value1);
}
DYN_VALUE gt_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    return lt_dynamic_temp_3(value2, value1);
}

/** Compare two dynamic values with the ">=" operator. If the types are not compatible, the program will exit with an error.
 */
DYN_VALUE ge_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = lt_dynamic(value1, value2);
    *((int64 *)(result + 1 + 8)) = !(*((int64 *)(result + 1 + 8)));
    return result;
}
DYN_VALUE ge_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    lt_dynamic_temp_1(value1, value2);
    *((int64 *)(value1 + 1 + 8)) = !(*((int64 *)(value1 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
DYN_VALUE ge_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    lt_dynamic_temp_2(value1, value2);
    *((int64 *)(value2 + 1 + 8)) = !(*((int64 *)(value2 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
DYN_VALUE ge_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    lt_dynamic_temp_3(value1, value2);
    *((int64 *)(value1 + 1 + 8)) = !(*((int64 *)(value1 + 1 + 8)));
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}

/** Compare two dynamic values with the "<=" operator. If the types are not compatible, the program will exit with an error.
 */
DYN_VALUE le_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    return ge_dynamic(value2, value1);
}
DYN_VALUE le_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    return ge_dynamic_temp_2(value2, value1);
}
DYN_VALUE le_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    return ge_dynamic_temp_1(value2, value1);
}
DYN_VALUE le_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    return ge_dynamic_temp_3(value2, value1);
}

/** Compute the negation of a value. If the type is incompatible, the program will exit with an error
 */
static inline void neg_dynamic_helper(DYN_VALUE value, DYN_VALUE result)
{
    // Compatible: int and bool -> int
    // All others are not

    switch (type_value(value))
    {
    case INT64:
    case BOOL:
        *((int64 *)(result + 1 + 8)) = -(*((int64 *)(value + 1 + 8)));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type for -: '%s'\n", value_label(value));
        exit(1);
        break;
    }
}

DYN_VALUE neg_dynamic(DYN_VALUE value)
{
    DYN_VALUE result;
    result = allocate_int64();
    neg_dynamic_helper(value, result);
    return result;
}
DYN_VALUE neg_dynamic_temp(DYN_VALUE value)
{
    neg_dynamic_helper(value, value);
    return value;
}

/** Multiply two dynamic values. If the types are not compatible, the program will exit with an error.
 */
DYN_VALUE mul_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // string & int
    // none is never compatible
    DYN_VALUE result = NULL;

    switch (combined_type(value1, value2))
    {
    // Boolean and integer addition.
    case BOOL_BOOL:
    case BOOL_INT64:
    case INT64_BOOL:
    case INT64_INT64:
        result = allocate_int64();
        mul_int_helper(value1, value2, result);
        return result;

    case STRING_INT64:
    {
        int64 value2_int = get_int_value(value2);
        int64 size = get_size(value1) * value2_int;
        result = allocate_string(size);
        mul_string_int_helper(value1, value2, result);
        return result;
    }
    case INT64_STRING:
    {
        int64 value1_int = get_int_value(value1);
        int64 size = get_size(value2) * value1_int;
        result = allocate_string(size);
        mul_string_int_helper(value2, value1, result);
        return result;
    }
    case LIST_INT64:
    {
        result = mul_list_int_helper(value1, value2);
        return result;
    }
    case INT64_LIST:
    {
        result = mul_list_int_helper(value2, value1);
        return result;
    }

    default:
        // Default: unsupported types
        printf("TypeError: unsupported operand type(s) for x: '%s' and '%s'\n", value_label(value1), value_label(value2));
        exit(1);
        break;
    }
}
DYN_VALUE mul_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
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
        DYN_VALUE result = NULL;
        int64 value2_int = get_int_value(value2);
        int64 size = get_size(value1) * value2_int;
        if (get_capacity(value1) >= size)
        {
            result = value1;
            // Technically one copy too much
            mul_string_int_helper(value1, value2, result);
            *((int64 *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            mul_string_int_helper(value1, value2, result);
            free(value1);
        }
        free(value2);
        return result;
    }
    case INT64_STRING:
    {
        DYN_VALUE result = NULL;
        int64 value1_int = get_int_value(value1);
        int64 size = get_size(value2) * value1_int;
        if (get_capacity(value2) >= size)
        {
            result = value2;
            // Technically one copy too much
            mul_string_int_helper(value2, value1, result);
            *((int64 *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            mul_string_int_helper(value2, value1, result);
            free(value2);
        }
        free(value1);
        return result;
    }
    case LIST_INT64:
    {
        DYN_VALUE result = mul_list_int_helper(value1, value2);
        free(value1);
        return result;
    }
    case INT64_LIST:
    {
        DYN_VALUE result = mul_list_int_helper(value2, value1);
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
DYN_VALUE mul_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
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
        DYN_VALUE result = NULL;
        int64 value2_int = get_int_value(value2);
        int64 size = get_size(value1) * value2_int;
        result = allocate_string(size);
        mul_string_int_helper(value1, value2, result);
        free(value2);
        return result;
    }
    case INT64_STRING:
    {
        DYN_VALUE result = NULL;
        int64 value1_int = get_int_value(value1);
        int64 size = get_size(value2) * value1_int;
        result = allocate_string(size);
        mul_string_int_helper(value2, value1, result);
        free(value2);
        return result;
    }
    case LIST_INT64:
    {
        DYN_VALUE result = mul_list_int_helper(value1, value2);
        free(value2);
        return result;
    }
    case INT64_LIST:
    {
        DYN_VALUE result = mul_list_int_helper(value2, value1);
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
DYN_VALUE mul_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
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
        DYN_VALUE result = NULL;
        int64 value2_int = get_int_value(value2);
        int64 size = get_size(value1) * value2_int;
        if (get_capacity(value1) >= size)
        {
            result = value1;
            // Technically one copy too much
            mul_string_int_helper(value1, value2, result);
            *((int64 *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            mul_string_int_helper(value1, value2, result);
            free(value1);
        }
        free(value2);
        return result;
    }
    case INT64_STRING:
    {
        DYN_VALUE result = NULL;
        int64 value1_int = get_int_value(value1);
        int64 size = get_size(value2) * value1_int;
        if (get_capacity(value2) >= size)
        {
            result = value2;
            // Technically one copy too much
            mul_string_int_helper(value2, value1, result);
            *((int64 *)(result + 1 + 8)) = size;
        }
        else
        {
            result = allocate_string(size);
            mul_string_int_helper(value2, value1, result);
            free(value2);
        }
        free(value1);
        return result;
    }
    case LIST_INT64:
    {
        DYN_VALUE result = mul_list_int_helper(value1, value2);
        free(value1);
        free(value2);
        return result;
    }
    case INT64_LIST:
    {
        DYN_VALUE result = mul_list_int_helper(value2, value1);
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
DYN_VALUE div_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // none is never compatible

    DYN_VALUE result = NULL;

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
DYN_VALUE div_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE div_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE div_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE mod_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    // compatible : int & bool
    // none is never compatible

    DYN_VALUE result = NULL;

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
DYN_VALUE mod_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE mod_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE mod_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
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
DYN_VALUE truthy_dynamic(DYN_VALUE value)
{
    // Compatible: all types can be coerced to a truthy or falsy value
    DYN_VALUE result = allocate_bool();
    *((int64 *)(result + 1 + 8)) = is_truthy(value);
    return result;
}
DYN_VALUE truthy_dynamic_temp(DYN_VALUE value)
{
    // Compatible: all types can be coerced to a truthy or falsy value
    *((int64 *)(value + 1 + 8)) = is_truthy(value);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value) = BOOL;
    return value;
}

/** Compute the not operation for a value. If the type is incompatible, the program will exit with an error */
static inline DYN_VALUE not_dynamic_helper(DYN_VALUE value, DYN_VALUE result)
{
    *((int64 *)(result + 1 + 8)) = !is_truthy(value);
    return result;
}

DYN_VALUE not_dynamic(DYN_VALUE value)
{
    DYN_VALUE result = allocate_bool();
    not_dynamic_helper(value, result);
    return result;
}

DYN_VALUE not_dynamic_temp(DYN_VALUE value)
{
    not_dynamic_helper(value, value);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value) = BOOL;
    return value;
}

/** Compute the and operation for two values. If the types are incompatible, the program will exit with an error */
DYN_VALUE and_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = allocate_bool();
    *((int64 *)(result + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    return result;
}
DYN_VALUE and_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
DYN_VALUE and_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value2 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
DYN_VALUE and_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_truthy(value1) && is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the or operation for two values. If the types are incompatible, the program will exit with an error */
DYN_VALUE or_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = allocate_bool();
    *((int64 *)(result + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    return result;
}
DYN_VALUE or_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
DYN_VALUE or_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value2 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
DYN_VALUE or_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_truthy(value1) || is_truthy(value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the == operation for two values. If the types are incompatible, the program will exit with an error */
DYN_VALUE eq_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = allocate_bool();
    *((int64 *)(result + 1 + 8)) = is_equal(value1, value2);
    return result;
}
DYN_VALUE eq_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    return value1;
}
DYN_VALUE eq_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value2 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value2) = BOOL;
    return value2;
}
DYN_VALUE eq_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    *((int64 *)(value1 + 1 + 8)) = is_equal(value1, value2);
    // update value type
    // NOTE : if value is big we lose some memory until value is freed
    *((char *)value1) = BOOL;
    free(value2);
    return value1;
}

/** Compute the != operation for two values. If the types are incompatible, the program will exit with an error */
DYN_VALUE neq_dynamic(DYN_VALUE value1, DYN_VALUE value2)
{
    DYN_VALUE result = eq_dynamic(value1, value2);
    *((int64 *)(result + 1 + 8)) = !(*((int64 *)(result + 1 + 8)));
    return result;
}
DYN_VALUE neq_dynamic_temp_1(DYN_VALUE value1, DYN_VALUE value2)
{
    eq_dynamic_temp_1(value1, value2);
    *((int64 *)(value1 + 1 + 8)) = !(*((int64 *)(value1 + 1 + 8)));
    return value1;
}
DYN_VALUE neq_dynamic_temp_2(DYN_VALUE value1, DYN_VALUE value2)
{
    eq_dynamic_temp_2(value1, value2);
    *((int64 *)(value2 + 1 + 8)) = !(*((int64 *)(value2 + 1 + 8)));
    return value2;
}
DYN_VALUE neq_dynamic_temp_3(DYN_VALUE value1, DYN_VALUE value2)
{
    eq_dynamic_temp_3(value1, value2);
    *((int64 *)(value1 + 1 + 8)) = !(*((int64 *)(value1 + 1 + 8)));
    return value1;
}

/** Compute the length of a dynamic value. Only works for strings and lists */
DYN_VALUE len_dynamic(DYN_VALUE value)
{
    DYN_VALUE result = allocate_int64();

    switch (type_value(value))
    {
    case STRING:
    case LIST:
        *((int64 *)(result + 1 + 8)) = *((int64 *)(value + 1 + 8));
        break;

    default:
        // Default: unsupported types
        printf("TypeError: object of type '%s' has no len()\n", value_label(value));
        exit(1);
        break;
    }

    return result;
}

DYN_VALUE range_list(DYN_VALUE value)
{
    DYN_VALUE result = NULL;

    switch (type_value(value))
    {
    case INT64:
    case BOOL:
    {
        int64 size = *((int64 *)(value + 1 + 8));
        result = allocate_list(size);

        for (int64 i = 0; i < size; i++)
        {
            DYN_VALUE elem = allocate_int64();
            *((int64 *)(elem + 1 + 8)) = i;
            *((DYN_ARRAY)(result + 1 + 8 + 8 + i * 8)) = elem;
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
