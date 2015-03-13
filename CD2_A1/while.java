class Main {

  void main() {
    int a;
    int b, c;

    a = 3;
    b = 5;
    c = 0;
    if (a <= 3) {
      c = 3;
      while (a <= 3) {
        a = a + b;
      }
    } else {
      if (a == 5) {
        a = 3;
      }
    }

    write(a);
    writeln();
  }
}
