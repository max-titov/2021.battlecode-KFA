package muckrakertest;

import battlecode.common.*;

public strictfp class RobotPlayer {

	static RobotController rc;
	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	public static void run(RobotController rc) throws GameActionException {

		/**
		 * We have declared an object of type 'Robot'
		 */
		Robot me = null;

		/**
		 * In this switch statement, we will initialize the 'Robot' object to its
		 * respective type
		 */
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER:
				me = new EnlightenmentCenter(rc);
				break;
			case POLITICIAN:
				me = new Politician(rc);
				break;
			case SLANDERER:
				me = new Slanderer(rc);
				break;
			case MUCKRAKER:
				me = new Muckraker(rc);
				break;
		}

		while (true) {
			/**
			 * Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
			 */
			try {
				/**
				 * Here, we perform the takeTurn method, which is a method of 'Robot' This
				 * method is overridden in each subclass of Robot
				 */
				me.takeTurn();

				/**
				 * Clock.yield() makes the robot wait until the next turn, then it will perform
				 * this loop again
				 */
				Clock.yield();

			} catch (Exception e) {
				/**
				 * Print any exceptions if needed
				 */
				System.out.println(rc.getType() + " Exception");
				e.printStackTrace();
			}
		}
	}

}
