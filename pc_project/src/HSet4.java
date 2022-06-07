import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

public class HSet4<E> implements IHSet<E>{

  private static class Node<T> {
    T value;
    Ref.View<Node<T>> prev = STM.newRef(null);
    Ref.View<Node<T>> next = STM.newRef(null);
  }

  private final Ref.View<TArray.View<Node<E>>> table;
  private final Ref.View<Integer> size;

  public HSet4(int h_size) {
    table = STM.newRef(STM.newTArray(h_size));
    size = STM.newRef(0); 
  }

  
  private Node<E> getEntry(E elem){
	  return table.get().apply(getIndex(elem));  
  }
  
  private int getIndex(E elem) {
	  return Math.abs(elem.hashCode() % table.get().length());
  }
  
  private boolean nodeContains(Node<E> start, E elem) {
	  return STM.atomic(()->{ 
		 Node<E> node = start;
		  while(node != null) {
			  if(node.value.equals(elem)) return true;
			  node = node.next.get();
		  }
		  return false;
	  });
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
    		Node<E> oldNode = getEntry(elem);
    		boolean r = !contains(elem);
    		if(r) {
        		Node<E> node = new Node<E>();
        		node.value = elem;
        		node.next.set(oldNode); 
        		if(oldNode !=null) oldNode.prev.set(node); 
        		table.get().update(getIndex(elem), node);
            	STM.increment(size, 1);
    		}
        	return r;
    });
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() -> {
		Node<E> oldNode = getEntry(elem);
		while(oldNode != null) {
			if(oldNode.value.equals(elem)) {
	    	Node<E> prev = oldNode.prev.get();
	    	Node<E> next = oldNode.next.get();

	    	if(next != null) { next.prev.set(prev); }
	    	if(prev != null) { prev.next.set(next);  }
	    	else { table.get().update(getIndex(elem), next); }

    		STM.increment(size, -1);
	    	return true;
			}
			oldNode = oldNode.next.get();
    	}
    	return false;
    });
  }
  
  @Override
  public boolean contains(E elem) {
	  if (elem == null) {
	      throw new IllegalArgumentException();
	    }
    return STM.atomic(() -> {
    	return nodeContains(getEntry(elem), elem);
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
    	TArray.View<Node<E>> oldTable = table.get();
    	table.set(STM.newTArray(2 * oldTable.length()));
    	
    	for(int i=0; i<oldTable.length(); i++) {
    		Node<E> head = oldTable.apply(i);
    		while(head!=null) {
    			add(head.value);
    			head = head.next.get();
    		}
    	}
    });
  }

}
