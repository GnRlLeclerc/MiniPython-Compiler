/** Debug utils */
#pragma once

#include "int_helpers.h"
#include "type_helpers.h"
#include "types.h"
#include <stdio.h>

static inline void debug_print(DYN_VALUE value)
{
    printf("{\n");
    printf("  => %s\n", value_label(value));
    printf("  References: %lld\n", *((int64 *)(value + 1)));

    switch (type_value(value))
    {
    case INT64:
    case BOOL:
        printf("  Value: %lld\n", get_int_value(value));
        break;

    case STRING:
        printf("  Value: %s\n", value + 1 + 8 + 8 + 8);
        printf("  Size: %lld\n", *(int64 *)(value + 1 + 8));
        printf("  Capacity: %lld\n", *(int64 *)(value + 1 + 8 + 8));
        break;
    case LIST:
        printf("  Size: %lld\n", *(int64 *)(value + 1 + 8));
        break;
    }

    printf("}\n");
}
