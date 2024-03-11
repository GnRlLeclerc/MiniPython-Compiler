#include <string.h>
#include "alloc_helpers.h"
#include "type_helpers.h"

/** Get size of a dynamic value assumed to be a list or a string.*/
static inline long long get_size(void *value)
{
    return *((long long *)(value + 1 + 8));
}

static inline void *add_string_helper(void *value1, void *value2)
{
    // Compute output size
    long long size = get_size(value1) + get_size(value2);
    // Since the new string is bigger, we always allocate a new one
    void *result = allocate_string(size);

    // Copy the first string
    strcpy((char *)(result + 1 + 8 + 8), (char *)(value1 + 1 + 8 + 8));
    // Append the second string
    strcat((char *)(result + 1 + 8 + 8), (char *)(value2 + 1 + 8 + 8));

    return result;
}

static inline void *add_list_helper(void *value1, void *value2)
{
    long long size1 = get_size(value1);
    long long size2 = get_size(value2);
    long long size = size1 + size2;
    void *result = allocate_list(size);

    // Copy the first list
    for (long long i = 0; i < size1; i++)
    {
        *((void **)(result + 1 + 8 + 8 + i * 8)) = *((void **)(value1 + 1 + 8 + 8 + i * 8));
        *((long long *)(*((void **)(result + 1 + 8 + 8 + i * 8)) + 1)) += 1;
    }

    // Copy the second list
    for (long long i = 0; i < size2; i++)
    {
        *((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) = *((void **)(value2 + 1 + 8 + 8 + i * 8));
        *((long long *)(*((void **)(result + 1 + 8 + 8 + (*((long long *)(value1 + 1 + 8)) + i) * 8)) + 1)) += 1;
    }

    return result;
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
