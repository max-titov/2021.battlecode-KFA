package commsplayer;

import battlecode.common.*;

public class Comms {
	/**
	 * Constants
	 */
	public final int FOUND_EDGE = 0;
	public final int FOUND_EC = 1;

	/**
	 * Attributes
	 */
	public RobotController rc;
	public Team myTeam;
	public Team opponentTeam;
	public MapLocation currLoc;
	public MapLocation myECLoc;

	public Comms(RobotController rc, Team myTeam, Team opponentTeam, MapLocation currLoc, MapLocation myECLoc) {
		this.rc = rc;
		this.myTeam = myTeam;
		this.opponentTeam = opponentTeam;
		this.currLoc = currLoc;
		this.myECLoc = myECLoc;
	}

	public void sendMessage(int packedMessage) throws GameActionException {
		if (rc.canSetFlag(packedMessage)) {
			rc.setFlag(packedMessage);
		} else {
			System.out.println("Flag failed to send");
		}
	}

	public int readRawMessage(int robotID) throws GameActionException {
		if (rc.canGetFlag(robotID)) {
			return rc.getFlag(robotID);
		} else {
			System.out.println("Failed to get flag");
			return -1;
		}
	}

	public int[] readMessage(int robotID) throws GameActionException {
		int rawMessage = readRawMessage(robotID);
		int messageType = rawMessage & 0b1111;
		int[] info = null;
		switch (messageType) {
			case FOUND_EDGE:
				info = readFoundEdgeMessage(rawMessage);
			case FOUND_EC:
				info = readFoundECMessage(rawMessage);
		}
		return info;
		// get flag
		// check last 4 bits for type of message
		// switch statement for each type of message
		// returns int array of information
	}

	// Edge type, X and Y offsets of the edge
	public void sendFoundEdgeMessage(int edgeType, MapLocation edgeLoc) throws GameActionException {
		int xOff = edgeLoc.x - myECLoc.x;
		int yOff = edgeLoc.y - myECLoc.y;
		int packedMessage = 0;
		// edgeType = 3 bits
		packedMessage = (packedMessage << 3) + edgeType;
		// xOff, yOff = 7 each
		packedMessage = (packedMessage << 7) + xOff;
		packedMessage = (packedMessage << 7) + yOff;
		// messageType = 4
		packedMessage = (packedMessage << 4) + FOUND_EDGE;
		sendMessage(packedMessage);
	}

	// Reads edgeMessages: Edge type, x, y
	public int[] readFoundEdgeMessage(int rawMessage) throws GameActionException {
		// [edgeType, xOff, yOff]
		int[] retList = new int[3];
		retList[2] = rawMessage & 0b1111111;
		retList[1] = (rawMessage >> 7) & 0b1111111;
		retList[0] = (rawMessage >> 7) & 0b111;
		return retList;
	}

	// Found EC Message: Ec allegiance, EC Conviction, x and y offsets of the EC
	public void sendFoundECMessage(Team team, int targetConviction, MapLocation ecLoc) throws GameActionException {
		int xOff = ecLoc.x - myECLoc.x;
		int yOff = ecLoc.y - myECLoc.y;

		int intTeam;
		// Set value for the EC's team
		if (team.equals(myTeam)) {
			intTeam = 0;
		} else if (team.equals(opponentTeam)) {
			intTeam = 1;
		} else {
			intTeam = 2;
		}

		int convictionRange = 0;
		if (targetConviction <= 30) {
			convictionRange = 0;
		} else if (targetConviction <= 70) {
			convictionRange = 1;
		} else if (targetConviction <= 99) {
			convictionRange = 2;
		} else {
			convictionRange = 3;
		}

		int packedMessage = 0;
		packedMessage = (packedMessage << 2) + intTeam;
		packedMessage = (packedMessage << 2) + convictionRange;
		packedMessage = (packedMessage << 7) + xOff;
		packedMessage = (packedMessage << 7) + yOff;
		packedMessage = (packedMessage << 4) + FOUND_EC;
		sendMessage(packedMessage);
	}

	public int[] readFoundECMessage(int rawMessage) {

	}

	/**
	 * Updates the current location for nav
	 * 
	 * @param currLoc
	 */
	public void updateCurrLoc(MapLocation currLoc) {
		this.currLoc = currLoc;
	}
}