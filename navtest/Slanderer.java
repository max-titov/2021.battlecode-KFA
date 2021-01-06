package navtest;

import battlecode.common.*;

public class Slanderer extends Robot {

	public Slanderer(RobotController r) {
		super(r);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		if (tryMove(randomDirection()))
			System.out.println("I moved!");
	}

}