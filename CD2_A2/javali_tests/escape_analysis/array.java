class P {
  P next;
}

class A {
  P[] x;
}

class Main {
  void main() {
    A a;
    P[] p;

    a = new A();
    a.x = new P[2];

    a.x[2] = new P();
    a.x[2].next = new P();

    p = new P[100];

    p[3] = new P();
    p[5].next = new P();
  }

  void doit(A a) {
    a.x[3].next = new P();
  }
}
