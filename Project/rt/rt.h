#include <pthread.h>

#ifndef RT_H
#define RT_H

struct object {
    void *object_vtable;

    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
};

struct thread {
    struct thread_vtable *vtable;

    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
    pthread_t *thread;
};

struct stopwatch {
    struct stopwatch_vtable *vtable;
    pthread_mutex_t *mutex;
    pthread_cond_t *cond;
    struct timespec *start;
    struct timespec *stop;
};

struct thread_vtable {
    void *object_vtable;

    void *(*object_lock) (struct object*);
    void *(*object_unlock) (struct object*);
    void *(*object_wait) (struct object*);
    void *(*object_notify) (struct object*);

    void *(*thread_run) (struct thread*);
    void *(*thread_start) (struct thread*);
    void *(*thread_join) (struct thread*);
};

struct stopwatch_vtable {
    void *object_vtable;

    void *(*object_lock) (struct object*);
    void *(*object_unlock) (struct object*);
    void *(*object_wait) (struct object*);
    void *(*object_notify) (struct object*);

    void *(*stopwatch_init) (struct stopwatch*);
    void *(*stopwatch_start) (struct stopwatch*);
    void *(*stopwatch_stop) (struct stopwatch*);
    void *(*stopwatch_print) (struct stopwatch*);
};



#endif /* RT_H */
