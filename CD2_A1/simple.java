class Main {
    int gg;
    void main() {
        A a;

        a = new A();
        a.x = 3;
        a.y = 3.4;
        a.b = new B();
    }
}

class A {
    int x;
    float y;
    B b;
}

class B {
}

