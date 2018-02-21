package org.usfirst.frc.team6411.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

public class BeetBot extends IterativeRobot {

	//////////////////////LimeLight////////////////////////
	NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	NetworkTableEntry tx = table.getEntry("tx");
	NetworkTableEntry ty = table.getEntry("ty");
	NetworkTableEntry ta = table.getEntry("ta");
	NetworkTableEntry tv = table.getEntry("tv");
	NetworkTableEntry ledMode = table.getEntry("ledMode");
	NetworkTableEntry camMode = table.getEntry("camMode");
	double y = ty.getDouble(0);
	double area;
	
	/////////////////////auto vars/////////////////////////
	public double xfromLimelight, v, xf, xi, error, errorP, errorD, kP, kD, leftspin, 
			rightspin, spinDirection;
	
	String GameData;
	
	public boolean LeftStartPosition, CenterStartPosition, RightStartPosition, BaselineAuto,
			LeftSwitchLight, RightSwitchLight, timetotrack, cubegot, RobotAndSwitch = false;
	
	//////////////////auto chooser stuff////////////////////
	private static final String Left = "Left";
	private static final String Center = "Center";
	private static final String Right = "Right";
	private static final String Baseline = "Baseline and Cube";
	private String BeetBotPosition;
	private SendableChooser<String> StartingPositionChooser = new SendableChooser<>();
	
	//////////////////////Joysticks////////////////////////
	Joystick Xbox1 = new Joystick(0);

	////////////////////DriveTrain////////////////////////
	Victor Right1 = new Victor(0);
	Victor Right2 = new Victor(1);
	Victor Left1 = new Victor(2);
	Victor Left2 = new Victor(3);
	
	SpeedControllerGroup leftboi = new SpeedControllerGroup(Left1, Left2);
	SpeedControllerGroup rightboi = new SpeedControllerGroup(Right1, Right2);
	
	DifferentialDrive ZoomBoi = new DifferentialDrive(leftboi, rightboi);
	
	////////////////////Cube Grabber//////////////////////
	Spark LeftArm = new Spark(4);
	Spark RightArm = new Spark(5);
	Spark Elevator = new Spark(6);
	
	////////////////////random variables//////////////////
	int autoCount, autoCountV, autoCountB = 0;
	
	@Override
	public void robotInit() {
		StartingPositionChooser.addDefault("The Left", Left);
		StartingPositionChooser.addObject("The Center", Center);
		StartingPositionChooser.addObject("The Right", Right);
		StartingPositionChooser.addObject("Baseline Only", Baseline);
		SmartDashboard.putData("Auto(boi) choices", StartingPositionChooser);
		
		/////////////////////Cube Grabber/////////////////////
		LeftArm.enableDeadbandElimination(true);
		RightArm.enableDeadbandElimination(true);
		Elevator.enableDeadbandElimination(true);
		
		/////////////////////get switch/scale/////////////////////////
		GameData = DriverStation.getInstance().getGameSpecificMessage();
	}

	
	@Override
	public void autonomousInit() {
		autoCount = 0;
		camMode.setNumber(0);
		ledMode.setNumber(0);
		
		BeetBotPosition = StartingPositionChooser.getSelected();
		GameData = DriverStation.getInstance().getGameSpecificMessage();
		DetermineNecessaryTurns();
	}

	@Override
	public void autonomousPeriodic() {
		area = ta.getDouble(0);
		v = tv.getDouble(0);
		xfromLimelight = tx.getDouble(0);
		AutoRoutine();
	}

	//////////////axis from controller/////////////
	public double LeftStick, RightStick, LeftThrottle, RightThrottle;
	public boolean in, out;
	///////////////////////////////////////////////
	@Override
	public void teleopPeriodic() {
		
		controllerInput();
		driveTrain();
		cubeIntake();
		Elevator();
		
	}

	@Override
	public void testPeriodic() {
	}
	
/*****************************************************************************************/
	/*******************************Basic Methods***********************************/
	void driveTrain() {	
			LeftStick = LeftStick * -1;
			RightStick = RightStick * 1;
			
			Right1.set(RightStick);
			Right2.set(RightStick);
			
			Left1.set(LeftStick);
			Left2.set(LeftStick);
			
	}
	
	void autoDrivetrain(double lauto, double rauto) {
		Right1.set(-rauto);
		Right2.set(-rauto);
		
		Left1.set(lauto);
		Left2.set(lauto);
	}
	
	void controllerInput() { //add function for cubic output from the Xbox controller
			LeftStick = Xbox1.getRawAxis(1);
			RightStick = Xbox1.getRawAxis(5); //Driving
			
			/////////////////////(.2*x^5 - .333*x^3)/(.1334)//////////////
			/////////https://www.desmos.com/calculator/5rdpqmkmue/////////
			LeftStick =  (.2*Math.pow(LeftStick, 5)-(.33333*Math.pow(LeftStick, 3)));
			RightStick = (.2*Math.pow(RightStick, 5)-(.33333*Math.pow(RightStick, 3)));
			
			LeftStick = (LeftStick/.13334);
			RightStick = (RightStick/.13334);
			//////////////////////////////////////////////////////////////
			
			in = Xbox1.getRawButton(5);
			out = Xbox1.getRawButton(6); //Intake
			
			LeftThrottle = Xbox1.getRawAxis(2);
			RightThrottle = Xbox1.getRawAxis(3); //Elevator
			
			LeftThrottle = LeftThrottle * .75;
			RightThrottle = RightThrottle * .75; //Elevator
	}
	
