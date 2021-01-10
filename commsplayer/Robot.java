package commsplayer;
import battlecode.common.*;

public class Robot {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    int turnCount = 0;
    int creatorID = 0;
    int xOffset = 0;
    int yOffset = 0;
    MapLocation creatorLoc;
    Team myTeam;

    public Robot(RobotController r){
        this.rc = r;
        this.myTeam = rc.getTeam();
    }

    //Gets the id of the enlightenment center that spawned the robot in
    //Hardcoded for the robot's first turn only
    public int getSpawnECID() throws GameActionException{
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2, myTeam);
        for(int i = 0; i < nearbyRobots.length; i++){
            if(nearbyRobots[i].team == myTeam && nearbyRobots[i].type == RobotType.ENLIGHTENMENT_CENTER){
                this.creatorID = nearbyRobots[i].ID;
                this.creatorLoc = nearbyRobots[i].location; 
            }
        }
        return 0; //TEMPORARY FIX BY MAX SO CODE COULD BE COMPILED
    }

    //This method is also meant for the first turn
    public void calculateOffset(MapLocation currentLoc) throws GameActionException{
        this.xOffset = creatorLoc.x - currentLoc.x;
        this.yOffset = creatorLoc.y - currentLoc.y;
    }

    //Add the other input fields here
    public void createMessage(int deltaX, int deltaY) throws GameActionException{
        int packedMessage = 0;
        //packedMessage = (packedMessage << 12) + 1;
        packedMessage = (packedMessage << 6) + deltaX;
        packedMessage = (packedMessage << 6) + deltaY;
        System.out.println("Packed message: " + packedMessage);
        //Binary Output
        //System.out.println("Binary message: " + Integer.toString(packedMessage,2))
        if(rc.canSetFlag(packedMessage)) {
            rc.setFlag(packedMessage);
        } else {
            System.out.println("Flag failed to send :(");
        }
    }

    public void interpretMessage(int id) throws GameActionException{
        if(rc.canGetFlag(id)){
            int packedMessage = rc.getFlag(id);
            int sentDeltaY = packedMessage & 0b111111;
            int sendDeltaX = (packedMessage >> 6) & 0b111111;

            System.out.println("Original Y = 57: " + Integer.toString(sentDeltaY));
            System.out.println("Original X = 7: " + Integer.toString(sendDeltaX));
        }
    }

    public void takeTurn() throws GameActionException{
        turnCount += 1;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}