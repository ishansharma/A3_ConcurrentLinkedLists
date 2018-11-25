package cll;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentList {
    Node head, tail;
    int size;

    /**
     * Node class that forms the basic block on List
     */
    public class Node {
        Integer item;
        int key;
        boolean marked;
        Node next;
        public Lock l = new ReentrantLock();

        /**
         * Default constructor to store an item of type T
         *
         * @param item Item to store
         */
        public Node(Integer item) {
            this.item = item;
            key = item.hashCode();
            marked = false;
            next = null;
        }

        @Override
        public String toString() {
            return item.toString();
        }
    }

    /**
     * Initialize an empty list
     */
    public ConcurrentList() {
        size = 0;
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }

    /**
     * Validation check to make sure that pred and curr are part of the list
     * and pred still points to curr
     * @param pred Predecessor of current node
     * @param curr Current node
     * @return True if window is still valid, false otherwise
     */
    public boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }

    /**
     * Decide if the list contains an item
     *
     * @param item Item to check for
     * @return True if list is present and not logically deleted(marked), false otherwise
     */
    public boolean contains(Integer item) {
        // if there are no items, return false without checking
        if (size < 0) {
            return false;
        }

        int key = item.hashCode();
        Node curr = head;
        while (curr.key < key) {
            curr = curr.next;
        }
        return curr.key == key && !curr.marked;
    }

    /**
     * Add an item to the list
     *
     * @param item Item to be added
     * @return True if item was inserted. False if item was not inserted
     */
    public boolean insert(Integer item) {
        int key = item.hashCode();
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.l.lock();
            try {
                curr.l.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key == key) {
                            return false;
                        } else {
                            Node node = new Node(item);
                            node.next = curr;
                            pred.next = node;
                            size++;
                            return true;
                        }
                    }
                } finally {
                    curr.l.unlock();
                }
            } finally {
                pred.l.unlock();
            }
        }
    }

    /**
     * Remove an item from the list
     *
     * @param item Item to be removed
     * @return True if item was removed, False if it wasn't present
     */
    public boolean delete(Integer item) {
        int key = item.hashCode();
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.l.lock();
            try {
                curr.l.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key != key) {
                            return false;
                        } else {
                            curr.marked = true;
                            pred.next = curr.next;
                            return true;
                        }
                    }
                } finally {
                    curr.l.unlock();
                }
            } finally {
                pred.l.unlock();
            }
        }
    }

    /**
     * Return list as a string
     *
     * @return string representation of the list
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Node curr = head.next;
        while (curr.next != null) {
            out.append(curr).append(" ");
            curr = curr.next;
        }
        return out.toString().trim();
    }
}
