package cll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentListTest {
    @Test
    void add() {
        ConcurrentList l = new ConcurrentList();
        assertTrue(l.insert(1));
        assertTrue(l.insert(2));
        assertTrue(l.insert(3));
        assertFalse(l.insert(1));
        assertTrue(l.insert(0));
        assertEquals("0 1 2 3", l.toString());
    }

    @Test
    void contains() {
        ConcurrentList l = new ConcurrentList();
        assertFalse(l.contains(1));
        l.insert(1);
        assertTrue(l.contains(1));
        assertFalse(l.contains(10));
        l.insert(0);
        assertFalse(l.contains(10));
        assertTrue(l.contains(0));
    }

    @Test
    void remove() {
        ConcurrentList l = new ConcurrentList();
        l.insert(1);
        assertTrue(l.contains(1));
        l.delete(1);
        assertFalse(l.contains(1));
        l.insert(2);
        l.insert(3);
        l.insert(4);
        l.delete(3);
        assertFalse(l.contains(3));
        assertTrue(l.contains(2));
        assertTrue(l.contains(4));
    }

    @Test
    void string() {
        ConcurrentList l = new ConcurrentList();
        assertEquals("", l.toString());
        l.insert(1);
        assertEquals("1", l.toString());
    }

    @Test
    void replaceWithSame() {
        ConcurrentList l = new ConcurrentList();
        l.insert(1);
        l.insert(4);
        l.insert(6);
        assertTrue(l.replace(2, 2));
        assertEquals("1 2 4 6", l.toString());

        assertFalse(l.replace(4, 4));
        assertEquals("1 2 4 6", l.toString());
    }

    @Test
    void replaceWithDifferent() {
        ConcurrentList l = new ConcurrentList();
        l.insert(1);
        l.insert(2);
        l.insert(5);
        l.insert(10);

        assertTrue(l.replace(2, 7));
        assertEquals("1 5 7 10", l.toString());

        assertTrue(l.replace(7, 8));
        assertEquals("1 5 8 10", l.toString());
    }
}