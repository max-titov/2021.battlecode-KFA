package navtest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	boolean created = false;
	int influence = 24;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER };


	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = randomSpawnableRobotType();
		//nav.randomDirection();
		for(int i = 0; i < 8; i++){
			Direction dir = nav.directions[i];
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				if(toBuild.equals(RobotType.POLITICIAN)){
					rc.buildRobot(toBuild, dir, 61);
				}
				else{
					rc.buildRobot(toBuild, dir, 41);
				}
			}
		}
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}

}