/** Define type constants.
 *
 * We also use (tag1 << 3 + tag2) values to represent type combinations using 1 byte,
 * in order to be able to use switch statements for efficient type combination processing.
 */

#pragma once

#define NONETYPE 0
#define BOOL 1
#define INT64 2
#define STRING 3
#define LIST 4

// Type combinations using tag1 << 3 + tag2
#define NONE_NONE 0
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

// Type alias for our dynamic type values
// They technically are *char as byte arrays.
#define DYN_VALUE char *
#define DYN_ARRAY char **
#define int64 long long