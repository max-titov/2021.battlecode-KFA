package commsplayer;

import battlecode.common.*;

public class Comms {
	/**
	 * Constants
	 */
	public static final int FOUND_EDGE = 1;
	public static final int FOUND_EC = 2;

	/**
	 * Attributes
	 */
	public RobotController rc;
	public Team myTeam;
	public Team opponentTeam;
	public MapLocation currLoc;
	public MapLocation myECLoc;

	/**
	 * Constructor
	 * 
	 * @param rc
	 * @param myTeam
	 * @param opponentTeam
	 * @param currLoc
	 * @param myECLoc
	 */
	public Comms(RobotController rc, Team myTeam, Team opponentTeam, MapLocation currLoc, MapLocation myECLoc) {
		this.rc = rc;
		this.myTeam = myTeam;
		this.opponentTeam = opponentTeam;
		this.currLoc = currLoc;
		this.myECLoc = myECLoc;
	}

	/**
	 * Raise flag if it can
	 * 
	 * @param packedMessage
	 * @throws GameActionException
	 */
	public void sendMessage(int packedMessage) throws GameActionException {
		if (rc.canSetFlag(packedMessage)) {
			rc.setFlag(packedMessage);
		} else {
			System.out.println("Flag failed to send");
		}
	}

	/**
	 * Read a robot's flag if it can
	 * 
	 * @param robotID
	 * @return
	 * @throws GameActionException
	 */
	public int readRawMessage(int robotID) throws GameActionException {
		if (rc.canGetFlag(robotID)) {
			return rc.getFlag(robotID);
		} else {
			System.out.println("Failed to get flag");
			return -1;
		}
	}

	/**
	 * General read message, returns array of integers with information about the
	 * message
	 * 
	 * @param robotID
	 * @return
	 * @throws GameActionException
	 */
	public int[] readMessage(int robotID) throws GameActionException {
		int rawMessage = readRawMessage(robotID);
		if (rawMessage == -1 || rawMessage == 0) {
			return null;
		}
		int messageType = rawMessage & 0b1111;
		switch (messageType) {
			case FOUND_EDGE:
				System.out.println("Found edge message");
				return readFoundEdgeMessage(rawMessage);
			case FOUND_EC:
				return readFoundECMessage(rawMessage);
		}
		return null;
	}

	/**
	 * Raise flag if edge is found with information about the edge
	 * 
	 * @param edgeType
	 * @param edgeLoc
	 * @throws GameActionException
	 */
	public void sendFoundEdgeMessage(int edgeType, int edgeLocX, int edgeLocY) throws GameActionException {
		int xOff = edgeLocX - myECLoc.x;
		int yOff = edgeLocY - myECLoc.y;
		int packedMessage = 0;
		packedMessage += edgeType;
		packedMessage = packedMessage << 7;
		packedMessage += xOff + 64;
		packedMessage = packedMessage << 7;
		packedMessage += yOff + 64;
		packedMessage = packedMessage << 4;
		packedMessage += FOUND_EDGE;
		sendMessage(packedMessage);
	}

	/**
	 * Unpack edge found message
	 * 
	 * @param rawMessage
	 * @return
	 * @throws GameActionException
	 */
	public int[] readFoundEdgeMessage(int rawMessage) throws GameActionException {
		// [messageType, edgeType, xOff, yOff]
		int[] info = new int[4];
		info[0] = rawMessage & 0b1111;
		rawMessage = rawMessage >> 4;
		info[3] = (rawMessage & 0b1111111) - 64;
		rawMessage = rawMessage >> 7;
		info[2] = (rawMessage & 0b1111111) - 64;
		rawMessage = rawMessage >> 7;
		info[1] = rawMessage;
		return info;
	}

	/**
	 * Raise flag if EC is found giving information about it
	 * 
	 * @param ri
	 * @throws GameActionException
	 */
	public void sendFoundECMessage(RobotInfo ri) throws GameActionException {
		int xOff = ri.location.x - myECLoc.x;
		int yOff = ri.location.y - myECLoc.y;

		int intTeam;
		// Set value for the EC's team
		if (ri.team.equals(myTeam)) {
			intTeam = 0;
		} else if (ri.team.equals(opponentTeam)) {
			intTeam = 1;
		} else {
			intTeam = 2;
		}

		// Value for conviction
		int convIntRange = convToConvIntRange(ri.conviction);

		int packedMessage = 0;
		packedMessage += intTeam;
		packedMessage = packedMessage << 4;
		packedMessage += convIntRange;
		packedMessage = packedMessage << 7;
		packedMessage += xOff + 64;
		packedMessage = packedMessage << 7;
		packedMessage += yOff + 64;
		packedMessage = packedMessage << 4;
		packedMessage += FOUND_EC;
		sendMessage(packedMessage);
	}

	/**
	 * Unpack EC found message
	 * 
	 * @param rawMessage
	 * @return
	 */
	public int[] readFoundECMessage(int rawMessage) {
		// [MessageType, Team, ConvictionRange, xOff, yOff]
		int[] info = new int[5];
		info[0] = rawMessage & 0b1111;
		rawMessage = rawMessage >> 4;
		info[4] = (rawMessage & 0b1111111) - 64;
		rawMessage = rawMessage >> 7;
		info[3] = (rawMessage & 0b1111111) - 64;
		rawMessage = rawMessage >> 7;
		info[2] = rawMessage & 0b1111;
		rawMessage = rawMessage >> 4;
		info[1] = rawMessage;
		return info;
	}

	public void dropFlag() throws GameActionException {
		rc.setFlag(0);
	}

	public int convIntRangeToMaxConv(int convInt) {
		switch (convInt) {
			case 0:
				return 50;
			case 1:
				return 75;
			case 2:
				return 100;
			case 3:
				return 125;
			case 4:
				return 150;
			case 5:
				return 175;
			case 6:
				return 200;
			case 7:
				return 225;
			case 8:
				return 250;
			case 9:
				return 275;
			case 10:
				return 300;
			case 11:
				return 350;
			case 12:
				return 400;
			case 13:
				return 450;
			case 14:
				return 500;
			case 15:
				return 505;
		}
		return -1;
	}

	public int convToConvIntRange(int conv) {
		if (conv < 50) {
			return 0;
		} else if (conv < 75) {
			return 1;
		} else if (conv <= 100) {
			return 2;
		} else if (conv <= 125) {
			return 3;
		} else if (conv <= 150) {
			return 4;
		} else if (conv <= 175) {
			return 5;
		} else if (conv <= 200) {
			return 6;
		} else if (conv <= 225) {
			return 7;
		} else if (conv <= 250) {
			return 8;
		} else if (conv <= 275) {
			return 9;
		} else if (conv <= 300) {
			return 10;
		} else if (conv <= 350) {
			return 11;
		} else if (conv <= 400) {
			return 12;
		} else if (conv <= 450) {
			return 13;
		} else if (conv <= 500) {
			return 14;
		} else {
			return 15;
		}
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