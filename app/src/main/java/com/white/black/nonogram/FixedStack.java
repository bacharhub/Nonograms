package com.white.black.nonogram;

import java.util.LinkedList;
import java.util.List;

public class FixedStack<T> {

    private final int size;
    private final List<T> list;

    public FixedStack(int size) {
        this.size = size;
        this.list = new LinkedList<>();
    }

    public void push(T t) {
        this.list.add(t);
        if (this.list.size() > this.size) {
            this.list.remove(0);
        }
    }

    public boolean empty() {
        return list.size() == 0;
    }

    public T pop() {
        T t = null;
        if (list.size() > 0) {
            t = list.get(list.size() - 1);
            list.remove(list.size() - 1);
        }

        return t;
    }

    public void clear() {
        list.clear();
    }

    public int getSize() {
        return this.size;
    }
}
