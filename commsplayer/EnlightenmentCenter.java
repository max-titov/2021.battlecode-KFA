package commsplayer;

import battlecode.common.*;
import java.util.Arrays;

public class EnlightenmentCenter extends Robot {
	int spawnedBotID = 0;
	int turn = 0;
	Comms comm;

	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
		comm = new Comms(rc, this.myTeam, this.opponentTeam, this.currLoc, this.myECLoc);
	}

	public void takeTurn() throws GameActionException {
		RobotType toBuild = RobotType.POLITICIAN;
		int influence = 50;
		if(turn == 2){
			if(rc.canBuildRobot(RobotType.POLITICIAN, Direction.NORTH, 10)){
				rc.buildRobot(RobotType.POLITICIAN, Direction.NORTH, 10);
			}
		}
		System.out.println(rc.senseNearbyRobots(-1, rc.getTeam()));
		if (rc.senseNearbyRobots(-1, rc.getTeam()).length >= 1) {
			RobotInfo spawnedBot = rc.senseNearbyRobots(-1, rc.getTeam())[0];
			this.spawnedBotID = spawnedBot.getID();
		}
		if(rc.canGetFlag(spawnedBotID)){
			int[] commsTest = comm.readFoundECMessage(rc.getFlag(spawnedBotID));
			System.out.println(Arrays.toString(commsTest));
		}
		turn += 1;
	}

}