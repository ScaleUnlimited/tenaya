package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class LongQueueTest {

	@Test
	public void testQueue() {
		LongQueue queue = new LongQueue(100);
		queue.add(10);
		queue.add(20);
		queue.add(30);
		assertEquals("Last long in should be the first one out", 10, queue.get());
	}
	
	@Test
	public void testQueueOverflow() {
		LongQueue queue = new LongQueue(1);
		queue.add(1);
		assertEquals("Queue should not allow overflow", false, queue.add(2));
	}
	
}
