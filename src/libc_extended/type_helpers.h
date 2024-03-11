#ifndef INCLUDE_TYPE_HELPERS
#define INCLUDE_TYPE_HELPERS

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

/** Extract the type value from a value */
static inline char type_value(void *value)
{
    return *((char *)value);
}

/** Combine 2 byte types by multiplying the first by 8 and adding the second.
 * This is a fast way to switch through all possible combinations of types.
 * Because our tags do not go over 4 bits, the result fits in a byte too.
 */
static inline char combined_type(void *value1, void *value2)
{
    return (type_value(value1) << 3) + type_value(value2);
}

#endif
