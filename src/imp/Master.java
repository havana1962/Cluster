package imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import model.Job;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import stat.RamData;

public class Master extends Thread{
	final static int argsSize = 2;
	private Scanner sc;
	private RamData rd;
	private List<Job> jobs;
	private List<Thread> threads;
	private String[] words;
	
	public Master() throws SigarException{
		sc = new Scanner(System.in);
		rd = new RamData(new Sigar());
		jobs = new ArrayList<Job>();
		threads = new ArrayList<Thread>();
	}
	
	public void run(){
		int c;
		try {
			while((c = analyseLine())!=0){
				switch(c){
				case 1:
					jobList();
					break;
				case 2:
					createJob(words);
					break;
				case 3:
					killJob(words[1]);
					break;
				default: System.err.println("unknown command...");
				}
				displayThreads();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void displayThreads() {
		for(Thread t : threads){
			System.out.println(t.getId() + " " + t.getState());
		}
		
	}

	private int analyseLine() throws Exception {
		String line = sc.nextLine();
		if(line.isEmpty())
			return -1;
		
		words = line.split(" ");

		/*for(int i=0;i<words.length;i++)
			System.out.println(words[i] + " ");*/
		
		if(line.equals("exit")){
			System.out.println("############## end of the Master ##############");
			return 0;
		}
		else if(line.equals("list")){
			System.out.println("List of jobs:");
			return 1;
		}
		else if(words[0].equals("launch")){
			System.out.println("Launching a job");
			return 2;
		}
		else if(words[0].equals("kill")){
			System.out.println("Killing of job...");
			return 3;
		}
		else{
			System.err.println("unknown command...");
			return -1;
		}
	}
	
	private void createJob(String[] words) throws Exception {
		if(testLaunchArgs(words)){
			Job j = addJob(words);
			if(enoughtRAM(words[3]))
				launchJob(j);
			else
				System.err.println("No enought memory at the moment to launch your job. We are sorry...");
		}
		else
			System.err.println("Invalid arguments for launching job. We are sorry...");
	}
	
	private Job addJob(String[] words) {
		Job j = new Job(words[1],words[2], Long.parseLong(words[3]));
		jobs.add(j);
		return j;
	}

	@SuppressWarnings("deprecation")
	private void killJob(String name) throws InterruptedException {
		for(Job j : jobs){
			if(j.getName().equals(name)){
				j.setState(model.State.Stoped);
				for(Thread t : threads){
					if(t.getName().equals(name)){
							t.stop();						
					}
				}
					
			}
		}
	}

	private void jobList() {
		// TODO Auto-generated method stub
		
	}

	private void launchJob(Job j) {
		Thread t = new Thread(new Runnable() {			
			public void run() {
					try {
						Thread.sleep(Long.parseLong(j.getTime())*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
			}
		});
		t.setName(j.getName());
		threads.add(t);		
		t.start();
	}

	private long currentMemory() throws Exception{
		rd.startMetricTest();
		return Long.parseLong(rd.getFreeMemory());
	}
	
	private boolean enoughtRAM(String ram) throws Exception{
		if(currentMemory() - Long.parseLong(ram) > 0)
			return true;
		return false;
	}

	private boolean testLaunchArgs(String[] words) {
		String regexJobName = "^[a-zA-Z]{3,10}";
		if(!Pattern.matches(regexJobName, words[1])){
			System.err.println("invalid job name");
			return false;
		}
		
		String regexTimeValue = "^[0-9]{1,10}";
		if(!Pattern.matches(regexTimeValue, words[2])){
			System.err.println("wrong time value");
			return false;
		}
		
		String regexRAMValue = "^[0-9]{1,10}";
		if(!Pattern.matches(regexRAMValue, words[3])){
			System.out.println("wrong RAM value");
			return false;
		}	
		
		return true;
	}

	

}
