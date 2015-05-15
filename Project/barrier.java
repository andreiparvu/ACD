class Main {
  void main() {
    T[] threads;
    int i;
    Barrier b;
    int n;

    n = 5;

    threads = new T[n];
    b = new Barrier();
    b.init(n);

    i = 0;
    while (i < n) {
      threads[i] = new T();
      threads[i].barrier = b;
      threads[i].cnt = i;
      threads[i].start();

      i = i + 1;
    }

    i = 0;
    while (i < n) {
      threads[i].join();
      i = i + 1;
    }

    writeln();
  }
}

class T extends Thread {
  Barrier barrier;
  int cnt;

  void run() {
    int i;
    i = 0;

    while (i < 10) {
      write(cnt);
      barrier.wait();
      i = i + 1;
    }
  }
}

class Barrier {
  int numThreads;
  int cnt1, cnt2;
  Object counterLock;
  Semaphore tsem1, tsem2;

  void init(int numThreads) {
    this.numThreads = numThreads;
    cnt1 = numThreads;
    cnt2 = numThreads;
    counterLock = new Object();
    tsem1 = new Semaphore();
    tsem1.init(0);
    tsem2 = new Semaphore();
    tsem2.init(0);
  }

  void wait() {
    phase1();
    phase2();
  }

  void phase1() {
    int i;

    counterLock.lock();
    cnt1 = cnt1 - 1;
    if (cnt1 == 0) {
      i = 0;
      while (i < numThreads) {
        tsem1.release();

        i = i + 1;
      }
      cnt1 = numThreads;
    }
    counterLock.unlock();
    tsem1.acquire();
  }

  void phase2() {
    int i;

    counterLock.lock();
    cnt2 = cnt2 - 1;
    if (cnt2 == 0) {
      i = 0;
      writeln();
      while (i < numThreads) {
        tsem2.release();

        i = i + 1;
      }
      cnt2 = numThreads;
    }
    counterLock.unlock();
    tsem2.acquire();
  }
}

class Semaphore {
  int count;

  void init(int count) {
    this.count = count;
  }

  void acquire() {
    this.lock_cond();

    while (count == 0) {
      this.wait();
    }
    count = count - 1;

    this.unlock_cond();
  }

  void release() {
    this.lock_cond();

    count = count + 1;
    this.notify();

    this.unlock_cond();
  }
}
