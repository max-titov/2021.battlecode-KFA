package navtest2;

import battlecode.common.*;

public class Muckraker extends Robot {

	public Muckraker(RobotController r) {
		super(r);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		testNavCircle();
		Team enemy = rc.getTeam().opponent();
		int actionRadius = rc.getType().actionRadiusSquared;
		for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
			if (robot.type.canBeExposed()) {
				// It's a slanderer... go get them!
				if (rc.canExpose(robot.location)) {
					rc.expose(robot.location);
					return;
				}
			}
		}
		tryMove(randomDirection());
	}

	public void testNavCircle() throws GameActionException {
		int navCircleLen = nav.navCircle.length;
		MapLocation tempLoc = rc.getLocation();
		for (int i = 0; i < navCircleLen; i++){
			tempLoc = tempLoc.add(nav.navCircle[i]);
			int c = i*2;
			rc.setIndicatorDot(tempLoc,c,c,c);
		}
	}

}