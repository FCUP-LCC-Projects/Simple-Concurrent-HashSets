import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SetTest1 {
  public static void main(String[] args) throws Exception {
    new SetTest1(args);
  }
  final CyclicBarrier barrier;
  final int T, N, OPS; 
  final IHSet<Integer> set;
  final Object PRINT_LOCK = new Object();
  final AtomicInteger errors = new AtomicInteger();
  final AtomicInteger expectedSetSize = new AtomicInteger();

  @SuppressWarnings("unchecked")
  SetTest1(String[] args) throws Exception {
    // Define the set
    Class<?> clazz = args.length < 1 ? HSet0.class : Class.forName(args[0]);
    // Number of threads
    T = args.length < 2 ? 8 : Integer.parseInt(args[1]);
    // Number of elements in set per thread
    N = 100;
    // Number of operations per thread
    OPS = 1000;
    set = (IHSet<Integer>) 
        clazz.getDeclaredConstructor(Integer.TYPE).newInstance((Integer)(T));
    System.out.println("Testing " + set.getClass().getName() + " ...");

    barrier = new CyclicBarrier(T + 1);
    for (int i = 0; i < T; i++) {
      final int id = i;
      new Thread(() -> run(id)).start();
    }
    barrier.await(); // sync on start
    barrier.await(); // sync before verification
    barrier.await(); // sync at the end
    if (expectedSetSize.get() != set.size()) {
      System.out.printf("Expected set size: %d, reported size is %d!%n", expectedSetSize.get(), set.size());
      errors.incrementAndGet();
    }
    if (errors.get() == 0) {
      System.out.println("all seems ok :)");
    } else {
      System.out.println("There were errors :(");
    }
  }
  
  void run(int id) {
    try {
      barrier.await(); 
      java.util.Set<Integer> mySet = new java.util.TreeSet<>();
      int min = id * N;
      int max = min + N;
      Random rng = new Random(id);
      for (int i = 0; i < OPS; i++) {
        int v = min + rng.nextInt(max - min);
        switch(rng.nextInt(10)) {
          case 0: 
            //System.out.println("+"+v);
            set.add(v); mySet.add(v); break;
          case 1: 
            //System.out.println(v);System.out.println("-" + v);
            set.remove(v); mySet.remove(v); break;   
          default:
            set.contains(rng.nextInt(T * N)); break; 
        }
      }
      barrier.await(); 
      expectedSetSize.getAndAdd(mySet.size());
      synchronized(PRINT_LOCK) {
        for (int i = min; i < max; i++) {
          if (mySet.contains(i) != set.contains(i)) {
            System.out.printf("thread %d: test failed for value %d%n", id, i);
            System.out.printf("thread %d: expects %s%n", id, mySet.toString());
            errors.incrementAndGet();
            break;
          }
        }
      }
      barrier.await();
    } 
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
