package cll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentListTest {
    @Test
    void add() {
        ConcurrentList l = new ConcurrentList();
        assertTrue(l.add(1));
        assertTrue(l.add(2));
        assertTrue(l.add(3));
        assertFalse(l.add(1));
        assertTrue(l.add(0));
        assertEquals("0 1 2 3", l.toString());
    }

    @Test
    void contains() {
        ConcurrentList l = new ConcurrentList();
        assertFalse(l.contains(1));
        l.add(1);
        assertTrue(l.contains(1));
        assertFalse(l.contains(10));
        l.add(0);
        assertFalse(l.contains(10));
        assertTrue(l.contains(0));
    }

    @Test
    void remove() {
        ConcurrentList l = new ConcurrentList();
        l.add(1);
        assertTrue(l.contains(1));
        l.remove(1);
        assertFalse(l.contains(1));
        l.add(2);
        l.add(3);
        l.add(4);
        l.remove(3);
        assertFalse(l.contains(3));
        assertTrue(l.contains(2));
        assertTrue(l.contains(4));
    }

    @Test
    void string() {
        ConcurrentList l = new ConcurrentList();
        assertEquals("", l.toString());
        l.add(1);
        assertEquals("1", l.toString());
    }
}