#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

struct thread_obj {
    struct thread_vtable *vtable;
    pthread_t *pthread;
};

struct thread_vtable {
    void *object_vtable;
    void *(*thread_run) (struct thread_obj*);
    void *(*thread_start) (struct thread_obj*);
    void *(*thread_join) (struct thread_obj*);
};



void *thread_entry(void *arg)
{
    struct thread_obj *this = arg;
    // call this.run()
    this->vtable->thread_run(this);
    pthread_exit(NULL);
}

void Thread_start(struct thread_obj *this)
{
    assert (this != NULL);

    this->pthread = malloc(sizeof(pthread_t));
    int rc = pthread_create(this->pthread, NULL, thread_entry, (void *)this);
    if (rc) {
        perror("pthread_create: ");
        exit(1);
    }
}

void Thread_run(struct thread_obj *this) {

}

void Thread_join(struct thread_obj *this) {
    int rc = pthread_join(*(this->pthread), NULL);
    if (rc) {
        perror("pthread_join: ");
        exit(1);
    }
}
