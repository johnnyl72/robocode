package johnnylam_samnav;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.*; 
/**
 * Cloud9 - a robot by (Sam Navarrete & Johnny Lam)
 * FOR 1V1, TERRIBLE IN EVERYTHING ELSE
 * heading - absolute angle in degrees with 0 facing up the screen, positive clockwise. 0 <= heading < 360. 
 * bearing - relative angle to some object from your robot's heading, positive clockwise. -180 < bearing <= 180
 * CREDIT TO WAVESURFING CHALLENGE BOT C FOR CIRCULAR TARGETTING
 */
public class Cloud9 extends AdvancedRobot
{		
	boolean movingForward;
	static double oldEnemyHeading; //keep static so we can keep track each scan
	static double prevEnergy = 100.0;		
	static double wallhitOne=0.0, wallhitTwo=0.0;
	
	/**
	 * run: Cloud9's default behavior
	 * Cloud9 movement acts like a pull back toy that swings far and rewinds step by step back & repeats
	 */

	public void run() {
		// Initialization of the robot should be put here

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(Color.cyan,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		while(true) {	
		ahead(50); //50 & 300 for more mobility
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		back(300);
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

	//typical stop and go, they fire when we are moving, we stop & they fire when we stop, we move
	//energy monitoring
	if(getDistanceRemaining()==0 && prevEnergy-e.getEnergy()>0){
    setAhead(45);
  	}
	prevEnergy = e.getEnergy();
	
	//turn car perpendicular so we can start strafing 
	setTurnRight(e.getBearing() + 90);
	
	//bulletpower picks either 3 or what the energy is
	double bulletPower = Math.min(3.0,getEnergy());
	//get my own coordinates
	double myX = getX();
	double myY = getY();
	
	//get enemy's cartesian coordinates by calculating position of your target relative to myself
	double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
	double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
	double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
	//enemy heading
	double enemyHeading = e.getHeadingRadians();
	double enemyHeadingChange = enemyHeading - oldEnemyHeading;
	//update oldEnemyHeading from last scan
	oldEnemyHeading = enemyHeading;
	double enemyVelocity = e.getVelocity();
	
	
	//Credit to Circular Targetting tutorial page from robowiki.net for the calculations
	double deltaTime = 0;
	double battleFieldHeight = getBattleFieldHeight(), 
	       battleFieldWidth = getBattleFieldWidth();
	// assign predictedX/Y from enemy coordinates
	double predictedX = enemyX, predictedY = enemyY;
	//point2d.distance is use to calculate the distance between two coordinates
	//formula to predict 
	while((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
		//WE ASSUME TARGET IS MOVIN CIRCULAR ARCS, so we predict its location base on the arcturn
		predictedX += Math.sin(enemyHeading) * enemyVelocity;
		predictedY += Math.cos(enemyHeading) * enemyVelocity;
		enemyHeading += enemyHeadingChange;
		//acknowledges the walls and assume bot will stop when reaching a wawll
		if(	predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0){
			predictedX = Math.min(Math.max(18.0, predictedX), 
			    battleFieldWidth - 18.0);	
			predictedY = Math.min(Math.max(18.0, predictedY), 
			    battleFieldHeight - 18.0);
			break;
			}
			
		}
	
	double degree = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
	//turn gun and radar
	setTurnGunRightRadians(Utils.normalRelativeAngle(degree - getGunHeadingRadians()));
	//1v1 perfect lock, based off turn multipler lock, can multiply for a wider radius scan but our circular targetting would get messed up
	setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
	
	
	
		if(e.getDistance() < 200){ // the the enemy is further than 200p
		        setFire(3); // fire full power
				}
	 	else if(e.getDistance() < 400){
		        setFire(2.7); // else fire 2
				}
		else{
				setFire(2); // else fire 2 SPRAY & PRAY
		}
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 * I found that sometimes it strafes back and forth in the corner with limited mobility so this should open up the mobility
	 */
	public void onHitWall(HitWallEvent e) {
		if (wallhitOne==0)
			wallhitOne=getX();
		else if (wallhitOne!=0)
			wallhitTwo=getX();
		
		if ((Math.abs(wallhitOne-wallhitTwo)<100))
			turnRight(30);
	}
	
	public void reverseDirection() {
		if (movingForward) {
			back(150); //swing us back
			movingForward = false;	
		} else {	
			ahead(150); //swing us forward for more room to strafe
			movingForward = true;
		}
	}	
	
	//this should protect Cloud9 from getting targeted while fighting another bot
/*	public void onHitByBullet(HitByBulletEvent e) {
		resetPosition();
		ahead(100);
	}
*/

	public void onHitRobot(HitRobotEvent e) {
		//reverseDirection(); // bounces off when we hit wall
		if(e.getBearing()>-90 && e.getBearing()<=90){
			setBack(100);
			resetPosition();
		} else {
			setAhead(100);
			resetPosition();
		}
		
	}

	public void resetPosition(){
		turnRight(90);
		turnRadarRight(360);
	}
}// class