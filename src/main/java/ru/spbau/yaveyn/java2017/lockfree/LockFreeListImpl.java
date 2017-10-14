package ru.spbau.yaveyn.java2017.lockfree;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeListImpl<T> implements LockFreeList<T> {

    private final AtomicReference<Node> head = new AtomicReference<>(null);

    private void checkedDeleteNext(Node current) {
        Node next = current.getNext();
        if (next == null || !next.isDeleted()) return;
        current.trySetNext(next, next.getNext());
    }

    @Override
    public boolean isEmpty() {
        while(true) {
            Node current = head.get();
            if (current == null) return true;
            if (!current.isDeleted()) return false;
            head.compareAndSet(current, current.getNext());
        }
    }

    @Override
    public void append(T value) {
        while(true) {
            Node newNode = new Node(value, head.get());
            if (head.compareAndSet(newNode.getNext(), newNode)) return;
        }
    }

    @Override
    public boolean remove(T value) {
        Node current = head.get();
        while (current != null) {
            checkedDeleteNext(current);
            if (!current.isDeleted() && current.getValue().equals(value)) {
                while (!current.isDeleted()) {
                    if (current.tryDelete(current.getNext())) return true;
                }
            }
            current = current.getNext();
        }
        return false;
    }

    @Override
    public boolean contains(T value) {
        Node current = head.get();
        while (current != null) {
            checkedDeleteNext(current);
            if (!current.isDeleted() && value.equals(current.getValue())) return true;
            current = current.getNext();
        }
        return false;
    }

    private class Node {
        private final T value;
        private final AtomicMarkableReference<Node> nextAndIsDeleted;

        Node(T value, Node next) {
            this.value = value;
            this.nextAndIsDeleted = new AtomicMarkableReference<>(next, false);
        }

        boolean tryDelete(Node expectedNext) {
            return nextAndIsDeleted.compareAndSet(expectedNext, expectedNext, false, true);
        }

        T getValue() {
            return value;
        }

        Node getNext() {
            return nextAndIsDeleted.getReference();
        }

        boolean isDeleted() {
            return nextAndIsDeleted.isMarked();
        }

        boolean trySetNext(Node expectedNext, Node newNext) {
            boolean currentMark = nextAndIsDeleted.isMarked();
            return nextAndIsDeleted.compareAndSet(expectedNext, newNext, currentMark, currentMark);
        }
    }

}
