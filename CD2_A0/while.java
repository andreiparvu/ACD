class Main {
  void main() {
    int a;
    int b;

    if (a <= 3) {
      while (a <= 3) {
        a = a + b;
      }
    } else {
      if (a == 5) {
        a = smth();
      }
    }

    write(a);
    writeln();
  }

  int smth() {
    int a;

    a = 3;

    return a + 1;
  }
}
