/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

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




/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */



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
	//NetworkTableEntry pipeline0 = table.getEntry("pipeline");
	double y = ty.getDouble(0);
	double area;
	double x,v;
	
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
		
	}

	
	@Override
	public void autonomousInit() {
		autoCount = 0;
		camMode.setNumber(0);
		ledMode.setNumber(0);
	}

	@Override
	public void autonomousPeriodic() {
		area = ta.getDouble(0);
		v = tv.getDouble(0);
		x = tx.getDouble(0);
		trickierVisiontracking(0);
	}

	//////////////axis/////////////
	public double LeftStick, RightStick, LeftThrottle, RightThrottle;
	public boolean in, out, Target, AutoSuck, SuckB;
	double steeringAdjust;
	double headingError;
	@Override
	public void teleopPeriodic() {
		
		controllerInput();
		driveTrain();
		cubeIntake(in, out);
		Elevator(LeftThrottle, RightThrottle);
		
	}

	@Override
	public void testPeriodic() {
	}
	
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
	
	void cubeIntake(boolean inin, boolean inout) {
			if(inin) {
				LeftArm.set(.44);
				RightArm.set(-.44);
			}
			
			else if(inout) {
				LeftArm.set(-1);
				RightArm.set(1);
			}
			
			else{
				LeftArm.set(0);
				RightArm.set(0);
			}
	}
	
	void Elevator(double liftUp, double liftDown) {
			if(liftUp > .1) {
				Elevator.set(liftUp);
			}
			else if(liftDown > .1) {
				Elevator.set(-liftDown);
			}
			else {
				Elevator.set(0);
			}
	}
	
	void trickierVisiontracking(int RobotLocation) {
			autoCount ++;
			if(autoCount < 25) {
				Elevator(1,0);
			}else if(autoCount == 25){ 
				Elevator(0,0);
			}else if(autoCount < 75 && autoCount > 25) {
				autoDrivetrain(.4,.45);
			}else if (autoCount < 125 && autoCount > 75) {
				autoDrivetrain(-.4,-.45);
			}else if (autoCount < 175 && autoCount > 125) {
				autoDrivetrain(.4,.45);
				cubeIntake(true, false);
			}else if (autoCount > 175) {
			
					if(v == 1.0) {
						if(area < 7) {
							error = x;
							error = error/35;
							ZoomBoi.arcadeDrive(.65, error);
							}
						else {
							ZoomBoi.arcadeDrive(0, 0);
							}
						}
////////////////////////switch for field position and switch colors/////////////////////////////////////
					
				if(v != 1.0) { //switch statement to add for different field areas
					autoDrivetrain(-.2, .2);
				}
				
				
				if(area > 10) {
					cubeIntake(false, true);
				}
			
			}
			else {
				System.out.println("cry");
			}
	}
	
}
