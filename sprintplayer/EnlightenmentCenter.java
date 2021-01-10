package sprintplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	public EnlightenmentCenter(RobotController r) {
		super(r);
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