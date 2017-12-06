package com.golaxy.converter.convert;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 线控线程，监控其他线程
 */
public class ThreadMonitor implements Runnable{
	public static ThreadMonitor instance;
	public static Thread instanceThread;
	private final static Logger logger = LoggerFactory.getLogger(ThreadMonitor.class);
	private Map<Thread, Runnable> monitoredThread = new ConcurrentHashMap<>(); // 要监控的线程

	public ThreadMonitor() {

	}
	
	public void add(Thread thread, Runnable target) {
		monitoredThread.put(thread, target);
	}
	
	@Override
	public void run() {			

	    while (true) {
	    	try {
	    		monitor();
	    		TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
            	logger.error("Exception!!", e);
                continue;
            }
	    }
	}
	
	private void monitor() {
		for (Entry<Thread, Runnable> entry : monitoredThread.entrySet()) {
			Thread thread = entry.getKey();
			Thread.State state = thread.getState();
			
			//logger.info(thread.getName() + " 线程状态: " + state);
			
			if (Thread.State.TERMINATED.equals(state)) {
				monitoredThread.remove(thread);
				logger.info(thread.getName() + "----------------------线程已终止,正在重启");
				Runnable target = entry.getValue();
				Thread newThread = new Thread(target);
				newThread.setName(thread.getName());
				newThread.start();
				monitoredThread.put(newThread, target);
			}
			
		}
//        for (int i = 0; i < monitoredThread.size(); i++) {
//            Thread.State state = monitoredThread.get(i).getState(); // 获得指定线程状态
//
//            logger.info(monitoredThread.get(i).getName() + " 线程状态: " + monitoredThread.get(i).getState());
//
//            /*
//             * 如果被监控线程为终止状态,则重启监控线程
//             */
//            if (Thread.State.TERMINATED.equals(state)) {
//                System.out.println(monitoredThread.get(i).getName() + " 线程已终止,正在重启");
//                monitoredThread.get(i).start(); //同一个县城只能start一次，第二次会报错IllegalThreadStateException，线程挂了需要再new新线程
//                monitoredThread.get(i).
//                Thread thread = new Thread();
//                thread.setName(monitoredThread.get(i).getName());
//                thread.start();
//
//            }
//        }
    }
	
	public static synchronized ThreadMonitor getInstance() {
		if (instance == null) {
			instance = new ThreadMonitor();
			instanceThreadStart(instance);
		}
		return instance;
	}
	
	public static synchronized void instanceThreadStart(ThreadMonitor instance) {
		if (instanceThread == null) {
			instanceThread = new Thread(instance);
			instanceThread.setName("Monitor");
			instanceThread.start();
		}
	}
}
