package commsplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {
	int spawnedBotID = 0;

	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		RobotType toBuild = RobotType.POLITICIAN;
		int influence = 50;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				rc.buildRobot(toBuild, dir, influence);
			} else {
				break;
			}
		}
		System.out.println(rc.senseNearbyRobots(-1, rc.getTeam()));
		if (rc.senseNearbyRobots(-1, rc.getTeam()).length >= 1) {
			RobotInfo spawnedBot = rc.senseNearbyRobots(-1, rc.getTeam())[0];
			this.spawnedBotID = spawnedBot.getID();
		}
	}

}