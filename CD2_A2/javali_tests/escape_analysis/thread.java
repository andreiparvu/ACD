class Main {
  void main() {
   doit();
  }

  Pair doit() {
    A t;
    Pair p;

    t = new A();
    t.p = new Pair();
    t.p2 = new Pair();
    t.p2.x = 3;
    t.p2.y = 10;

    p = new Pair();

    t.p3 = p;

    t.start();

    return p;
  }

  void foo() {
    B b;
    b = new B();

    b.pb = new Pair();

    b.start();
  }
}

class Foo {
  Pair next;
}

class Pair {
  int x, y;
}

class B extends Thread {
  Pair pb;

  void start() {
    B[] b;

    b = new B[100];

    b[0] = this;
  }
}

class A extends Thread {
  Pair p, p2, p3;

  void start() {
    Foo[] x;

    x = new Foo[10];

    x[3].next = p;

    write(p.x + p2.x);
    write(p.y + p2.y);
  }
}

