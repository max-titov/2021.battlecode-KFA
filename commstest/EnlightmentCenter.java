package commstest;

import battlecode.common.*;

public class EnlightmentCenter extends Robot {

	boolean created = false;
	int influence = 50;

	public EnlightmentCenter(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.SLANDERER;
		Direction dir = randomDirection();
		if (rc.canBuildRobot(toBuild, dir, influence) && !created) {
			rc.buildRobot(toBuild, dir, influence);
		}
	}

}