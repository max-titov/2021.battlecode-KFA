package navtest2;

import battlecode.common.*;

public class EnlightmentCenter extends Robot {

	public EnlightmentCenter(RobotController r) {
		super(r);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();

		if (rc.canBuildRobot(RobotType.MUCKRAKER, nav.E, 50)) {
			rc.buildRobot(RobotType.MUCKRAKER, nav.E, 50);
		} 
	}

}