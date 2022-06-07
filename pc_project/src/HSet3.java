import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import scala.concurrent.stm.japi.STM;


public class HSet3<E> implements IHSet<E>{
	private LinkedList<E>[] table;
	private int lockSize;
	private final ReentrantReadWriteLock[] locks;
	private final Condition[] contain;
	
	public HSet3(int ht_size){
		table = createTable(ht_size);
	    lockSize = ht_size;
	    locks = new ReentrantReadWriteLock[lockSize];
	    contain = new Condition[lockSize];
	    
	    for(int i=0; i<lockSize; i++) {
	    	locks[i] = new ReentrantReadWriteLock();
	    	contain[i] = locks[i].writeLock().newCondition();
	    }
	}
	
	private LinkedList<E> getEntry(E elem) {
	    return table[Math.abs(elem.hashCode() % lockSize)];
	 }
	
	private int getIndex(E elem) {
		return Math.abs(elem.hashCode() % lockSize);
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
		int size = 0;
		for(ReentrantReadWriteLock l : locks)
			l.readLock().lock();
		try {
			for(LinkedList<E> l : table)
				size += l.size();
			return size;
		}finally {
			for(ReentrantReadWriteLock l : locks)
				l.readLock().unlock();
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
		int index = getIndex(elem);
		locks[index].writeLock().lock();
		try {
			LinkedList<E> list = getEntry(elem);
		    boolean r = ! list.contains(elem);
		    if(r) {
		        list.addFirst(elem);
		    	contain[index].signalAll();
		    }
		    return r;
		}
		finally {
			locks[index].writeLock().unlock();
		}
	}

	@Override
	public boolean remove(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		int index = getIndex(elem);
		locks[index].writeLock().lock();
		try {
			boolean r = getEntry(elem).remove(elem);
			
			return r;
		}finally {
			locks[index].writeLock().unlock();
		}
	}

	@Override
	public boolean contains(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		int index = getIndex(elem);
		locks[index].readLock().lock();
		try {
			return getEntry(elem).contains(elem);
		}finally {
			locks[index].readLock().unlock();
		}
	}
	
	@Override
	public void waitFor(E elem) {
		if (elem == null) {
		      throw new IllegalArgumentException();
		}
		int index = getIndex(elem);
		locks[index].writeLock().lock();
		try {
			while(!contains(elem))
				try {
					contain[index].await();
				}
				catch(InterruptedException e) {
				e.printStackTrace();
				}
		}finally {
			locks[index].writeLock().unlock();
		}
	}

	@Override
	public void rehash() {
		for(ReentrantReadWriteLock l : locks)
			l.writeLock().lock();
		try {

			LinkedList<E>[] oldTable = table;
			table = createTable(2 * oldTable.length);
			for (LinkedList<E> list : oldTable) 
				for (E elem : list ) 
					getEntry(elem).add(elem);	          
		}
		finally {
			for(ReentrantReadWriteLock l : locks)
				l.writeLock().unlock();
		}
	}


}

