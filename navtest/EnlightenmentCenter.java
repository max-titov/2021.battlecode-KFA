package navtest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	boolean created = false;
	int influence = 21;

	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.SLANDERER;
		Direction dir = nav.randomDirection();
		if (rc.canBuildRobot(toBuild, dir, influence)) {
			rc.buildRobot(toBuild, dir, influence);
		}
	}

}