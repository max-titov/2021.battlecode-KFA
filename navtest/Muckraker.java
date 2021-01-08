package navtest;

import battlecode.common.*;

public class Muckraker extends Robot {

	public Muckraker(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		// int navCircleLen = nav.navCircle.length;
		// MapLocation tempLoc = rc.getLocation();
		// for (int i = 0; i < navCircleLen; i++) {
		// tempLoc.add(nav.navCircle[i]);
		// System.out.println(tempLoc);
		// rc.setIndicatorDot(tempLoc, 255, 255, 255);
		// }

		// Team enemy = rc.getTeam().opponent();
		// int actionRadius = rc.getType().actionRadiusSquared;
		// for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
		// if (robot.type.canBeExposed()) {
		// // It's a slanderer... go get them!
		// if (rc.canExpose(robot.location)) {
		// System.out.println("e x p o s e d");
		// rc.expose(robot.location);
		// return;
		// }
		// }
		// }
		if (nav.tryMove(randomDirection()))
			System.out.println("I moved!");
	}

}