package cll;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentList {
    Node head, tail;
    AtomicInteger size;

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
        size = new AtomicInteger(0);
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
        if (size.intValue() < 0) {
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
                            size.incrementAndGet();
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
                            size.decrementAndGet();
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
     * Given two keys, replace old with new
     *
     * @param oldItem Old item
     * @param newItem New Item
     * @return True if old item was removed or new item was added. False otherwise
     */
    public boolean replace(Integer oldItem, Integer newItem) {
        int k_new = newItem.hashCode();
        int k_old = oldItem.hashCode();
        while (true) {
            Node old_pred = head, old_curr = head.next, new_pred = head, new_curr = head.next;
            // if old and new are same, we need to find where k_old is/should be
            // if it's there, don't do anything.
            // if it's not there, insert k_new
            if (k_old == k_new) {
                while (old_curr.key < k_old) {
                    old_pred = old_curr;
                    old_curr = old_curr.next;
                }
                old_pred.l.lock();
                try {
                    old_curr.l.lock();
                    try {
                        if (validate(old_pred, old_curr)) {
                            // if the key is already there, we don't need to do anything
                            if (old_curr.key == k_old) {
                                return false;
                            } else {
                                Node node = new Node(newItem);
                                node.next = old_curr;
                                old_pred.next = node;
                                size.incrementAndGet();
                                return true;
                            }
                        }
                    } finally {
                        old_curr.l.unlock();
                    }
                } finally {
                    old_pred.l.unlock();
                }
            } else {
                boolean oldDeleted, newAdded;
                // in case old and new aren't same, find where k_old is/should be
                // find where k_new is/should be and act accordingly
                while (old_curr.key < k_old || new_curr.key < k_new) {
                    // update only if we haven't found a position for old_curr
                    if (old_curr.key < k_old) {
                        old_pred = old_curr;
                        old_curr = old_curr.next;
                    }

                    // update only if we haven't found new_curr
                    if (new_curr.key < k_new) {
                        new_pred = new_curr;
                        new_curr = new_curr.next;
                    }
                }

                old_pred.l.lock();
                old_curr.l.lock();
                new_pred.l.lock();
                new_curr.l.lock();
                try {
                    if (validate(old_pred, old_curr) && validate(new_pred, new_curr)) {
                        // add new key if it's not present
                        if (new_curr.key == k_new) {
                            newAdded = false;
                        } else {
                            Node node = new Node(newItem);
                            node.next = new_curr;
                            new_pred.next = node;
                            size.incrementAndGet();
                            newAdded = true;
                        }

                        // remove old key if it's present
                        if (old_curr.key != k_old) {
                            oldDeleted = false;
                        } else {
                            old_curr.marked = true;
                            old_pred.next = old_curr.next;
                            size.decrementAndGet();
                            oldDeleted = true;
                        }

                        // if we did any of the two operations successfully, return true
                        return oldDeleted || newAdded;
                    }
                } finally {
                    old_pred.l.unlock();
                    old_curr.l.unlock();
                    new_pred.l.unlock();
                    new_curr.l.unlock();
                }
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
