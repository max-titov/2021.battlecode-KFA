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
		rawMessage = rawMessage >> 4;
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
		packedMessage += edgeType;// packedMessage = 000000000000000000000(edge type)
		packedMessage = packedMessage << 7;// packedMessage = 00000000000000(edge type)0000000
		packedMessage += xOff + 64;// packedMessage = 00000000000000(edge type)(xOff+64)
		packedMessage = packedMessage << 7;// packedMessage = 0000000(edge type)(xOff+64)0000000
		packedMessage += yOff + 64;// packedMessage = 0000000(edge type)(xOff+64)(yOff+64)
		packedMessage = packedMessage << 4;// packedMessage = 000(edge type)(xOff+64)(yOff+64)0000
		packedMessage += FOUND_EDGE;// packedMessage = 000(edge type)(xOff+64)(yOff+64)(messageType)
		sendMessage(packedMessage);
	}

	// Reads edgeMessages: Edge type, x, y
	public int[] readFoundEdgeMessage(int rawMessage) throws GameActionException {
		// [edgeType, xOff, yOff]
		int[] info = new int[3];
		info[2] = rawMessage & 0b1111111;
		rawMessage = rawMessage >> 7;
		info[1] = rawMessage & 0b1111111;
		rawMessage = rawMessage >> 7;
		info[0] = rawMessage & 0b111;
		return info;
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
			intTeam = 3;
		}

		// Value for conviction
		int convictionRange = 0;
		if (targetConviction <= 5) {
			convictionRange = 0;
		} else if (targetConviction <= 40) {
			convictionRange = 1;
		} else if (targetConviction <= 100) {
			convictionRange = 2;
		} else {
			convictionRange = 3;
		}

		int packedMessage = 0;
		packedMessage = (packedMessage << 2) + intTeam;
		packedMessage = (packedMessage << 2) + convictionRange;
		packedMessage = (packedMessage << 7) + xOff + 64;
		packedMessage = (packedMessage << 7) + yOff + 64;
		packedMessage = (packedMessage << 4) + FOUND_EC;
		sendMessage(packedMessage);
	}

	public int[] readFoundECMessage(int rawMessage) {
		int[] retList = new int[5];
		retList[0] = rawMessage & 0b1111;
		retList[4] = (rawMessage >> 4) & 0b1111111;
		retList[3] = (rawMessage >> 7) & 0b1111111;
		retList[2] = (rawMessage >> 7) & 0b11;
		retList[1] = (rawMessage >> 2) & 0b11;
		return retList;
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