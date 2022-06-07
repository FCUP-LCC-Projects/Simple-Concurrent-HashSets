import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.cooperari.config.*;
import org.cooperari.CSystem;
import org.cooperari.junit.CJUnitRunner;
import org.cooperari.core.scheduling.CProgramStateFactory;
import org.cooperari.core.scheduling.CSchedulerFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;


@RunWith(CJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@CMaxTrials(100) 
@CScheduling(schedulerFactory=CSchedulerFactory.OBLITUS, stateFactory=CProgramStateFactory.RAW)
public class SetTest2 {
  static Constructor<?> CTOR;
  static final int INITIAL_CAPACITY = 2;

  IHSet<String> set;
  
  @BeforeClass
  public static void globalSetup() throws Exception {
    Class<?> clazz = Class.forName(System.getenv("SUT_CLASS"));
    System.out.println("Testing " + clazz.getName() + " ...");
    CTOR = clazz.getDeclaredConstructor(Integer.TYPE); 
  }
  
  @SuppressWarnings("unchecked")
  @Before 
  public void setup() throws Exception {
    set = (IHSet<String>) CTOR.newInstance((Integer) INITIAL_CAPACITY);

  }

  
  @Test
  public void testWaitFor1() {
    CSystem.forkAndJoin(
        () -> { set.add("a"); },
        () -> { set.waitFor("a"); assertTrue(set.contains("a"));},
        () -> { set.waitFor("a"); assertTrue(set.contains("a"));},
        () -> { set.waitFor("a"); assertTrue(set.contains("a"));}
    );
  }
  
  @Test
  public void testWaitFor2() {
    CSystem.forkAndJoin(
        () -> { 
           set.add("a"); 
           set.waitFor("b"); assertTrue(set.contains("b")); 
           set.waitFor("c"); assertTrue(set.contains("c")); 
           set.waitFor("d"); assertTrue(set.contains("d")); 
        },
        () -> { 
          set.waitFor("a"); assertTrue(set.contains("a")); 
          set.add("b");
        },
        () -> { 
          set.waitFor("a"); assertTrue(set.contains("a")); 
          set.add("c");
        },
        () -> { 
          set.waitFor("a"); 
          assertTrue(set.contains("a")); 
          set.add("d");
        }
    );
  }
  
  @Test
  public void testWaitFor3() {
    StringBuffer sb = new StringBuffer();
    CSystem.forkAndJoin(
        () -> { 
           sb.append('a');
           set.add("a"); 
           set.waitFor("d"); ; 
           assertTrue(set.contains("d")); 
           set.waitFor("a");
        },
        () -> { 
          set.waitFor("a");
          assertTrue(set.contains("a")); 
          sb.append('b');
          set.add("b"); 
          set.waitFor("b");
          set.waitFor("a");
        },
        () -> { 
          set.waitFor("b");
          assertTrue(set.contains("b")); 
          sb.append('c');
          set.add("c"); 
          set.waitFor("c");
          set.waitFor("b");
        },
        () -> { 
          set.waitFor("c");
          assertTrue(set.contains("c"));
          sb.append('d');
          set.add("d"); 
          set.waitFor("d");
          set.waitFor("c");
        }
    );
    assertEquals("abcd", sb.toString());
  }
  
  
  @Test
  public void testRehash1() {
    CSystem.forkAndJoin(
       () -> {
         set.add("a", "b", "c", "d");
         Thread.yield();
         set.rehash();
         assertEquals(4, set.contains("a", "b", "c", "d"));
       },
       () -> {
         set.add("e", "f", "g");
         Thread.yield();
         set.rehash();
         assertEquals(3, set.contains("e", "f", "g"));
         Thread.yield();
         set.add("h");
       }
    );
    assertEquals(8, set.contains("a", "b", "c", "d", "e", "f", "g", "h"));
    assertEquals(4 * INITIAL_CAPACITY, set.capacity());
  }
  
  
  @Test
  public void testRehash2() {
    CSystem.forkAndJoin(
       () -> {
         set.add("a", "b", "c", "d");
         Thread.yield();
         set.rehash();
         Thread.yield();
         assertEquals(4, set.contains("a", "b", "c", "d"));
         Thread.yield();
         assertEquals(4, set.remove("a", "b", "c", "d"));
         Thread.yield();
         assertEquals(0, set.contains("a", "b", "c", "d"));
       },
       () -> {
         set.add("e", "f", "g");
         Thread.yield();
         set.rehash();
         Thread.yield();
         assertEquals(3, set.contains("e", "f", "g"));
         Thread.yield();
         assertEquals(3, set.remove("e", "f", "g"));
         Thread.yield();
         assertEquals(0, set.contains("e", "f", "g"));
         Thread.yield();
         set.add("h");
         Thread.yield();
         set.rehash();
         assertTrue(set.contains("h"));
       }
    );
    assertTrue(set.contains("h"));
    assertEquals(0, set.contains("a", "b", "c", "d", "e", "f", "g"));
    assertEquals(8 * INITIAL_CAPACITY, set.capacity());
  }
  
}

