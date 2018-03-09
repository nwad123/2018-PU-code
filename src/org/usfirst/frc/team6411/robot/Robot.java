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
import edu.wpi.first.wpilibj.GenericHID;

public class Robot extends IterativeRobot {

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
	
	public boolean LeftStartPosition = false, CenterStartPosition = false, RightStartPosition = false, 
			BaselineAuto = false, LeftSwitchLight = false, RightSwitchLight = false, 
			timetotrack = false, cubegot = false, RobotAndSwitch = false;
	
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
		Right1.setSafetyEnabled(true);
		Right2.setSafetyEnabled(true);
		Left1.setSafetyEnabled(true);
		Left2.setSafetyEnabled(true);
		
		Right1.setExpiration(.1);
		Right2.setExpiration(.1);
		Left1.setExpiration(.1);
		Left2.setExpiration(.1);
		
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
		ledMode.setNumber(1);
	}

	
	@Override
	public void autonomousInit() {
		autoCount = 0;
		camMode.setNumber(0);
		ledMode.setNumber(0);
		
		BeetBotPosition = StartingPositionChooser.getSelected();
		GameData = DriverStation.getInstance().getGameSpecificMessage();
		
		autoCountV = 0;
		cubegot = false;
		SwitchSide();
	}

	@Override
	public void autonomousPeriodic() {
		area = ta.getDouble(0);
		v = tv.getDouble(0);
		xfromLimelight = tx.getDouble(0);
	}

	//////////////axis from controller/////////////
	public double LeftStick, RightStick, LeftThrottle, RightThrottle;
	public boolean in, out;
	///////////////////////////////////////////////
	@Override
	public void teleopPeriodic() {
		ledMode.setNumber(1);
		camMode.setNumber(1);
		controllerInput();
		ZoomBoi.tankDrive(-LeftStick, -RightStick);
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
			
			in = Xbox1.getRawButton(6);
			out = Xbox1.getRawButton(5); //Intake
			
			LeftThrottle = Xbox1.getRawAxis(2);
			RightThrottle = Xbox1.getRawAxis(3); //Elevator
			
			RightThrottle = RightThrottle * .75; //Elevator
	}
	
	void cubeIntake() {
			if(in) {
				LeftArm.set(-1);
				RightArm.set(1);
			}
			
			else if(out) {
				LeftArm.set(1);
				RightArm.set(-1);
			}
			
			else{
				LeftArm.set(0);
				RightArm.set(0);
			}
	}
	
	void Elevator() {
			if(LeftThrottle > .1) {
				Elevator.set(LeftThrottle); //up
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
						if(area < 1.7) {
							kP = .75;				//this sets up the PD control for the vision
							kD = .01;				//we don't use full PID because of rare sustained
													//errors. We use the D in order to smooth out the 
							xi = xfromLimelight - 4;					//large overcorrects though
							xi = xi/27;
							errorP = xi;			//read a few PID loop things to understand how to tune this
							errorD = (xf - xi)/.02;	//I suggest "frc coding done right"
							
							error = (kP * errorP) + (kD * errorD);
							
							ZoomBoi.arcadeDrive(.25, error);
							}
						else {
							ZoomBoi.arcadeDrive(0, 0);
							}
						}
				if(v != 1.0) { 
					ZoomBoi.arcadeDrive(.2, spinDirection);
					}
			} 
		///////////////////////////////////////////////////////
		
	}
	
	void AutogetCube() {
		if(!cubegot) {
				autoCount ++;
				if(autoCount < 25) {
					autoDrivetrain(.3,.33);
				}else if(autoCount == 25){ 
					autoDrivetrain(0,0);
				}else if(autoCount < 75 && autoCount > 25) {
					autoDrivetrain(-.35,-.35);
				}else if (autoCount >= 75) {
					autoDrivetrain(0,0);
					Elevator.set(0);
					LeftArm.set(0);
					RightArm.set(0);
				}
		}
		if (autoCount >= 75) {
			cubegot = true;
		}
	}
	
	void CrosstheBaseline() {
		autoCountB ++;
		if(autoCountB > 300) {
			ZoomBoi.arcadeDrive(.5, (-.05)*spinDirection);
		}
	}

	void SwitchSide() {
		switch (GameData.charAt(0)) {
		case 'L':
			LeftSwitchLight = true;
			break;
		case 'R':
			RightSwitchLight = true;
			break;
		}
		
		switch(StartingPositionChooser.getSelected()) {
		case Left:
			if(LeftSwitchLight) {
				RobotAndSwitch = true;
			}
			break;
		case Right:
			if(RightSwitchLight) {
				RobotAndSwitch = true;
			}
			break;
		case Center:
			RobotAndSwitch = true;
			if(LeftSwitchLight) {
				spinDirection = -1;
			} 
			if(RightSwitchLight) {
				spinDirection = 1;
			}
			break;
		case Baseline:
			RobotAndSwitch = false;
			break;
		}
		
	}
	
	void DetermineNecessaryTurns() {
		//////////////call at auto init/////////////////
		
	/////////////////////set turning if needed////////////////////////
		if(LeftStartPosition && LeftSwitchLight) {
			RobotAndSwitch = true;
		} else if(RightStartPosition && RightSwitchLight){
			RobotAndSwitch = true;
		}else if(CenterStartPosition){ //if it's in the center it chooses which way it's gonna turn
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
//			autoCountV ++;
//			//////////after cube pickup is done//////
//		if(CenterStartPosition) { //uses spin value from DetermineNecessaryTurn to turn the right way
//				if(autoCountV < 30) {
//					ZoomBoi.arcadeDrive(.5, spinDirection);
//				} if (autoCountV >= 30) {
//					timetotrack = true;
//					ZoomBoi.arcadeDrive(0, 0);
//				} 
//		} else if(!CenterStartPosition) { //if the bot isn't in the center the target is right ahead of it
				timetotrack = true;
			}
//		}
	}
/**********************************************************************************************/
		/*************************High Level Methods***********************************/
	void AutoRoutine() {
		//don't forget to add a baseline routine
			AutogetCube();
			ExecuteNecessaryTurns();
			tracktheSwitch();
	}
}
