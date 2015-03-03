package imp;

import java.util.ArrayList;
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
	private long ramAvailable;
	
	public Master() throws SigarException, InterruptedException{
		sc = new Scanner(System.in);
		rd = new RamData(new Sigar());
		jobs = new ArrayList<Job>();
		threads = new ArrayList<Thread>();
		rd.startMetricTest();
		setRamAvailable(Long.parseLong(rd.getFreeMemory())/100*80);
		
		System.out.println(getRamAvailable());
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
				//displayThreads();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*private void displayThreads() {
		for(Thread t : threads){
			System.out.println(t.getId() + " " + t.getState());
		}	
	}*/

	private int analyseLine() throws Exception {
		String line = sc.nextLine();
		if(line.isEmpty())
			return -1;
		
		words = line.split(" ");
		
		if(line.equals("exit")){
			System.out.println("############## Cluster stopped ##############");
			return 0;
		}
		else if(line.equals("list"))
			return 1;
		
		else if(words[0].equals("launch"))
			return 2;
		
		else if(words[0].equals("kill"))
			return 3;
		
		
		return -1;
	}
	
	private void createJob(String[] words) throws Exception {
		if(testLaunchArgs(words)){
			Job j = addJob(words);
			System.out.println(words[0] + " is waiting to be launched...");
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
	private void killJob(String name) throws Exception {
		if(name.isEmpty()){
			System.err.println("Missing name...");
			return;
		}
		
		for(Job j : jobs){
			if(j.getName().equals(name)){
				j.setState(model.State.Stoped);
				for(Thread t : threads){
					if(t.getName().equals(name)){
							t.stop();	
							System.out.println("@ " + name + " is stopped!");
							setRamAvailable(getRamAvailable()+j.getRam());
							searchJob();
					}
				}
					
			}
		}
	}

	private void searchJob() throws Exception{
		for(Job j : jobs){
			if(j.getState().equals(model.State.Wainting)){ 
				if(enoughtRAM(Long.toString(j.getRam())))
					launchJob(j);
			}
		}
	}

	private void jobList() {
		System.out.println("======================");
		System.out.println("List of Cluster jobs: ");
		System.out.println("----------------------");
		for(Job j: jobs){
			System.out.println(j.getName() + " : " + j.getState());
		}
		System.out.println("----------------------");
		System.out.println("RAM available = " + getRamAvailable());
		System.out.println("======================");

	}

	private void launchJob(Job j) {
		System.out.println("@ Launching " + j.getName() + " ...");
		j.setState(model.State.Running);
		Thread t = new Thread(new Runnable() {			
			public void run() {
					try {
						Thread.sleep(Long.parseLong(j.getTime())*1000);
						j.setState(model.State.Finished);
						System.out.println("@ " + j.getName() + " finished.");
						setRamAvailable(getRamAvailable()+j.getRam());
						searchJob();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				
			}
		});
		t.setName(j.getName());
		threads.add(t);		
		t.start();
	}
	
	private boolean enoughtRAM(String ram) throws Exception{
		long tmp = getRamAvailable() - Long.parseLong(ram);
		if(tmp > 0){
			setRamAvailable(tmp);
			return true;
		}
		return false;
	}

	private boolean testLaunchArgs(String[] words) throws Exception{
		if(words.length != 4){
			System.err.println("@ Missing arguments...");
			return false;
		}
		
		String regexJobName = "^[a-zA-Z0-9]{3,10}";
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

	public long getRamAvailable() {
		return ramAvailable;
	}

	public void setRamAvailable(long ramAvailable) {
		this.ramAvailable = ramAvailable;
	}

}
