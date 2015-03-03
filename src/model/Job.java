package model;

public class Job {
	private String name;
	private String time;
	private long ram;
	private int cpu;
	private int hdd;
	private State state;
	
	public Job(String name, String time, long ram){
		this.name = name;
		this.time = time;
		this.ram = ram;
		/*this.cpu = cpu;
		this.hdd = hdd;*/
		this.state = State.Wainting;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public long getRam() {
		return ram;
	}

	public void setRam(long ram) {
		this.ram = ram;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getHdd() {
		return hdd;
	}

	public void setHdd(int hdd) {
		this.hdd = hdd;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}	
}
