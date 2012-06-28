/*
 * 
 */
package muscle.util.data;

import muscle.util.data.FastArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author jborgdo1
 */
public class FastArrayListTest {
	
	public FastArrayListTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of size method, of class FastArrayList.
	 */
	@Test
	public void testSize() {
		System.out.println("size");
		FastArrayList<String> instance = new FastArrayList<String>(2);
		assertEquals(0, instance.size());
		
		instance.add("new");
		assertEquals(1, instance.size());
		instance.add("new");
		assertEquals(2, instance.size());
		instance.add("new");
		assertEquals(3, instance.size());
	}

	/**
	 * Test of isEmpty method, of class FastArrayList.
	 */
	@Test
	public void testIsEmpty() {
		System.out.println("isEmpty");
		FastArrayList<String> instance = new FastArrayList<String>();
		assertTrue(instance.isEmpty());
		instance.add("new");
		assertFalse(instance.isEmpty());
	}

	/**
	 * Test of contains method, of class FastArrayList.
	 */
	@Test
	public void testContains() {
		System.out.println("contains");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("something"); instance.add("somethingElse");
		assertTrue(instance.contains("somethingElse"));
		assertTrue(instance.contains("something"));
	}

	/**
	 * Test of iterator method, of class FastArrayList.
	 */
	@Test
	public void testIterator() {
		System.out.println("iterator");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("something"); instance.add("somethingElse"); instance.add("yetSomethingElse");
		Iterator result = instance.iterator();
		assertTrue(result.hasNext());
		assertEquals("something", result.next());
		assertTrue(result.hasNext());
		assertEquals("somethingElse", result.next());
		result.remove();
		assertTrue(result.hasNext());
		assertEquals("yetSomethingElse", result.next());
		assertFalse(result.hasNext());
	}

	/**
	 * Test of toArray method, of class FastArrayList.
	 */
	@Test
	public void testToArray_0args() {
		System.out.println("toArray");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("something"); instance.add("somethingElse");
		Object[] expResult = new String[] {"something", "somethingElse"};
		Object[] result = instance.toArray();
		assertArrayEquals(expResult, result);
	}

	/**
	 * Test of toArray method, of class FastArrayList.
	 */
	@Test
	public void testToArray_GenericType() {
		System.out.println("toArray");
		String[] ts = new String[0];
		String[] ts2 = new String[2];
		FastArrayList<String> instance = new FastArrayList<String>();	
		instance.add("something"); instance.add("somethingElse");
		Object[] expResult = new String[] {"something", "somethingElse"};
		assertArrayEquals(expResult, instance.toArray(ts));
		assertArrayEquals(expResult, instance.toArray(ts2));
	}

	/**
	 * Test of add method, of class FastArrayList.
	 */
	@Test
	public void testAdd_GenericType() {
		System.out.println("add");
		assertTrue(new FastArrayList<String>().add("lala"));
	}
	
	/**
	 * Test of add method, of class FastArrayList.
	 */
	@Test
	public void testAddAll_GenericType() {
		System.out.println("addAll");
		FastArrayList<String> eqList = new FastArrayList<String>();
		eqList.add("some stuff"); eqList.add("some more");

		ArrayList<String> list = new ArrayList<String>();
		list.add("some stuff"); list.add("some more");
		FastArrayList<String> fastList = new FastArrayList<String>();
		fastList.addAll(list);

		assertEquals(eqList, fastList);
		
		Set<String> set = new HashSet<String>();
		set.add("solala"); set.add("soooo");
		fastList.addAll(set);
		assertTrue(fastList.indexOf("solala") >= 2);
		assertTrue(fastList.indexOf("soooo") >= 2);
	}

	/**
	 * Test of remove method, of class FastArrayList.
	 */
	@Test
	public void testRemove_Object() {
		System.out.println("remove");
		Object o = null;
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("lala"); instance.add("lalalala");
		assertTrue(instance.remove("lala"));
		assertFalse(instance.remove("e"));
		assertEquals(1, instance.size());
	}

	/**
	 * Test of clear method, of class FastArrayList.
	 */
	@Test
	public void testClear() {
		System.out.println("clear");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this");
		instance.clear();
		assertTrue(instance.isEmpty());
	}

	/**
	 * Test of get method, of class FastArrayList.
	 */
	@Test
	public void testGet() {
		System.out.println("get");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this");
		assertEquals("this", instance.get(1));
		assertEquals("now", instance.get(0));
	}

	/**
	 * Test of set method, of class FastArrayList.
	 */
	@Test
	public void testSet() {
		System.out.println("set");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this");
		String result = instance.set(1, "thistoo");
		assertEquals("this", result);
		assertEquals("thistoo", instance.get(1));
	}

	/**
	 * Test of add method, of class FastArrayList.
	 */
	@Test
	public void testAdd_int_GenericType() {
		System.out.println("add");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this");
		instance.add(1, "thistoo");
		assertEquals("thistoo", instance.get(1));
		assertEquals("this", instance.get(2));
	}

	/**
	 * Test of remove method, of class FastArrayList.
	 */
	@Test
	public void testRemove_int() {
		System.out.println("remove");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this"); instance.add("thistoo");
		Object result = instance.remove(1);
		assertEquals("this", result);
		assertEquals("thistoo", instance.get(1));
	}

	/**
	 * Test of indexOf method, of class FastArrayList.
	 */
	@Test
	public void testIndexOf() {
		System.out.println("indexOf");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this"); instance.add("thistoo"); instance.add("this");
		assertEquals(1, instance.indexOf("this"));
		assertEquals(-1, instance.indexOf("thiees"));
	}

	/**
	 * Test of lastIndexOf method, of class FastArrayList.
	 */
	@Test
	public void testLastIndexOf() {
		System.out.println("lastIndexOf");
		FastArrayList<String> instance = new FastArrayList<String>();
		instance.add("now"); instance.add("this"); instance.add("thistoo"); instance.add("this");
		assertEquals(3, instance.lastIndexOf("this"));
		assertEquals(-1, instance.indexOf("thiees"));
	}
}
