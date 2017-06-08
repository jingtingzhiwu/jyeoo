package com.wilkey.test;

public class ThreadTest {
	private static boolean stop = false;
	static Thread thread = null;
	public static void openThread() {
		 thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stop) {
					System.err.println("doing");
					try {
						Thread.sleep(1000 * 1);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		thread.run();
		System.err.println("over");
	}

	public static void main(String[] args) throws InterruptedException {

		 Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 3);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				stop = true;
				thread.interrupt();
			}
		});
		 t1.setDaemon(true);
		 t1.start();
		
		
		openThread();
		
		
	}
}
