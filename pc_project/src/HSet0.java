import java.util.LinkedList;

public class HSet0<E> implements IHSet<E>{

  private LinkedList<E>[] table;
  private int size;

  /**
   * Constructor.
   * @param ht_size Initial size for internal hash table.
   */
  public HSet0(int ht_size) {
    table = createTable(ht_size);
    size = 0;
  }
  
  
  // Auxiliary method to return the list where
  // an element should be stored.
  private LinkedList<E> getEntry(E elem) {
    return table[Math.abs(elem.hashCode() % table.length)];
  }

  // Auxiliary method to create the hash table.
  private LinkedList<E>[] createTable(int ht_size) {
    @SuppressWarnings("unchecked")
    LinkedList<E>[] t = (LinkedList<E>[]) new LinkedList[ht_size];
    for (int i = 0; i < t.length; i++) {
      t[i] = new LinkedList<>();
    }
    return t;
  }
 
  @Override
  public int capacity() {
    return table.length;
  }
  
  @Override
  public int size() {
    synchronized (this) {
      return size;
    }
  }

  @Override
  public boolean add(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      LinkedList<E> list = getEntry(elem);
      boolean r = ! list.contains(elem);
      if (r) {
        list.addFirst(elem);
        notifyAll(); // there may threads waiting in waitEleme
        size++;
      }
      return r;
    }
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {    
      boolean r = getEntry(elem).remove(elem);
      if (r) {
        size--;
      }
      return r;
    }
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      return getEntry(elem).contains(elem);
    }
  }

  @Override
  public void waitFor(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    synchronized(this) {
      while (! getEntry(elem).contains(elem)) {
        try {
          wait();
        }
        catch(InterruptedException e) { 
          // Ignore interrupts
        }
      }
    }
  }
  
  @Override
  public void rehash() {
    synchronized (this) {
      LinkedList<E>[] oldTable = table;
      table = createTable(2 * oldTable.length);
      for (LinkedList<E> list : oldTable) {
        for (E elem : list ) {
          getEntry(elem).add(elem);
        }
      }
    }
  }
}
