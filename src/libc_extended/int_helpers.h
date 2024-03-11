/** Get the integer value of a dynamic value that is assumed to be an integer.*/
static inline long long get_int_value(void *value)
{
    return *((long long *)(value + 1 + 8));
}

/** Set the integer value of a dynamic value that is assumed to be an integer..*/
static inline void set_int_value(void *value, int new_value)
{
    *((long long *)(value + 1 + 8)) = new_value;
}

static inline void *add_int_helper(void *value1, void *value2, void *result)
{
    set_int_value(result, get_int_value(value1) + get_int_value(value2));
}

static inline void *sub_int_helper(void *value1, void *value2, void *result)
{
    set_int_value(result, get_int_value(value1) - get_int_value(value2));
}

static inline void *mul_int_helper(void *value1, void *value2, void *result)
{
    set_int_value(result, get_int_value(value1) * get_int_value(value2));
}

static inline void *div_int_helper(void *value1, void *value2, void *result)
{
    if (get_int_value(value2) == 0)
    {
        printf("ZeroDivisionError: division by zero\n");
        exit(1);
    }
    set_int_value(result, get_int_value(value1) / get_int_value(value2));
}

static inline void *mod_int_helper(void *value1, void *value2, void *result)
{
    if (get_int_value(value2) == 0)
    {
        printf("ZeroDivisionError: modulo by zero\n");
        exit(1);
    }
    set_int_value(result, get_int_value(value1) % get_int_value(value2));
}
