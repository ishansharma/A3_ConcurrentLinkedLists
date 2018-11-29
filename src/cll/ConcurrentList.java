package cll;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("Duplicates")
public class ConcurrentList {
    private Node head, tail;
    private AtomicInteger size;

    /**
     * Node class that forms the basic block on List
     */
    public class Node {
        private int key;
        private boolean marked;
        private boolean ongoingReplace;
        private Integer item;
        private Node next;
        private Lock l = new ReentrantLock();

        /**
         * Default constructor to store an item of type Integer
         *
         * @param item Item to store
         */
        public Node(Integer item) {
            this.item = item;
            key = item.hashCode();
            marked = false;
            ongoingReplace = false;
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
     * Validation check to make sure that pred and curr are part of the list, pred
     * still points to curr, and the pred and/or curr does not overlap with an
     * ongoing Replace operation window
     *
     * @param pred      Predecessor of current node
     * @param curr      Current node
     * @param isRegular The type of validation, regular validation or replace
     *                  validation
     * @return True if window is still valid, false otherwise
     */
    private boolean validateWindow(Node pred, Node curr, boolean isRegular) {
        if (!isRegular)
            return !pred.ongoingReplace && !pred.marked && !curr.ongoingReplace && !curr.marked && pred.next == curr;

        return !pred.marked && !curr.marked && pred.next == curr;
    }

    /**
     * A helper method for inserting an element in the list
     *
     * @param pred Predecessor of current node
     * @param curr Current node
     * @param item The item to be inserted in the list
     * @return True if item is not present in the list, false otherwise
     */
    private boolean insertHelper(Node pred, Node curr, Integer item) {
        if (curr.key == item.hashCode())
            return false;

        Node node = new Node(item);
        node.next = curr;
        pred.next = node;
        size.incrementAndGet();
        return true;
    }

    /**
     * A helper method for deleting an element from the list
     *
     * @param pred Predecessor of current node
     * @param curr Current node
     * @return True if item is present in the list, false otherwise
     */
    private boolean deleteHelper(Node pred, Node curr, int key) {
        if (curr.key != key) {
            return false;
        } else {
            curr.marked = true;
            pred.next = curr.next;
            size.decrementAndGet();
            return true;
        }
    }

    /**
     * Decide if the list contains an item
     *
     * @param item Item to check for
     * @return True if list is present and not logically deleted(marked), false
     * otherwise
     */
    public boolean contains(Integer item) {

        // if there are no items, return false without checking
        if (size.intValue() <= 0) {
            return false;
        }

        int key = item.hashCode();
        Node curr = head;

        while (curr.key < key) {
            curr = curr.next;
        }

        return curr.key == key && !curr.marked && !curr.ongoingReplace;
    }

    /**
     * Default insert operation
     *
     * @param item Item to be inserted
     * @return True if item was inserted, false otherwise
     */
    public boolean insert(Integer item) {
        return insert(item, false);
    }

    /**
     * Insert an item to the list
     *
     * @param item      Item to be added
     * @param inReplace Normal insertion or an insertion within a Replace
     * @return True if item was inserted. False if item was not inserted
     */
    public boolean insert(Integer item, boolean inReplace) {
        int key = item.hashCode();
        Node pred, curr;

        while (true) {
            pred = head;
            curr = head.next;

            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }

            pred.l.lock();
            try {
                curr.l.lock();
                try {
                    if (validateWindow(pred, curr, inReplace)) {
                        return insertHelper(pred, curr, item);
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
     * Default delete for compatibility with outside classes
     *
     * @param item Item to be deleted
     * @return True if item was deleted, false otherwise
     */
    public boolean delete(Integer item) {
        return delete(item, false);
    }

    /**
     * Remove an item from the list
     *
     * @param item      Item to be removed
     * @param inReplace Normal Deletion or deletion within a replace
     * @return True if item was deleted, False if it wasn't present
     */
    public boolean delete(Integer item, boolean inReplace) {
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
                    if (validateWindow(pred, curr, inReplace)) {
                        return deleteHelper(pred, curr, key);
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
        int k_old = oldItem.hashCode();
        int k_new = newItem.hashCode();
        boolean output;
        Node new_pred, new_curr;

        if (k_old <= k_new) {
            output = delete(oldItem, true);
            output = output | insert(newItem, true);
        } else {
            output = insert(newItem, true);
            output = output | delete(oldItem, true);
        }

        while (true) {
            new_pred = head;
            new_curr = head.next;

            while (new_curr.key != k_new) {
                new_pred = new_curr;
                new_curr = new_curr.next;
            }

            new_pred.l.lock();
            try {
                new_curr.l.lock();
                try {
                    new_curr.ongoingReplace = false;
                    return output;

                } finally {
                    new_curr.l.unlock();
                }
            } finally {
                new_pred.l.unlock();
            }
        }
    }

    /**
     * Returns the size of list
     *
     * @return int
     */
    public int size() {
        return size.get();
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
            out.append(curr.key).append(" ");
            curr = curr.next;
        }
        return out.toString().trim();
    }
}