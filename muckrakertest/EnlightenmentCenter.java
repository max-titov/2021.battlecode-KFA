package muckrakertest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	boolean created = false;
	int influence = 50;

	public EnlightenmentCenter(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.MUCKRAKER;
		Direction dir = Direction.NORTHWEST;
		if (rc.canBuildRobot(toBuild, dir, influence) && !created) {
			rc.buildRobot(toBuild, dir, influence);
			created = true;
		}
	}

}