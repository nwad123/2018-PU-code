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
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DriverStation;

public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();

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
	public double x,v, xf, xi, errorP, errorD, kP, kD, leftspin, 
			rightspin, spinDirection;
	
	String GameData;
	
	public boolean LeftStartPosition, CenterStartPosition, RightStartingPosition, 
			LeftSwitchLight, RightSwitchLight, timetotrack, cubegot = false;
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
	int autoCount = -25;
	double error;
	
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
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
		
		GameData = DriverStation.getInstance().getGameSpecificMessage();
		DetermineAuto();
	}

	@Override
	public void autonomousPeriodic() {
		area = ta.getDouble(0);
		v = tv.getDouble(0);
		x = tx.getDouble(0);
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
	/**************BASIC METHODS**************************************************/
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
	
	void controllerInput() {
			LeftStick = Xbox1.getRawAxis(1);
			RightStick = Xbox1.getRawAxis(5); //Driving
			
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
	
	
			/////////////////Vision power////////////
	void tracktheSwitch() {
			if(cubegot) {	
			//////////after cube pickup is done//////
			if(CenterStartPosition) {
				for(int autoCountV = 0; autoCountV < 40; autoCountV ++) {
					ZoomBoi.arcadeDrive(.5, spinDirection);
				} if (autoCount >= 40) {
					timetotrack = true;
				}
			} else {
				timetotrack = true;
			}
		////////wait for initial turn then do a vision///////
				if(timetotrack) {
					if(v == 1.0) {
						if(area < 7) {
							kP = .75;
							kD = .01;
							
							xi = x;
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
					ZoomBoi.arcadeDrive(.5, spinDirection);
				}
			}
		///////////////////////////////////////////////////////
		}
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
				else if (autoCount == 175) {
					cubegot = true;
				}
		}
	}
	void DetermineAuto() { //////////////call at autoinit/////////////////
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
		
		if(RightSwitchLight) {
			spinDirection = 1;
		}
		else if(LeftSwitchLight) {
			spinDirection = -1;
		}
	}
/**********************************************************************************************/
		/*************************High Level Methods***********************************/
	void AutoRoutine() {
		AutogetCube();
		tracktheSwitch();
	}
}
