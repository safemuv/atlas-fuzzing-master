package atlasdsl;

import java.util.ArrayList;
import java.util.List;

public class Mission {
	// TODO: change to DSL, propagate back into spec
	private List<Robot> robots = new ArrayList<Robot>();
	private List<Computer> computers = new ArrayList<Computer>();
	
	public List<Robot> getAllRobots() {
		return robots;
	}
	
	public boolean includesComputer() {
		return (computers.size() > 0); 
	}
	
	public void addRobot(Robot r) {
		robots.add(r);
	}
}