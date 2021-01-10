package playertemplate;

import battlecode.common.*;

public class Robot {
	/**
	 * Constants
	 */
	public final int ROUND_TO_START_DEFENSE = 500;
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

	/**
	 * Constructor
	 * 
	 * @param rc
	 */
	public Robot(RobotController rc) {
		this.rc = rc;
		this.nav = new Navigation(rc);
		this.comms = new Comms(rc);
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
	}

	public Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	public RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}
}