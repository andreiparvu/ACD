class Main {
  void main() {
    int x;
    Semaphore s;
    T t1, t2, t3;

    s = new Semaphore();
    s.init(1);

    t1 = new T();
    //t2 = new T();
    t3 = new T();

    t1.sem = s;
    //t2.sem = s;
    t3.sem = s;

    t1.start();
    //t2.start();


    t1.join();
    //t2.join();

    t3.start();

    x = read();
    t1.sem.release();


    t3.join();
  }
}

class T extends Thread {
  Semaphore sem;

  void run() {
    sem.acquire();
    write(10);
    writeln();
  }
}

class Semaphore {
  int count, maxSize;

  void init(int count) {
    this.count = count;
  }

  void acquire() {
    this.lock();

    while (count == 0) {
      this.wait();
    }
    count = count - 1;

    this.unlock();
  }

  void release() {
    this.lock();

    count = count + 1;
    this.notify();

    this.unlock();
  }
}
