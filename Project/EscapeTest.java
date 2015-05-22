class Custom extends Thread {
    Box arg;
    void run() {
        arg.lock();
        write(arg.val); writeln();
        arg.unlock();
    }
}

class Box {
    int val;
}

class Main {
    Thread escapeOne(Box b, Box e) {
        Custom t, t2;
        write(b.val); writeln();
        t = new Custom();
        t.arg = e;
        t.start();
        
        return t;
    }

    void main() {
        Thread t;
        Box b, e;
        
        b = new Box();
        e = new Box();
        b.val = 23;
        e.val = 42;
        
        t = escapeOne(b, e);
        e.lock();
        b.lock();
        t.join();
        e.unlock();
        b.unlock();

     }
}
