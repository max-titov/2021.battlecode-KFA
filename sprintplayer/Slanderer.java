package sprintplayer;

import battlecode.common.*;

public class Slanderer extends Robot {

	public Slanderer(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		if (nav.tryMove(nav.randomDirection()))
			System.out.println("I moved!");
	}

}