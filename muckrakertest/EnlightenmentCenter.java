package muckrakertest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	boolean created = false;
	int influence = 1;
	int dirIndex;
	int slanderers = 0;

	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.MUCKRAKER;
		Direction dir = nav.randomDirection();
		if (rc.canBuildRobot(toBuild, dir, influence) && !created) {
			rc.buildRobot(toBuild, dir, influence);
			dirIndex++;
			slanderers++;
			if (slanderers == 9) {
				created = true;
			}
		}
	}

}