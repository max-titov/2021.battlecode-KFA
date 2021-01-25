package sprintplayer;

import battlecode.common.*;

public class Politician extends Robot {

	/**
	 * Constants
	 */
	public final int HERDER_POLITICIAN = 1;
	public final int CAPTURER_POLITICIAN = 2;
	public final int WANDERER_POLITICIAN = 3;

	public static final int SLANDERER_FLAG = 934245;

	public static final int HERDER_POLITCIAN_INFLUENCE = 18;

	/**
	 * Politician's attributes
	 */
	public int politicianType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;
	public RobotInfo[] robotsInEmpowerMax;
	public MapLocation mainTargetLoc;

	public boolean clockwise = true; // what direction the politician will be circling the slanderer group

	public Politician(RobotController rc) throws GameActionException {
		super(rc);
		if (coinFlip()) {
			clockwise = false;
		}
		readECMessage();
		if (conviction%5==1) {
			politicianType = CAPTURER_POLITICIAN;
		} else if (conviction == HERDER_POLITCIAN_INFLUENCE){ 
			politicianType = HERDER_POLITICIAN;
		} else {
			politicianType = WANDERER_POLITICIAN;
		}
	}

	public void readECMessage() throws GameActionException {
		int[] message = comms.readMessage(myECid);
		int messageType = message[0];
		if(messageType==Comms.FOUND_EC){
			int targetXOffset = message[3];
			int targetYOffset = message[4];
			int targetX=myECLoc.x+targetXOffset;
			int targetY=myECLoc.y+targetYOffset;
			mainTargetLoc=new MapLocation(targetX, targetY);
		}
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		rc.setFlag(0); //TODO: this is a temp fix probably add something to robot
		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		neutralBots = rc.senseNearbyRobots(sensorRadSq, Team.NEUTRAL);
		robotsInEmpowerMax = rc.senseNearbyRobots(9);
		// nav.tryMove(randomDirection());
		switch (politicianType) {
			case HERDER_POLITICIAN:
				herderPolitician();
				break;
			case CAPTURER_POLITICIAN:
				capturerPolitician();
				break;
			case WANDERER_POLITICIAN:
				wandererPolitician();
				break;
		}
	}

	public void herderPolitician() throws GameActionException {
		//shouldEmpower();
		targetEnemyMucks();

		MapLocation avgLocNearbySlanderers = avgLocNearbySlanderers();
		if (avgLocNearbySlanderers == null) {
			// TODO: MAKE IT NOT MOVE RANDOMLY HERE
			nav.simpleExploration();
			return;
		}

		int sqDistToSlandererAvg = currLoc.distanceSquaredTo(avgLocNearbySlanderers);
		Direction dirToSlandererAvg = currLoc.directionTo(avgLocNearbySlanderers);

		Direction moveDir = dirToSlandererAvg;
		if (sqDistToSlandererAvg < 8) {
			moveDir = rotate(rotate(rotate(dirToSlandererAvg)));
		} else if (sqDistToSlandererAvg > 18) {
			moveDir = (rotate(dirToSlandererAvg));
		} else {
			moveDir = rotate(rotate(dirToSlandererAvg));
		}

		if (!rc.onTheMap(currLoc.add(moveDir).add(moveDir))) {
			clockwise = !clockwise;
			return;
		}
		nav.bugNav(moveDir);
	}

	public void targetEnemyMucks() throws GameActionException {
		int enemyBotsLen = enemyBots.length;
		RobotInfo target = null;
		for (int i = 0; i < enemyBotsLen; i++) {
			if (enemyBots[i].type.equals(RobotType.MUCKRAKER)) {
				target = enemyBots[i];
				break;
			}
		}
		if(target!=null){
			targetUnit(target);
		}
	}

	public void capturerPolitician() throws GameActionException {
		if(mainTargetLoc==null){
			politicianType=WANDERER_POLITICIAN;
			nav.simpleExploration();
			return;
		}

		if(rc.canSenseLocation(mainTargetLoc)){
			RobotInfo mainTargetInfo = rc.senseRobotAtLocation(mainTargetLoc);
			//if the target is no longer an enemy or neutral ec
			if(mainTargetInfo.team.equals(myTeam) && mainTargetInfo.type.equals(RobotType.ENLIGHTENMENT_CENTER)){
				politicianType=WANDERER_POLITICIAN;
				nav.simpleExploration();
				return;
			}
		}

		boolean foundEC = lookForECsToCapture();
		
		if (!foundEC) {
			nav.tryMoveToTarget(mainTargetLoc);
		}
	}

	public void wandererPolitician() throws GameActionException {
		boolean foundEC = lookForECsToCapture();
		if (!foundEC) {
			nav.simpleExploration();
		}
	}

	public boolean lookForECsToCapture() throws GameActionException {
		int enemyBotsLen = enemyBots.length;
		RobotInfo target = null;
		for (int i = 0; i < enemyBotsLen; i++) {
			if (enemyBots[i].type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				target = enemyBots[i];
				break;
			}
		}
		int neutralBotsLen = neutralBots.length;
		for (int i = 0; i < neutralBotsLen; i++) {
			if (neutralBots[i].type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				target = neutralBots[i];
				break;
			}
		}
		if (target != null) {
			targetUnit(target);
			return true;
		} else {
			return false;
		}
	}

	public boolean shouldEmpower() throws GameActionException {
		int empowerPower = (int) (conviction * rc.getEmpowerFactor(myTeam, 0));
		if (empowerPower <= 10) {
			return false;
		}
		boolean shouldSpeech = false;
		int robotsInSpeechCount = robotsInEmpowerMax.length;
		boolean enemyMuckrackerInRad = false;
		for (int i = 0; i < robotsInSpeechCount; i++) {
			RobotInfo ri = robotsInEmpowerMax[i];
			Team tempTeam = ri.getTeam();
			RobotType tempType = ri.getType();
			if (tempTeam.equals(opponentTeam) && tempType.equals(RobotType.MUCKRAKER)) {
				enemyMuckrackerInRad = true;
			}
		}

		// TODO: add more conditions for when a politician should explode

		if (enemyMuckrackerInRad) {
			shouldSpeech = true;
		}

		if (shouldSpeech && rc.canEmpower(9)) {
			rc.empower(9);
			return true;
		}
		return false;

	}

	public void targetUnit(RobotInfo targetRI) throws GameActionException {
		if(conviction<=10){
			return;
		}
		int empowerPower = (int) ((conviction-10) * rc.getEmpowerFactor(myTeam, 0));
		MapLocation targetLoc = targetRI.location;
		int distToTarget = currLoc.distanceSquaredTo(targetLoc);
		int targetConviction = targetRI.conviction;

		if (distToTarget > 9) {
			// TODO: make separate nav method that accounts for other units in the area
			nav.bugNav(targetLoc);
			return;
		}

		RobotInfo[] robotsInEmpowerTargetRad = rc.senseNearbyRobots(distToTarget);
		int robotsInEmpowerTargetRadLen = robotsInEmpowerTargetRad.length;

		int avgConvictionToDistribute = robotsInEmpowerTargetRadLen / empowerPower;

		if (robotsInEmpowerTargetRadLen==1 || avgConvictionToDistribute > targetConviction || distToTarget <= 2) {
			if (rc.canEmpower(distToTarget)) {
				rc.empower(distToTarget);
				return;
			}
		}

		nav.bugNav(targetLoc);

	}

	public Direction rotate(Direction dir) throws GameActionException {
		if (clockwise) {
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

			if (rc.getFlag(ri.getID()) == SLANDERER_FLAG) {
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