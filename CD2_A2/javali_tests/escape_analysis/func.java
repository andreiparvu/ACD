class Main {
  void main() {
    A a, c, d;
    B b;
    int[] arr;

    a = new A();
    b = new B();
    arr = new int[100];

    b.bar(a.foo());

    c = new A();
    d = new A();

    arr[c.v] = d.foo();
  }
}

class A {
  int v;

  int foo() {
    return 4;
  }
}

class B {
  int bar(int c) {
    return 10;
  }
}

