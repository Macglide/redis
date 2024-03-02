package za.co.macglide.redis.service;

import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<K, V> {

    private final int capacity;
    private final ConcurrentHashMap<K, Node<V>> map;
    private final DoublyLinkedList<V> list;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>();
        this.list = new DoublyLinkedList<>();
    }

    public V get(K key) {
        Node<V> node = map.get(key);
        if (node != null) {
            list.moveToFront(node); // Update usage order
            return node.value;
        }
        return null;
    }

    public void set(K key, V value) {
        Node<V> existingNode = map.get(key);
        if (existingNode != null) {
            existingNode.value = value; // Update existing value
            list.moveToFront(existingNode); // Update usage order
        } else {
            Node<V> newNode = new Node<>(key, value);
            map.put(key, newNode);
            list.addLast(newNode);
            if (map.size() > capacity) {
                removeLeastRecentlyUsed();
            }
        }
    }

    private void removeLeastRecentlyUsed() {
        Node<V> removedNode = list.removeFirst();
        map.remove(removedNode.key);
    }

    private class Node<V> {

        final K key;
        V value;
        Node<V> prev;
        Node<V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private class DoublyLinkedList<V> {

        private Node<V> head;
        private Node<V> tail;

        public void addLast(Node<V> node) {
            if (isEmpty()) {
                head = tail = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
        }

        public Node<V> removeFirst() {
            if (isEmpty()) {
                throw new IllegalStateException("List is empty");
            }

            Node<V> removedNode = head;
            //if you have one node in the DLL, make it null.
            if (head == tail) {
                head = tail = null;
            } else {
                head = head.next;
                head.prev = null;
            }
            // sets the next and prev references of the removed node to null.
            // This fully removes it from the DLL by disconnecting its links.
            removedNode.next = removedNode.prev = null;
            return removedNode;
        }

        public void moveToFront(Node<V> node) {
            if (node == head) {
                return; // Already at the front
            }

            if (node == tail) {
                tail = node.prev;
                tail.next = null;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }

            node.prev = null;
            node.next = head;
            head.prev = node;
            head = node;
        }

        private boolean isEmpty() {
            return head == null;
        }
    }
}
