OBJS = threads.o stopwatch.o

CFLAGS += -m32 -Wall -Werror

.PHONY: all clean

all: libjavali.a

libjavali.a: $(OBJS) 
	ar -rcs libjavali.a $(OBJS)

clean:
	rm -f libjavali.a $(OBJS)
