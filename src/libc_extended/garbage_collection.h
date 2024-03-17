/** Helpers for garbage collection */

#pragma once
#include "string_list_helpers.h"
#include "type_helpers.h"
#include "types.h"

static inline DYN_VALUE get_list_value(DYN_VALUE list, int64 index)
{
    return *((DYN_VALUE *)(list + 1 + 8 + 8 + index * 8));
}

/** Decrement the reference count of a value inside a list.
 * Returns the new reference count */
static inline int64 decrement_list_value_refcount(DYN_VALUE list, int64 index)
{
    return *(get_list_value(list, index) + 1) -= 1;
}

/** Free a dynamic value */
static inline void free_dyn_value(DYN_VALUE value)
{
    // Apply garbage collection
    switch (type_value(value))
    {
    case LIST:
        for (int i = 0; i < get_size(value); i++)
        {
            // Decrement the variable refcount, as the parent list no longer points to it
            if (decrement_list_value_refcount(value, i) <= 0)
            {
                free_dyn_value(get_list_value(value, i));
            };
        }
        break;

    default:
        free(value);
    }
}