package io.github.xivqn.entities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TabuMemo {

    Queue<Integer> tabuQueue;
    Set<Integer> tabuSet;
    int maxSize;

    public TabuMemo(int maxSize) {
        this.maxSize = maxSize;
        this.tabuQueue = new LinkedList<>();
        this.tabuSet = new HashSet<>();
    }

    public boolean isTabu(int item) {
        return tabuSet.contains(item);
    }

    public void add(int item) {
        if (maxSize <= 0) {
            return;
        }
        if (tabuQueue.size() >= maxSize) {
            int removed = tabuQueue.poll();
            tabuSet.remove(removed);
        }
        tabuQueue.offer(item);
        tabuSet.add(item);
    }

    public void clear() {
        tabuQueue.clear();
        tabuSet.clear();
    }

    public int getTenure() {
        return tabuSet.size();
    }

}
