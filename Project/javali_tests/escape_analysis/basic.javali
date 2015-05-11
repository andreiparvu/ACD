class A {
  B b;
}
class B {
  C c;
  int x;
}
class C {
  int a;
  int y;
}

class Main {

  void main() {
    A a;
    B y;
    C x, t;

    a = new A();
    a.b = new B();
    a.b.c = new C();

    y = new B();
    x = new C();

    y.c = x;

    a.b.c = x;
    t = a.b.c;
  }
}
