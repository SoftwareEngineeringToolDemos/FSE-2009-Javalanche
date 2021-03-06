package de.unisb.cs.st.javalanche.mutation.runtime.testDriver;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;


public class MutationTestDriverTest {

	private static class MyTestDriver extends MutationTestDriver {

		public MyTestDriver() {
			timeout = 1;
		}

		@Override
		protected List<String> getAllTests() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected MutationTestRunnable getTestRunnable(String testName) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static class EndlessThread implements MutationTestRunnable {

		private int n;

		private AtomicReference<EndlessThread> child = new AtomicReference<EndlessThread>();

		private AtomicReference<Thread> childThread = new AtomicReference<Thread>();

		private AtomicBoolean finished = new AtomicBoolean(false);

		private String message;

		public EndlessThread() {
			n = 0;
		}

		public EndlessThread(int n) {
			this.n = n;
		}

		public void run() {
			Thread ct = Thread.currentThread();
			System.out.println("Start " + n + " Daemon " + ct.isDaemon() + "  "
					+ ct.getId());

			if (n == 0) {
				EndlessThread et = new EndlessThread(n + 1);
				Thread t2 = new Thread(et);
				t2.setDaemon(false);
				t2.start();
				child.set(et);
				childThread.set(t2);
			}
			boolean b = n != 0;
			int count = 1;
			while (b) {
				if (count % 3 == 0) {
					System.out.println(count + "   " + this);
				}
				count++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			finished.set(true);
		}

		public EndlessThread getChild() {
			return child.get();
		}

		public Thread getChildThread() {
			return childThread.get();
		}

		public SingleTestResult getResult() {
			return null;
		}

		public boolean hasFinished() {
			return finished.get();
		}


		public synchronized boolean hasFailed() {
			return message != null;
		}

		@Override
		public void setFailed(String message, Throwable e) {
			this.message = message;
		}

	}

	@Test
	public void testThreadStartsOtherThread() throws InterruptedException {
		MyTestDriver m = new MyTestDriver();
		MutationTestListener mock = createMock(MutationTestListener.class);
		m.addMutationTestListener(mock);
		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		long[] threadsPre = threadMxBean.getAllThreadIds();
		EndlessThread e = new EndlessThread();
		m.runWithTimeout(e);
		Thread.sleep(500);
		e.getChild();
		// assertFalse(e.getChild().hasFinished());
		assertFalse(e.getChildThread().isAlive());
		long[] threadsPost = threadMxBean.getAllThreadIds();
		Long[] pre = ArrayUtils.toObject(threadsPre);
		Long[] post = ArrayUtils.toObject(threadsPost);
		// assertThat(pre, eq(post));
		assertThat(pre, equalTo(post));
		assertFalse(e.hasFailed());
	}

	@Test
	public void testEndlessLoopThread() throws InterruptedException {
		MyTestDriver m = new MyTestDriver();
		final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		long[] threadsPre = threadMxBean.getAllThreadIds();
		EndlessThread e = new EndlessThread(1);
		m.runWithTimeout(e);
		Thread.sleep(500);
		long[] threadsPost = threadMxBean.getAllThreadIds();
		assertThat(threadsPost.length - threadsPre.length, is(0));
		assertTrue(e.hasFailed());
	}
}
