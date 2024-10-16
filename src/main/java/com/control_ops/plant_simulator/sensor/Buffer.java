package com.control_ops.plant_simulator.sensor;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Buffer<T> {
    private final long capacity;
    long size = 0;

    Buffer(final long capacity) {
        this.capacity = capacity;
    }

    Deque<T> elements = new LinkedList<>();

    public void add(T object) {
        if (size == this.capacity) {
            this.elements.removeFirst();
        } else {
            this.size++;
        }
        this.elements.addLast(object);
    }

    public List<T> exportCopy() {
        final List<T> copiedElements = new ArrayList<>(this.elements.size());
        copiedElements.addAll(this.elements);
        return copiedElements;
    }

    public long size() {
        return size;
    }
}
