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
	 * Slanderer's attributes
	 */
	public int slandererType;
	public RobotInfo[] enemyBots;
	public RobotInfo[] alliedBots;
	public RobotInfo[] neutralBots;

	public boolean firstInBunch = false;
	public MapLocation bunchLoc;

	public Politician(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		nav.tryMove(randomDirection());
	}

}