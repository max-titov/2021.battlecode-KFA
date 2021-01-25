package sprintplayer;

import battlecode.common.*;

public class Slanderer extends Robot {

	/**
	 * Constants
	 */
	public final int BUNCH_SLANDERER = 1;
	public final int TYPE2_SLANDERER = 2;

	public static final int SLANDERER_FLAG = 934245;

	/**
	 * Slanderer's attributes
	 */
	public int slandererType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;

	public boolean firstInBunch = false;
	public MapLocation bunchLoc;

	public Robot convertedToPolitician = new Politician(rc);

	public Slanderer(RobotController rc) throws GameActionException {
		super(rc);
		rc.setFlag(SLANDERER_FLAG);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();

		if(rc.getType().equals(RobotType.POLITICIAN)){
			convertedToPolitician.takeTurn();
			return;
		}

		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		neutralBots = rc.senseNearbyRobots(sensorRadSq, Team.NEUTRAL);
		slandererType=BUNCH_SLANDERER;
		//nav.tryMove(randomDirection());
		switch (slandererType) {
			case BUNCH_SLANDERER:
				bunchSlanderer();
				break;
		}
	}

	/**
	 * 3 priorities 1) run away from enemy mucks 2) find closest bunch of slanderers
	 * and join in on the fun 3) create new bunch a certain distance from the EC
	 * 
	 * @throws GameActionException
	 */
	public void bunchSlanderer() throws GameActionException {
		if(beAPussy()){
			return;
		}

		MapLocation targetLocation = null;
		MapLocation avgSlandererLoc = avgLocNearbySlanderers();
		MapLocation desiredBunchLoc = newBunchAwayFromEC();
		if(avgSlandererLoc==null){
			targetLocation=desiredBunchLoc;
			rc.setIndicatorLine(currLoc, targetLocation, 255, 0, 0);
			rc.setIndicatorDot(desiredBunchLoc, 0, 0, 255);
		}else{
			targetLocation=nav.avgLocations(avgSlandererLoc, desiredBunchLoc);
			rc.setIndicatorLine(currLoc, targetLocation, 255, 0, 0);
			rc.setIndicatorDot(avgSlandererLoc, 0, 255, 0);
			rc.setIndicatorDot(desiredBunchLoc, 0, 0, 255);
		}
		
		if (targetLocation != null){
			nav.bugNav(targetLocation);
		}
	}

	public MapLocation avgEnemyMuckLoc() throws GameActionException {
		int enemyBotsLen = enemyBots.length;
		int totalX = 0;
		int totalY = 0;
		int enemyMuckCount = 0;

		for (int i = 0; i < enemyBotsLen; i++) {
			RobotInfo ri = enemyBots[i];
			if (ri.getType().equals(RobotType.MUCKRAKER)) {
				MapLocation tempLoc = ri.getLocation();
				totalX += tempLoc.x;
				totalY += tempLoc.y;
				enemyMuckCount++;
			}
		}
		if (enemyMuckCount == 0) {
			return null;
		}
		int avgX = totalX / enemyMuckCount;
		int avgY = totalY / enemyMuckCount;
		MapLocation avgLoc = new MapLocation(avgX, avgY);
		return avgLoc;
	}

	public boolean beAPussy() throws GameActionException {
		MapLocation avgEnemyMuckLoc = avgEnemyMuckLoc();
		if(avgEnemyMuckLoc==null){
			return false;
		}
		Direction dirToRun = avgEnemyMuckLoc.directionTo(currLoc);
		nav.tryMoveToTarget(dirToRun);
		return true;
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

	/**
	 * finds a spot 12 or more units away from the EC for a slanderer bunch makes
	 * sure that the spot is on the map
	 * 
	 * @return new location for bunch
	 * @throws GameActionException
	 */
	public MapLocation newBunchAwayFromEC() throws GameActionException {
		Direction dir = nav.relativeLocToEC();
		MapLocation testLoc = myECLoc.add(Direction.CENTER);
		for (int i = 0; i < 8; i++) {
			boolean foundSpot = true;
			testLoc = myECLoc.add(dir);// copies currLoc idk if there is a better way to do it
			while (myECLoc.isWithinDistanceSquared(testLoc, 35)) {
				testLoc = testLoc.add(dir);
				if (rc.canSenseLocation(testLoc) && !rc.onTheMap(testLoc)) {
					foundSpot = false;
					break;
				}
			}
			if (foundSpot) {
				break;
			}
			dir.rotateLeft();
		}
		return testLoc;
	}

	// public MapLocation bunchLocAwayFromEC() throws GameActionException {
	// 	Direction dir = nav.relativeLocToEC(currLoc);
	// 	MapLocation testLoc = currLoc.add(Direction.CENTER);
	// }

}