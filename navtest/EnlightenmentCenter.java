package navtest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	boolean created = false;
	int influence = 21;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER };


	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = randomSpawnableRobotType();
		Direction dir = nav.randomDirection();
		if (rc.canBuildRobot(toBuild, dir, influence)) {
			rc.buildRobot(toBuild, dir, influence);
		}
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}

}