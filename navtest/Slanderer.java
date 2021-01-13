package navtest;

import battlecode.common.*;

public class Slanderer extends Robot {

	/**
	 * Constants
	 */
	public final int BUNCH_SLANDERER = 1;
	public final int TYPE2_SLANDERER = 2;

	/**
	 * Slanderer's attributes
	 */
	public int slandererType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;

	public Slanderer(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		neutralBots = rc.senseNearbyRobots(sensorRadSq, Team.NEUTRAL);
		nav.tryMove(randomDirection());
	}

	public MapLocation avgLocNearbySlanderers() throws GameActionException {
		int alliedBotsLen = alliedBots.length;
		int totalX = 0;
		int totalY = 0;
		int slandererCount = 0;

		for (int i = 0; i < alliedBotsLen; i++){
			RobotInfo ri = alliedBots[i];
			if(ri.getType().equals(RobotType.SLANDERER)){
				MapLocation tempLoc = ri.getLocation();
				totalX += tempLoc.x;
				totalY += tempLoc.y;
				slandererCount++;
				
			}
		}
		if(slandererCount==0){
			return null;
		}
		int avgX = totalX/slandererCount;
		int avgY = totalY/slandererCount;
		MapLocation avgLoc = new MapLocation(avgX,avgY);
		return avgLoc;
	}

	public MapLocation newBunchAwayFromEC(){
		Direction dir = nav.relativeLocToEC(currLoc);
		for(int i = 0; i<8;i++){
			boolean foundSpot = false;
			MapLocation testLoc = currLoc.add(Direction.CENTER);//copies currLoc idk if there is a better way to do it
			while(myECLoc.isWithinDistanceSquared(testLoc, distanceSquared))
		}
	}




}