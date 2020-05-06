package middleware.logging;

import java.io.FileWriter;
import java.io.IOException;
import atlasdsl.GoalAction;

public class ATLASLog {
	private static ATLASLog logger = null;
	private FileWriter carsInboundLog;
	private FileWriter mqOutboundLog;
	private FileWriter goalLog;
	private FileWriter timeLog;
	
	ATLASLog() {
		try {
			carsInboundLog = new FileWriter("logs/atlasCARSInbound.log");
			mqOutboundLog = new FileWriter("logs/atlasMQOutbound.log");
			goalLog = new FileWriter("logs/goalLog.log");
			timeLog = new FileWriter("logs/atlasTime.log");
			timeLog.write("0.0\n");
			timeLog.flush();
			System.out.println("FileWriters created");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static ATLASLog getLog() {
		if (logger == null) {
			logger = new ATLASLog();
		} 
		return logger;
	}
	
	public static synchronized void logCARSInbound(String queueName, String text) {
		try {
			getLog().carsInboundLog.write("ActiveMQConsumer.handleMessage on " + queueName + " received textMessage: " + text + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void logActiveMQOutbound(String queueName, String text) {
		try {
			getLog().mqOutboundLog.write("ActiveMQProducer.send on " + queueName + " received textMessage: " + text + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void logGoalMessage(GoalAction ga, String msg) {
		try {
			String className = ga.getClass().getName();
			getLog().goalLog.write(className + "," + msg + "\n");
			// Ensure the stream is flushed as for some reason it wasn't 
			// ever updating the file, even though all the other log files are?
			// The writes should be infrequent though, so it shouldn't matter
			getLog().goalLog.flush();
			System.out.println("logGoalMessage:" + msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logCIInbound(String queueName, String text) {

	}
	
	public static synchronized void logTime(double timeVal) {
		try {
			getLog().timeLog.write(String.valueOf(timeVal) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
