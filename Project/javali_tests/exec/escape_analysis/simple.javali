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

  B var;
  
  void doit(int v1, int v2) {
    A a;
    B y, rat;
    C x, t;

    a = new A();
    rat = new B();
    
    a.t2 = 100;
    if (v1 + v2 <= 10) {
      a.b = rat;
      a.t1 = 10;
    } else {
      a.b = new B();
      a.t1 = 30;
    }

    a.b.c = new C();
    var = a.b;

    write(a.t1);
    write(a.t2);
  }

  void main() {
    doit(5, 40);
  }
}
