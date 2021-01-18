package navtest;

import battlecode.common.*;

public class Politician extends Robot {

	/**
	 * Constants
	 */
	public final int HERDER_POLITICIAN = 1;
	public final int CAPTURER_POLITICIAN = 2;

	public static final int SLANDERER_FLAG = 934245;

	/**
	 * Politician's attributes
	 */
	public int politicianType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;
	public RobotInfo[] robotsInEmpowerMax;


	public boolean clockwise = true; //what direction the politician will be circling the slanderer group

	public Politician(RobotController rc) throws GameActionException {
		super(rc);
		if(coinFlip()){
			clockwise=false;
		}
		if(conviction==50){
			politicianType=CAPTURER_POLITICIAN;
		} else {
			politicianType=HERDER_POLITICIAN;
		}
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		enemyBots = rc.senseNearbyRobots(sensorRadSq, opponentTeam);
		alliedBots = rc.senseNearbyRobots(sensorRadSq, myTeam);
		neutralBots = rc.senseNearbyRobots(sensorRadSq, Team.NEUTRAL);
		robotsInEmpowerMax = rc.senseNearbyRobots(9);
		//nav.tryMove(randomDirection());
		switch (politicianType) {
			case HERDER_POLITICIAN:
				herderPolitician();
				break;
			case CAPTURER_POLITICIAN:
				capturerPolitician();
				break;
		}
	}

	public void herderPolitician() throws GameActionException {
		shouldEmpower();

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

		if(!rc.onTheMap(currLoc.add(moveDir).add(moveDir))){
			clockwise = !clockwise;
			return;
		}
		nav.bugNav(moveDir);
	}

	public void capturerPolitician() throws GameActionException {
		int enemyBotsLen = enemyBots.length;
		RobotInfo target = null;
		for(int i = 0; i < enemyBotsLen; i++){
			if(enemyBots[i].equals(RobotType.ENLIGHTENMENT_CENTER)){
				target=enemyBots[i];
				break;
			}
		}
		int neutralBotsLen = neutralBots.length;
		for(int i = 0; i < neutralBotsLen; i++){
			if(neutralBots[i].equals(RobotType.ENLIGHTENMENT_CENTER)){
				target=neutralBots[i];
				break;
			}
		}
		if(target!=null){
			targetUnit(target);
		}else{
			nav.simpleExploration();
		}
	}

	public boolean shouldEmpower() throws GameActionException {
		int empowerPower = (int)(conviction*rc.getEmpowerFactor(myTeam, 0));
		if(empowerPower<=10){
			//TODO: if a muck killed a slanderer(s), then this might be the wrong move
			return false;
		}
		boolean shouldSpeech = false;
		int robotsInSpeechCount = robotsInEmpowerMax.length;
		boolean enemyMuckrackerInRad = false;
		for(int i = 0; i<robotsInSpeechCount;i++){
			RobotInfo ri = robotsInEmpowerMax[i];
			Team tempTeam = ri.getTeam();
			RobotType tempType = ri.getType();
			if(tempTeam.equals(opponentTeam) && tempType.equals(RobotType.MUCKRAKER)){
				enemyMuckrackerInRad = true;
			}
		}

		//TODO: add more conditions for when a politician should explode

		if(enemyMuckrackerInRad){
			shouldSpeech=true;
		}

		if(shouldSpeech && rc.canEmpower(9)){
			rc.empower(9);
			return true;
		}
		return false;

	}

	public void targetUnit(RobotInfo targetRI) throws GameActionException {
		int empowerPower = (int)(conviction*rc.getEmpowerFactor(myTeam, 0));
		MapLocation targetLoc = targetRI.location;
		int distToTarget = currLoc.distanceSquaredTo(targetLoc);
		int targetConviction = targetRI.conviction;

		if(distToTarget>9){
			//TODO: make separate nav method that accounts for other units in the area
			nav.bugNav(targetLoc);
			return;
		}

		RobotInfo[] robotsInEmpowerTargetRad = rc.senseNearbyRobots(distToTarget);
		int robotsInEmpowerTargetRadLen = robotsInEmpowerTargetRad.length;

		int avgConvictionToDistribute = robotsInEmpowerTargetRadLen/empowerPower;

		if(avgConvictionToDistribute>targetConviction || (robotsInEmpowerTargetRadLen<3&&distToTarget<=2)){
			if(rc.canEmpower(distToTarget)){
				rc.empower(distToTarget);
				return;
			}
		}

		nav.bugNav(targetLoc);

		
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