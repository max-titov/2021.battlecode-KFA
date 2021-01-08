package navtest;

import battlecode.common.*;

public class Muckraker extends Robot {

	public Muckraker(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		nav.tryMoveToTarget(new MapLocation(10026, 23940));
	}

}