#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <inttypes.h>
#include <time.h>
#include <pthread.h>

#ifndef CLOCK_MONOTONIC_RAW
// for Linux 2.6.28 and older
#define CLOCK_MONOTONIC_RAW CLOCK_MONOTONIC
#endif

struct stopwatch {
    struct stopwatch_vtable *vtable;
    pthread_mutex_t *mutex, *cond_mutex;
    pthread_cond_t *cond;
    struct timespec *start;
    struct timespec *stop;
};

struct stopwatch_vtable {
    void *object_vtable;
    void *(*stopwatch_init) (struct stopwatch*);
    void *(*stopwatch_start) (struct stopwatch*);
    void *(*stopwatch_stop) (struct stopwatch*);
    void *(*stopwatch_print) (struct stopwatch*);
};

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
