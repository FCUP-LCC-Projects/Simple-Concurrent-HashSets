/**
 * 
 * Interface for a set. 
 *
 * @param <E> Type of elements in the set.
 */
public interface IHSet<E> {

  /**
   * Get number of elements in the set.
   */
  int size();

  /**
   * Get size internal hash table. 
   */
  int capacity();

  /**
   * Add an element to the set.
   * @param elem The element to add.
   * @throws IllegalArgumentException if <code>elem == null</code>.
   * @return <code>true</code> if the element was added to the set,
   *  <code>false</code> if the set already contained the element.
   */
  boolean add(E elem);
  
  /**
   * Remove an element from the set.
   * @param elem The element to remove.
   * @throws IllegalArgumentException if <code>elem == null</code>.
   * @return <code>true</code> if the element was removed from the set,
   *  <code>false</code> if the set does not contain the element.
   */
  boolean remove(E elem);
  
  /**
   * Check if an element belongs to the set.
   * @param elem The element to search.
   * @throws IllegalArgumentException if <code>elem == null</code>.
   * @return <code>true</code> if the element is in the set,
   *  <code>false</code> otherwise.
   */
  boolean contains(E elem);
  
  /**
   * Wait for an element to be added to the set.
   * 
   * It should return immediately if the element is in the set,
   * otherwise it should block the calling thread until the element
   * is added.
   * 
   * Interrupts sent to the calling thread should be ignored.
   * 
   * @param elem The element to wait.
   * @throws IllegalArgumentException if <code>elem == null</code>.
   * @return <code>true</code> if the element is in the set,
   *  <code>false</code> otherwise.
   */
  void waitFor(E elem);
  
  /**
   * Re-hash the set. This should duplicate the size of the internal
   * hash table being used.
   */
  void rehash();
  
  // Utility methods to add, remove, and test several elements at once
  
  /**
   * Add several elements.
   * @param elems Elements to add.
   * @return Number of elements effectively added to the set.
   */
  @SuppressWarnings("unchecked")
  default int add(E... elems) {
    int n = 0;
    for (E elem : elems) n += add(elem) ? 1 : 0;
    return n;
  }
  
  /**
   * Remove several elements.
   * @param elems Elements to remove.
   * @return Number of elements effectively removed from  the set.
   */
  @SuppressWarnings("unchecked")
  default int remove(E... elems) {
    int n = 0;
    for (E elem : elems) n += remove(elem) ? 1 : 0;
    return n;
  }
  
  /**
   * Checks if elements are contained in the set.
   * @param elems Elements.
   * @return Number of elements contained removed in  the set.
   */
  @SuppressWarnings("unchecked")
  default int contains(E... elems) {
    int n = 0;
    for (E elem : elems) n += contains(elem) ? 1 : 0;
    return n;
  }
}
