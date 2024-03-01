#include <stdio.h>

/** Print the input value (int64 representation of a boolean) in Python boolean format */
void _print_bool(long long value) {
    if (value == 0){
        printf("False\n");
    } else {
        printf("True\n");
    }
}
