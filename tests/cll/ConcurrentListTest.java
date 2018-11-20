package cll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentListTest {

    @Test
    void find() throws Exception {
        Integer[] baseArr = new Integer[]{0, 1, 2, 6, 7, 8, 10};
        ConcurrentList<Integer> l = new ConcurrentList<>(baseArr);
        l.add(2);
        l.add(3);
        assertEquals(new Integer(1), l.find(2).pred.item);
        assertEquals(new Integer(2), l.find(2).curr.item);
    }

    @Test
    void add() throws Exception {
        Integer[] baseArr = new Integer[]{0, 1, 2, 6, 7, 8, 10};
        ConcurrentList<Integer> l = new ConcurrentList<>(baseArr);
        assertFalse(l.add(2));
        assertTrue(l.add(3));
    }

    @Test
    void remove() throws Exception {
        Integer[] baseArr = new Integer[]{0, 1, 2, 6, 7, 8, 10};
        ConcurrentList<Integer> l = new ConcurrentList<>(baseArr);
        assertTrue(l.remove(2));
        assertEquals("0 1 6 7 8 10", l.toString());
    }
}