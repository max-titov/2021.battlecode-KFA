package sprintplayer;

import battlecode.common.*;

public class BuildUnit {
	/**
	 * Attributes
	 */
	public RobotType type;
	public int conviction;
	// Only for capture politicians and attack politicians
	public MapLocation targetECLoc;
	public Team targetECTeam;

	public BuildUnit(RobotType type, int conviction, MapLocation targetECLoc, Team targetECTeam) {
		this.type = type;
		this.conviction = conviction;
		this.targetECLoc = targetECLoc;
		this.targetECTeam = targetECTeam;
	}

	public BuildUnit(RobotType type, int conviction) {
		this.type = type;
		this.conviction = conviction;
	}

	public boolean hasTargetEC() {
		return targetECLoc != null;
	}

	public String toString() {
		return type.toString() + " " + conviction + " has target EC: " + hasTargetEC();
	}

	public boolean equals(BuildUnit bu) {
		boolean targetCheck = true;
		if (bu.hasTargetEC() && hasTargetEC()) {
			if (targetECLoc.equals(bu.targetECLoc) && targetECTeam.equals(bu.targetECTeam)) {
				targetCheck = true;
			} else {
				targetCheck = false;
			}
		} else {
			targetCheck = false;
		}
		return type.equals(bu.type) && conviction == bu.conviction && targetCheck;
	}

}
