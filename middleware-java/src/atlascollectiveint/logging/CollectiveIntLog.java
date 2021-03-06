package atlascollectiveint.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CollectiveIntLog {
	private static CollectiveIntLog logger = null;
	private FileWriter algorithmLog;
	private FileWriter messagesLog;
	private FileWriter generationLog;
	private FileWriter exceptionLog;
	
	CollectiveIntLog() {
		try {
			algorithmLog = new FileWriter("logs/CIAlgorithm.log");
			messagesLog = new FileWriter("logs/CIMessages.log");
			generationLog = new FileWriter("logs/CIGeneration.log");
			exceptionLog = new FileWriter("logs/CIException.log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static CollectiveIntLog getLog() {
		if (logger == null) {
			logger = new CollectiveIntLog();
		} 
		return logger;
	}
	
	public static synchronized void logCI(String text) {
		try {
			FileWriter l = getLog().algorithmLog;
			l.write(text + "\n");
			System.out.println(text+"\n");
			l.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void logCIMessage(String queueName, String text) {
		try {
			getLog().messagesLog.write("CI consumer on " + queueName + " received: " + text + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logCIGeneration(String queueName, String text) {
		try {
			FileWriter l = getLog().generationLog;
			l.write("CI producer on " + queueName + " sent: " + text + "\n");
			System.out.println(text + "\n");
			l.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logCIExceptions(Exception expt) {
		try {
			StringWriter sw = new StringWriter();
			expt.printStackTrace(new PrintWriter(sw));
			getLog().exceptionLog.append(sw.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}