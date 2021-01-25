package commsplayer;

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
	public int myVotes;
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
	public int xOffset;
	public int yOffset;

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
		myVotes = rc.getTeamVotes();
		id = rc.getID();
		robotAge = 0;
		conviction = rc.getConviction();
		cooldownTurns = rc.getCooldownTurns();
		influence = rc.getInfluence();
		currLoc = rc.getLocation();
		roundNum = rc.getRoundNum();
		sensorRadSq = getSensorRadiusSq();
		this.comms = new Comms(rc, myTeam, opponentTeam, currLoc, myECLoc);
		this.nav = new Navigation(rc, currLoc, myECLoc);
	}

	/**
	 * 'Robot' Object Methods
	 */
	public void takeTurn() throws GameActionException {
		myVotes = rc.getTeamVotes();
		robotAge += 1;
		conviction = rc.getConviction();
		cooldownTurns = rc.getCooldownTurns();
		influence = rc.getInfluence();
		currLoc = rc.getLocation();
		roundNum = rc.getRoundNum();
		// this.xOffset = myECLoc.x - currLoc.x;
		// this.yOffset = myECLoc.y - currLoc.y;
		// message = comms.checkmessage()
		comms.updateCurrLoc(currLoc);
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

	public boolean coinFlip(double percentage) {
		return Math.random() > (1 - percentage);
	}

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}
}