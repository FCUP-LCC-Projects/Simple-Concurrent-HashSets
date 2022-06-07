import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

public class HSet4<E> implements IHSet<E>{

  private static class Node<T> {
    T value;
    Ref.View<Node<T>> prev = STM.newRef(null);
    Ref.View<Node<T>> next = STM.newRef(null);
  }

  private Ref.View<TArray.View<Node<E>>> table;
  private final Ref.View<Integer> size;

  public HSet4(int h_size) {
    table = STM.newRef(STM.newTArray(h_size));
    size = STM.newRef(0); 
  }
  
  private Node<E> getEntry(E elem){
	  return table.get().apply(Math.abs(elem.hashCode() % capacity()));  
  }

  @Override
  public int capacity() {
    return table.get().length();
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean add(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(()-> {
    	if(table.get().length() != capacity()) {
    		Node<E> node = new Node<E>();
    		Node<E> head = table.get().apply(0);
    		node.value = elem;
    		node.next = STM.newRef(head);
    		head.prev = STM.newRef(node);
        	STM.increment(size, 1);
        	return true;
    	}
    	else return false;
    });
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
    	Node<E> node = getEntry(elem);
    	if(node != null) {
	    	Node<E> prev = node.prev.get();
	    	Node<E> next = node.next.get();
	    	prev.next = STM.newRef(next);
	    	next.prev = STM.newRef(prev);
	    	STM.increment(size, -1);
	    	return true;
	    }
    	else return false;
    });
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
    	if(getEntry(elem) != null) return true;
    	else return false;
    });
  }

  @Override
  public void waitFor(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    STM.atomic(() -> {
    	while(!contains(elem))
    		STM.retry();
    });
  }

  @Override
  public void rehash() {
    STM.atomic(() -> {
    	Ref.View<TArray.View<Node<E>>> oldTable = table;
    	table = STM.newRef(STM.newTArray(2 * oldTable.get().length()));
    	for(int i=0; i< oldTable.get().length(); i++) {
    		table.get().update(i, oldTable.get().apply(i));
    	}
    });
  }

}
