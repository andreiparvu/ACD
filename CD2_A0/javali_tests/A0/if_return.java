class Main {
  void main() {
    int a;
    int b;

    a = 3;

    if (a == 3) {
      a = 4;
      return ;
    }

    if (a == 4) {
      b = 5;
      return ;
    } else {
      a = 4;
    }

    if (a == 4) {
      b = 5;
    } else {
      a = 4;

      return ;
    }

    if (a == 4) {
      b = 5;
      return ;
    } else {
      a = 4;
      return ;
    }

    a = a + b;
  }
} 

