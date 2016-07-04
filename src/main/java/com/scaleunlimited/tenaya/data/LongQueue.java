package com.scaleunlimited.tenaya.data;

public class LongQueue {
	
	private long[] queue;
	private int size;
	private int head;
	private int tail;
	
	public LongQueue(int size) {
		queue = new long[size];
		this.size = size;
		head = 0;
		tail = 0;
	}
	
	public int size() {
		if (tail >= head) {
			return tail - head;
		} else {
			return tail + size - head + 1;
		}
	}
	
	public boolean add(long value) {
		if (!isFull()) {
			queue[tail] = value;
			tail = (tail + 1) % size;
			return true;
		} else {
			return false;
		}
	}
	
	public long get() {
		if (!isEmpty()) {
			long value = queue[head];
			head = (head + 1) % size;
			return value;
		} else {
			// TODO Should we fail more verbosely?
			return 0L;
		}
	}
	
	public void clear() {
		head = 0;
		tail = 0;
	}
	
	public boolean isEmpty() {
		return head == tail;
	}
	
	public boolean isFull() {
		return ((tail + 1) % size) == head;
	}

}