	void cubeIntake() {
			if(in) {
				LeftArm.set(.44);
				RightArm.set(-.44);
			}
			
			else if(out) {
				LeftArm.set(-1);
				RightArm.set(1);
			}
			
			else{
				LeftArm.set(0);
				RightArm.set(0);
			}
	}
	
	void Elevator() {
			if(LeftThrottle > .1) {
				Elevator.set(LeftThrottle);
			}
			else if(RightThrottle > .1) {
				Elevator.set(-RightThrottle);
			}
			else {
				Elevator.set(0);
			}
	}
/**************************************************************************************/
	/***************************Low Level Auto Methods**************************/
	void tracktheSwitch() {			
		////////wait for initial turn then do a vision///////
				if(timetotrack && RobotAndSwitch) { //this makes sure that the robot and the switch are 												
					if(v == 1.0) {                  //on the same side, otherwise that would be a yikes
						if(area < 7) {
							kP = .75;				//this sets up the PD control for the vision
							kD = .01;				//we don't use full PID because of rare sustained
													//errors. We use the D in order to smooth out the 
							xi = xfromLimelight;					//large overcorrects though
							xi = xi/35;
							errorP = xi;
							errorD = (xf - xi)/.02;
							
							error = (kP * errorP) + (kD * errorD);
							
							ZoomBoi.arcadeDrive(.75, error);
							}
						else {
							ZoomBoi.arcadeDrive(0, 0);
							}
						}
				if(v != 1.0) { 
					ZoomBoi.arcadeDrive(.2, spinDirection);
				}
			} else {
				ZoomBoi.arcadeDrive(0, 0);
			}
		///////////////////////////////////////////////////////
		
	}
	
	void AutogetCube() {
		if(!cubegot) {
				autoCount ++;
				if(autoCount < 25) {
					Elevator.set(1);
				}else if(autoCount == 25){ 
					Elevator.set(0);
				}else if(autoCount < 75 && autoCount > 25) {
					autoDrivetrain(.4,.47);
				}else if (autoCount < 125 && autoCount > 75) {
					autoDrivetrain(-.4,-.47);
				}else if (autoCount < 175 && autoCount > 125) {
					//add elevator command
					autoDrivetrain(.4,.45);
					LeftArm.set(.34);
					RightArm.set(-.34);
				}
				else if (autoCount >= 175) {
					cubegot = true;
				}
		}
	}
	
	void CrosstheBaseline() {
		autoCountB ++;
		if(autoCountB > 300) {
			ZoomBoi.arcadeDrive(.5, (-.2)*spinDirection);
		}
	}
	void DetermineNecessaryTurns() {
		//////////////call at auto init/////////////////
		
	//////////determines turning if needed by switch colors//////////	
		if(GameData.charAt(0) == 'L') {
			LeftSwitchLight = true;
			RightSwitchLight = false;
		}
		else if(GameData.charAt(0) == 'R') {
			RightSwitchLight = true;
			LeftSwitchLight = false;
		}
		else {
			RightSwitchLight = false;
			LeftSwitchLight = false;
		}
		
	//////////////////determine position in boolean///////////////////
		//possibly change to boolean var(){} for simplcity
		if(BeetBotPosition == Left) {
			LeftStartPosition = true;
		} else if(BeetBotPosition == Right) {
			RightStartPosition = true;
		} else if(BeetBotPosition ==  Center) {
			CenterStartPosition = true;
		} else if(BeetBotPosition == Baseline) {
			BaselineAuto = true;
		}
		
	/////////////////////set turning if needed////////////////////////
		if((LeftStartPosition && LeftSwitchLight) || (RightStartPosition && RightSwitchLight)) {
			RobotAndSwitch = true;
		} else if(CenterStartPosition){ //if it's in the center it chooses which way it's gonna turn
			RobotAndSwitch = true;
				if(LeftSwitchLight) {
					spinDirection = -1;
				} else if(RightSwitchLight) {
					spinDirection = 1;
				}
		} else {
			RobotAndSwitch = false;
		}
	}
	
	void ExecuteNecessaryTurns() {
		if(cubegot) {	
			//////////after cube pickup is done//////
			if(CenterStartPosition) { //uses spin value from DetermineNecessaryTurn to turn the right way
				for(autoCountV = 0; autoCountV < 40; autoCountV ++) {
					ZoomBoi.arcadeDrive(.5, spinDirection);
				} if (autoCountV >= 40) {
					timetotrack = true;
				} 
			} else { //if the bot isn't in the center the target is right ahead of it
				timetotrack = true;
			}
		}
	}
/**********************************************************************************************/
		/*************************High Level Methods***********************************/
	void AutoRoutine() {
		//don't forget to add a baseline routine
		AutogetCube();
		if(!BaselineAuto) {
			ExecuteNecessaryTurns();
			tracktheSwitch();
		} else if(BaselineAuto) {
			CrosstheBaseline();
		}
	}
}
