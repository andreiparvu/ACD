class A {
  B b;
  int t1, t2;
}
class B {
  C c;
  int x;
}
class C {
  int a;
  int y;
}

class P {
  P next;
}

class Main {
  A var;

  void doit(A a, B b) {
    C c;
    c = new C();

    var = a;
    b.c = c;
    write(b.x);
  }

  void loop() {
    P p;
    p = new P();
    p.next = new P();

    p.next.next = p;
  }

  void main() {
    A a;
    B b;

    a = new A();
    b = new B();

    b.x = 3;

    doit(a, b);
  }
}
