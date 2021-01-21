package commsplayer;

import battlecode.common.*;

public class Comms {
	public RobotController rc;
	public Team myTeam;
	public Team opponentTeam;
	public MapLocation currLoc;
	public MapLocation myECLoc;
	public final int edgeMessage = 0;
	public final int foundECMessage = 1;

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
			System.out.println("Flag failed to send :(");
		}
	}

	// Changes x and y so negatives are represented
	public int[] setOffsets(int xOff, int yOff){
		if(xOff < 0){
			xOff += 128;
		}
		if(yOff < 0){
			yOff += 128;
		}
		int[] retArr = new int[] {xOff, yOff};
		return retArr;
	}

	// Interpret offsets
	public int[] interpretOffsets(int xOff, int yOff){
		if (xOff > 64) {
			xOff -= 128;
		}
		if (yOff > 64) {
			yOff -= 128;
		}
		int[] retArr = new int[] {xOff, yOff};
		return retArr;
	}

	// Edge type, X and Y offsets of the edge
	public void createEdgeMessage(int edgeType, int xOff, int yOff) throws GameActionException {
		int packedMessage = 0;
		// edgeType = 3 bits
		packedMessage = (packedMessage << 3) + edgeType;
		// xOff, yOff = 7 each
		packedMessage = (packedMessage << 7) + xOff;
		packedMessage = (packedMessage << 7) + yOff;
		// messageType = 4
		packedMessage = (packedMessage << 4) + edgeMessage;
		sendMessage(packedMessage);
	}

	// Reads edgeMessages: Edge type, x and y offsets of the edges
	public int[] readEdgeMessage(int packedMessage) throws GameActionException {
		// [messageType, edgeType, xOff, yOff]
		int[] retList = new int[4];
		retList[0] = packedMessage & 0b1111;
		retList[3] = (packedMessage >> 4) & 0b1111111;
		retList[2] = (packedMessage >> 7) & 0b1111111;
		retList[1] = (packedMessage >> 7) & 0b111;
		return retList;
	}

	// Found EC Message: Ec allegiance, EC Conviction, x and y offsets of the EC
	public void createFoundECMessage(Team team, int targetConviction, int xOff, int yOff) throws GameActionException {
		int intTeam = 0;
		// Set value for the EC's team
		if (team.equals(myTeam)) {
			intTeam = 1;
		} else if (team.equals(opponentTeam)){
			intTeam = 2;
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

		//Set the coord offsets
		int[] coordArr = setOffsets(xOff, yOff);
		int messageX = coordArr[0];
		int messageY = coordArr[1];

		int packedMessage = 0;
		packedMessage = (packedMessage << 2) + intTeam;
		packedMessage = (packedMessage << 2) + convictionRange;
		packedMessage = (packedMessage << 7) + messageX;
		packedMessage = (packedMessage << 7) + messageY;
		packedMessage = (packedMessage << 4) + foundECMessage;
		sendMessage(packedMessage);

	}

	// Interprets FoundEC Message
	public int[] readECMessage(int packedMessage) throws GameActionException {
		//[MessageType, ecAllegiance, ecConviction, x, y]
		int[] retList = new int[5];
		retList[0] = packedMessage & 0b1111;
		retList[4] = (packedMessage >> 4) & 0b1111111;
		retList[3] = (packedMessage >> 7) & 0b1111111;
		retList[2] = (packedMessage >> 7) & 0b11;
		retList[1] = (packedMessage >> 2) & 0b11;
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