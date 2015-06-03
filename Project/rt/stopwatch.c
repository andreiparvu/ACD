#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <inttypes.h>
#include <time.h>

#include "rt.h"

#ifndef CLOCK_MONOTONIC_RAW
// for Linux 2.6.28 and older
#define CLOCK_MONOTONIC_RAW CLOCK_MONOTONIC
#endif

void Stopwatch_init(struct stopwatch *this) {
    this->start = malloc(sizeof(struct timespec));
    this->stop = malloc(sizeof(struct timespec));
}

void Stopwatch_start(struct stopwatch *this) {
    clock_gettime(CLOCK_MONOTONIC_RAW, this->start);
}

void Stopwatch_stop(struct stopwatch *this) {
    clock_gettime(CLOCK_MONOTONIC_RAW, this->stop);
}

void Stopwatch_print(struct stopwatch *this) {
    int64_t stop_nsec = (int64_t) this->stop->tv_sec * 1000000000 + (int64_t) this->stop->tv_nsec;
    int64_t start_nsec = (int64_t) this->start->tv_sec * 1000000000 + (int64_t) this->start->tv_nsec;
    printf("Stopwatch: %"PRIi64" nanoseconds\n", stop_nsec - start_nsec);
}
