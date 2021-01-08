package navtest;

import battlecode.common.*;

public class EnlightmentCenter extends Robot {

	public EnlightmentCenter(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = randomSpawnableRobotType();
		int influence = 50;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				rc.buildRobot(toBuild, dir, influence);
			} else {
				break;
			}
		}
	}

}