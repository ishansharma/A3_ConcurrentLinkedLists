package cll;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class ConcurrentList<T> {
    Node head;

    /**
     * Default constructor for creating empty list.
     * Will create a dummy node for head
     */
    public ConcurrentList(T[] arr) throws Exception {
        // TODO: Remove requirement for initializing with items. Need to handle the null cases at head and tail for that
        if (arr.length < 3) {
            throw new Exception("Minimum array length of 3 required");
        }
        head = new Node(arr[0]);
        Node curr = head;
        Node nextNode;

        for (int i = 1; i < arr.length; i++) {
            nextNode = new Node(arr[i]);
            curr.next = new AtomicMarkableReference<>(nextNode, false);
            curr = curr.next.getReference();
        }
    }

    /**
     * Window class to help with find() operations
     */
    public class Window {
        public Node pred, curr;

        Window(Node myPred, Node myCurr) {
            pred = myPred;
            curr = myCurr;
        }
    }

    /**
     * Node class that forms the basic block on List
     */
    public class Node {
        T item;
        int key;
        AtomicMarkableReference<Node> next;

        /**
         * Default constructor to store an item of type T
         *
         * @param item Item to store
         */
        public Node(T item) {
            this.item = item;
            key = new Integer(item.toString());
        }

        @Override
        public String toString() {
            return Integer.toString(key);
        }
    }

    /**
     * Find a node in the list
     *
     * @param key Key we are looking for
     * @return Window that contains previous node and current node for the key
     */
    public Window find(int key) {
        Node pred = null, succ = null, curr = null;
        boolean[] marked = {false};
        boolean snip;
        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key) {
                    return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    /**
     * Add an item to the list
     *
     * @param item Item to be added
     * @return False if an item already exists
     */
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window window = find(key);
            Node pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            } else {
                Node node = new Node(item);
                node.next = new AtomicMarkableReference<>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Node curr = head;

        while (curr.next != null) {
            out.append(curr.next.getReference().toString()).append(" ");
            curr = curr.next.getReference();
        }

        return out.toString();
    }
}
