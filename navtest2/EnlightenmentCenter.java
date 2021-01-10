package navtest2;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	public EnlightenmentCenter(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();

		if (rc.canBuildRobot(RobotType.MUCKRAKER, nav.E, 50)) {
			rc.buildRobot(RobotType.MUCKRAKER, nav.E, 50);
		}
	}

}