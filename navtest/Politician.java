package navtest;

import battlecode.common.*;

public class Politician extends Robot {

	/**
	 * Constants
	 */
	public final int HERDER_POLITICIAN = 1;
	public final int TYPE2_POLITICIAN = 2;

	public static final int SLANDERER_FLAG = 934245;

	/**
	 * Politician's attributes
	 */
	public int politicianType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;


	public boolean clockwise = true; //what direction the politician will be circling the slanderer group

	public Politician(RobotController rc) throws GameActionException {
		super(rc);
		if(coinFlip()){
			clockwise=false;
		}
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		neutralBots = rc.senseNearbyRobots(sensorRadSq, Team.NEUTRAL);
		politicianType=HERDER_POLITICIAN;
		//nav.tryMove(randomDirection());
		switch (politicianType) {
			case HERDER_POLITICIAN:
				herderPolitician();
				break;
		}
	}

	public void herderPolitician() throws GameActionException {
		MapLocation avgLocNearbySlanderers = avgLocNearbySlanderers();
		if(avgLocNearbySlanderers==null){
			//TODO: MAKE IT NOT MOVE RANDOMLY HERE
			nav.simpleExploration();
			return;
		}

		int sqDistToSlandererAvg = currLoc.distanceSquaredTo(avgLocNearbySlanderers);
		Direction dirToSlandererAvg = currLoc.directionTo(avgLocNearbySlanderers);

		Direction moveDir = dirToSlandererAvg;
		if(sqDistToSlandererAvg<8){
			moveDir = rotate(rotate(rotate(dirToSlandererAvg)));
		}
		else if (sqDistToSlandererAvg > 18){
			moveDir = (rotate(dirToSlandererAvg));
		}
		else{
			moveDir = rotate(rotate(dirToSlandererAvg));
		}

		if(!rc.onTheMap(currLoc.add(moveDir))){
			clockwise = !clockwise;
			return;
		}
		nav.bugNav(moveDir);
	}

	public Direction rotate(Direction dir) throws GameActionException {
		if(clockwise){
			return dir.rotateRight();
		}
		return dir.rotateLeft();
	}

	public MapLocation avgLocNearbySlanderers() throws GameActionException {
		int alliedBotsLen = alliedBots.length;
		int totalX = 0;
		int totalY = 0;
		int slandererCount = 0;

		for (int i = 0; i < alliedBotsLen; i++) {
			RobotInfo ri = alliedBots[i];

			if (rc.getFlag(ri.getID())==SLANDERER_FLAG) {
				MapLocation tempLoc = ri.getLocation();
				totalX += tempLoc.x;
				totalY += tempLoc.y;
				slandererCount++;

			}
		}
		if (slandererCount == 0) {
			return null;
		}
		int avgX = totalX / slandererCount;
		int avgY = totalY / slandererCount;
		MapLocation avgLoc = new MapLocation(avgX, avgY);
		return avgLoc;
	}

}