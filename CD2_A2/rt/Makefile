OBJS = threads.o

CFLAGS += -m32

.PHONY: all clean

all: librt.a

librt.a: $(OBJS) 
	ar -rcs librt.a $(OBJS)

clean:
	rm -f librt.a $(OBJS)
