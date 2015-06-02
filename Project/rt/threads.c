#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

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

static void *thread_entry(void *arg) {
    struct thread *this = arg;
    this->vtable->thread_run(this); // calls this.run()
    pthread_exit(NULL);
}

void Object__init__(struct object *this) {
    this->mutex = malloc(sizeof(pthread_mutex_t));
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
    int rc = pthread_mutex_init(this->mutex, &attr);
    if (rc) {
        perror("pthread_mutex_init: ");
        exit(1);
    }
    pthread_mutexattr_destroy(&attr);

    this->cond = malloc(sizeof(pthread_cond_t));
    rc = pthread_cond_init(this->cond, NULL);
    if (rc) {
      perror("pthread_cond_init: ");
      exit(1);
    }
}

static void lock(pthread_mutex_t *mutex, char *error_msg) {
    int rc = pthread_mutex_lock(mutex);
    if (rc) {
        perror(error_msg);
        exit(1);
    }
}

static void unlock(pthread_mutex_t *mutex, char *error_msg) {
    int rc = pthread_mutex_unlock(mutex);
    if (rc) {
        perror(error_msg);
        exit(1);
    }
}

void Object_lock(struct object *this) {
    lock(this->mutex, "pthread_mutex_lock: ");
}

void Object_unlock(struct object *this) {
    unlock(this->mutex, "pthread_mutex_unlock: ");
}

void Object_wait(struct object *this) {
  int rc = pthread_cond_wait(this->cond, this->mutex);
  if (rc) {
    perror("pthread_cond_wait: ");
    exit(0);
  }
}

void Object_notify(struct object *this) {
  int rc = pthread_cond_signal(this->cond);
  if (rc) {
    perror("pthread_cond_signal: ");
    exit(0);
  }
}

void Thread_start(struct thread *this) {
    assert (this != NULL);

    this->thread = malloc(sizeof(pthread_t));
    int rc = pthread_create(this->thread, NULL, thread_entry, (void *)this);
    if (rc) {
        perror("pthread_create: ");
        exit(1);
    }
}

void Thread_run(struct thread *this) {

}

void Thread_join(struct thread *this) {
    int rc = pthread_join(*(this->thread), NULL);
    if (rc) {
        perror("pthread_join: ");
        exit(1);
    }
}
