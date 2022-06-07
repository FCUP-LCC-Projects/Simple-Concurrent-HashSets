import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class HSet1<E> implements IHSet<E> {
	
	private LinkedList<E>[] table;
	private int size;
	private final ReentrantLock reentrantLock;
	private final Condition contain;
	
	public HSet1(int h_size){
		table = createTable(h_size);
	    size = 0;
		this.reentrantLock = new ReentrantLock();
		contain = reentrantLock.newCondition();
	}
	
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
	public int size() {
		try {
			reentrantLock.lock();
			return size;
		}
		finally{
			reentrantLock.unlock();
		}
	}

	@Override
	public int capacity() {
		return table.length;
	}

	@Override
	public boolean add(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		reentrantLock.lock();
		try {
			LinkedList<E> list = getEntry(elem);
		    boolean r = ! list.contains(elem);
		    if(r) {
		        list.addFirst(elem);
		    	contain.signalAll();
		    	size++;
		    }
		    return r;
		}
		finally {
			reentrantLock.unlock();
		}
	}

	@Override
	public boolean remove(E elem) {
		reentrantLock.lock();
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		try {
			boolean r = getEntry(elem).remove(elem);
			if(r)
				size--;
			return r;
		}finally {
			reentrantLock.unlock();
		}
	}

	@Override
	public boolean contains(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		reentrantLock.lock();
		try {
			return getEntry(elem).contains(elem);
		}finally {
			reentrantLock.unlock();
		}
	}

	@Override
	public void waitFor(E elem)  {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		reentrantLock.lock();
		try {
			while(!contains(elem))
				try {
					contain.await();
				}
				catch(InterruptedException e) {
				e.printStackTrace();
				}
		}finally {
			reentrantLock.unlock();
		}
	}

	@Override
	public void rehash() {
		try {
			reentrantLock.lock();
			LinkedList<E>[] oldTable = table;
			table = createTable(2 * oldTable.length);
			for (LinkedList<E> list : oldTable) 
				for (E elem : list ) 
					getEntry(elem).add(elem);	          
		}
		finally {
			reentrantLock.unlock();
		}
	}

}

