package navtest;

import battlecode.common.*;

public class Robot {
	/**
	 * Constants
	 */
	public final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };
	public final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	/**
	 * 'Robot' Object Attributes
	 */
	public RobotController rc;
	public Navigation nav;
	public Comms comms;
	public Team myTeam;
	public Team opponentTeam;
	public RobotType myType;
	public int robotAge;
	public int conviction;
	public double cooldownTurns;
	public int id;
	public int influence;
	public MapLocation currLoc;
	public int roundNum;
	public int message;
	public int sensorRadSq;
	public MapLocation myECLoc;
	public int myECid;

	/**
	 * Constructor
	 * 
	 * @param rc
	 * @throws GameActionException
	 */
	public Robot(RobotController rc) throws GameActionException {
		this.rc = rc;
		getECDetails();
		myTeam = rc.getTeam();
		opponentTeam = myTeam.opponent();
		myType = rc.getType();
		id = rc.getID();
		robotAge = 0;
		conviction = rc.getConviction();
		cooldownTurns = rc.getCooldownTurns();
		influence = rc.getInfluence();
		currLoc = rc.getLocation();
		roundNum = rc.getRoundNum();
		sensorRadSq = getSensorRadiusSq();
		this.nav = new Navigation(rc, currLoc, myECLoc);
		this.comms = new Comms(rc);
	}

	/**
	 * 'Robot' Object Methods
	 */
	public void takeTurn() throws GameActionException {
		robotAge += 1;
		conviction = rc.getConviction();
		cooldownTurns = rc.getCooldownTurns();
		influence = rc.getInfluence();
		currLoc = rc.getLocation();
		roundNum = rc.getRoundNum();
		// message = comms.checkmessage()
		nav.updateCurrLoc(currLoc);
	}

	public void getECDetails() throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots(2, myTeam);
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				myECLoc = ri.getLocation();
				myECid = ri.getID();
			}
		}
	}

	public int getSensorRadiusSq() {
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER:
				return 40;
			case POLITICIAN:
				return 25;
			case SLANDERER:
				return 20;
			case MUCKRAKER:
				return 30;
			default:
				return -1;
		}
	}

	public boolean coinFlip() {
		return Math.random() > 0.5;
	}

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}
}