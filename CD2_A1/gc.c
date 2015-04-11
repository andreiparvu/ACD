#include <stdio.h>
#include <stdlib.h>

#define MAX_HASH 666013

typedef struct _alloc_entry {
  int *ptr;
  int marked;
  int size;

  struct _alloc_entry *next, *prev;
} alloc_entry;


typedef struct _list {
  alloc_entry *head, *tail;
} list;

list mem_alloc, stack_vars;

alloc_entry *ptr_to_list[MAX_HASH], *ptr_to_stack[MAX_HASH];

void init(alloc_entry *elem, void *ptr, int size) {
  elem->ptr = ptr;
  elem->size = size;
  elem->marked = 0;
  elem->next = elem->prev = NULL;
}

void add(list *l, int *ptr, int size) {
  alloc_entry *elem = malloc(sizeof(alloc_entry));

  init(elem, ptr, size);

  if (l->head == NULL) {
    l->head = elem;
  } else {
    l->tail->next = elem;
    elem->prev = l->tail;
  }
  l->tail = elem;

  if (l == &mem_alloc) {
    ptr_to_list[((unsigned int)ptr) % MAX_HASH] = elem;
  } else {
    ptr_to_stack[((unsigned int)ptr) % MAX_HASH] = elem;
  }
}

void delete(list *l, alloc_entry *elem) {
  if (elem->prev == NULL) {
    l->head = elem->next;
  } else {
    elem->prev->next = elem->next;
  }
  if (elem->next == NULL) {
    l->tail = elem->prev;
  } else {
    elem->next->prev = elem->prev;
  }

  free(elem);
}

alloc_entry* find(int *val) {
  return ptr_to_list[(unsigned int)val % MAX_HASH];
}

void mark(alloc_entry *entry) {
  int i;

  for (i = 0; i < entry->size - 1; i++) {
    int *val = (int*)*(entry->ptr + i);

    alloc_entry *next_entry = find(val);

    if (next_entry != NULL) {
      if (next_entry->marked) {
        continue;
      }

      next_entry->marked = 1;
      fprintf(stderr, "should inspect %p\n", next_entry->ptr);
      mark(next_entry);
    }
  }
}

void sweep() {
  alloc_entry *cur;

  for (cur = mem_alloc.head; cur != NULL; cur = cur->next) {
    if (cur->marked == 1) {
      cur->marked = 0;
    } else {
      fprintf(stderr, "just deleted %p\n", cur->ptr);
      free(cur->ptr);
      // should delete this
    }
  }
}
void cleanup() {
  alloc_entry *start;

  for (start = stack_vars.head; start != NULL; start = start->next) {
    mark(start);
  }

  sweep();
}

void add_vars(void *a, int size) {
  int *x = a;

  // add one in order to be compatible with struct
  add(&stack_vars, x, size + 1);
}

void delete_vars(void *a) {
  delete(&stack_vars, ptr_to_stack[(unsigned int)a % MAX_HASH]);
}

void* myalloc(int size) {
  void *newptr = malloc(size);

  add(&mem_alloc, newptr, size / 4);

  fprintf(stderr, "allocated %p with size %d\n", newptr, size);

  return newptr;
}