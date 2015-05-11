class Main {
  LinkedList list;
  int size;

  void init(int size) {
    int s;
    Node curr;
    FloatBox f;
    this.size = size;
    list = new LinkedList();
    list.head = new Node();
    s = 0;
    curr = list.head;
    while (s < size) {
      curr.next = new Node();
      f = new FloatBox();
      f.value = 0.0;
      curr.payload = f;
      curr = curr.next;
      s = s + 1;
    }
  }


  void main() {
    init(3);
  }
}

class LinkedList {
  Node head;
}

class Node {
  Node next;
  FloatBox payload;
}

class FloatBox {
  float value;
}

