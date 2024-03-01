#include <stdio.h>

/** Print the input value (int64 representation of a boolean) in Python boolean format */
void println_bool(long long value) {
    if (value == 0){
        printf("False\n");
    } else {
        printf("True\n");
    }
}

/** Print an integer value */
void println_int64(long long value) {
    printf("%lld\n", value);
}

/** Print a string value */
void println_string(const char* value) {
    printf("%s\n", value);
}

/** Print None */
void println_none() {
    printf("None\n");
}
