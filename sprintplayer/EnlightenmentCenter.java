package sprintplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {
	/**
	 * Constants
	 */
	public final int NUM_OF_UNITS_TO_TRACK = 100;
	// messages
	// public final int EDGE_MESSAGE = ;

	/**
	 * Attributes
	 */
	public int spawnIndex;
	public int dirIndex;
	public boolean firstSlandererCreated;
	public int influenceToMake;
	public int[] robotIDs;
	public int numOfRobotsCreated;
	public int influenceToVote;
	public int myPrevVotes;

	/**
	 * Constructor
	 * 
	 * @param rc
	 * @throws GameActionException
	 */
	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
		influenceToVote = 1;
		robotIDs = new int[NUM_OF_UNITS_TO_TRACK];
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		if (roundNum >= 50 && influence <= 50) {
			return;
		}
		if (roundNum != 0 && myPrevVotes == myVotes) {
			influenceToVote++;
		}
		rc.bid(influenceToVote);
		RobotType toBuild = spawnableRobot[spawnIndex];
		switch (toBuild) {
			case POLITICIAN:
				if (coinFlip(0.2) && roundNum > 30) {
					influenceToMake = 50;
				} else {
					influenceToMake = 15;
				}
				break;
			case SLANDERER:
				if (!firstSlandererCreated) {
					influenceToMake = 107;
					firstSlandererCreated = true;
				} else {
					influenceToMake = 41;
				}
				break;
			case MUCKRAKER:
				influenceToMake = 1;
				break;
			default:
				break;
		}
		Direction directionToBuild = directions[dirIndex];
		while (!rc.onTheMap(currLoc.add(directionToBuild))) {
			dirIndex++;
			if (dirIndex == 8) {
				dirIndex = 0;
			}
			directionToBuild = directions[dirIndex];
		}
		if (rc.canBuildRobot(toBuild, directionToBuild, influenceToMake)) {
			rc.buildRobot(toBuild, directionToBuild, influenceToMake);
			dirIndex++;
			spawnIndex++;
		}
		myPrevVotes = myVotes;
	}

	// public void checkFlags() {
	// for (int i = 0; i < numOfRobotsCreated; i++) {
	// int flagMessage = rc.getFlag(robotIDs[i]);
	// switch (flagMessage) {
	// case
	// }
	// }
	// }

	public int getIndexOfID(int id) {
		for (int i = 0; i < numOfRobotsCreated; i++) {
			if (robotIDs[i] == id) {
				return i;
			}
		}
		return -1;
	}
}