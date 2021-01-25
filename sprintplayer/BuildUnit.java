package sprintplayer;

import battlecode.common.*;

public class BuildUnit {
	/**
	 * Attributes
	 */
	public RobotType type;
	public int influence;
	public Direction dirToBuild;
	// Only for capture politicians and attack politicians
	public MapLocation targetECLoc;

	public BuildUnit(RobotType type, int influence, Direction dirToBuild, MapLocation targetECLoc) {
		this.type = type;
		this.influence = influence;
		this.dirToBuild = dirToBuild;
		this.targetECLoc = targetECLoc;
	}

	public BuildUnit(RobotType type, int influence, Direction dirToBuild) {
		this.type = type;
		this.influence = influence;
		this.dirToBuild = dirToBuild;
	}

	public boolean hasTargetEC() {
		return targetECLoc != null;
	}

}
