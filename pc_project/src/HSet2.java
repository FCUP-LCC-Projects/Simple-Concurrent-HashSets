import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


public class HSet2<E> implements IHSet<E>{
	private LinkedList<E>[] table;
	private int size;
	private final ReentrantReadWriteLock lock;
	private final Condition contain;

	public HSet2(int ht_size) {
		table = createTable(ht_size);
	    size = 0;
		this.lock = new ReentrantReadWriteLock();
		contain = lock.writeLock().newCondition();
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
			lock.readLock().lock();
			return size;
		}finally {
			lock.readLock().unlock();
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
		lock.writeLock().lock();
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
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean remove(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		lock.writeLock().lock();
		try {
			boolean r = getEntry(elem).remove(elem);
			if(r)
				size--;
			return r;
		}finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean contains(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		lock.readLock().lock();
		try {
			return getEntry(elem).contains(elem);
		}finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void waitFor(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		lock.writeLock().lock();
		try {
			while(!contains(elem))
				try {
					contain.await();
				}
				catch(InterruptedException e) {
				e.printStackTrace();
				}
		}finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void rehash() {
		try {
			lock.writeLock().lock();
			LinkedList<E>[] oldTable = table;
			table = createTable(2 * oldTable.length);
			for (LinkedList<E> list : oldTable) 
				for (E elem : list ) 
					getEntry(elem).add(elem);	          
		}
		finally {
			lock.writeLock().unlock();
		}
	}

}


