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
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;



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
//	NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
//	NetworkTableEntry tx = table.getEntry("tx");
//	NetworkTableEntry ty = table.getEntry("ty");
//	NetworkTableEntry ta = table.getEntry("ta");
//	NetworkTableEntry tv =  table.getEntry("tv");
//	double v = tv.getDouble(0);
//	double x = tx.getDouble(0);
//	double y = ty.getDouble(0);
//	double area = ta.getDouble(0);

	//////////////////////Joysticks////////////////////////
	Joystick Xbox1 = new Joystick(0);

	////////////////////DriveTrain////////////////////////
	Victor Right1 = new Victor(0);
	Victor Right2 = new Victor(1);
	Victor Left1 = new Victor(2);
	Victor Left2 = new Victor(3);
	
	////////////////////Cube Grabber//////////////////////
	Spark LeftArm = new Spark(4);
	Spark RightArm = new Spark(5);
	Spark Elevator = new Spark(6);
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
//		//////////////////DriveTrain//////////////////////////
		
//		Right1.enableDeadbandElimination(true);
//		Right2.enableDeadbandElimination(true);
//		Left1.enableDeadbandElimination(true);
//		Left2.enableDeadbandElimination(true);
//		
		/////////////////////Cube Grabber/////////////////////
		LeftArm.enableDeadbandElimination(true);
		RightArm.enableDeadbandElimination(true);
		Elevator.enableDeadbandElimination(true);
		
		
		
	
		
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	
	//////////////axis/////////////
	public double LeftStick, RightStick, LeftThrottle, RightThrottle;
	public boolean in, out, Target, AutoSuck, SuckB;
	double steeringAdjust;
	double headingError;
	@Override
	public void teleopPeriodic() {
		
		controllerInput();
		driveTrain();
		cubeIntake();
		Elevator();
		
//		////////////////////////////////////LIMELIGHT//////////////
//		
//		steeringAdjust = 0.0;
//		///////////////////////////Target Aquired/////////////////
//		if (v == 1)
//		{ 
//			Target = true;
//		}
//			
//		else {
//		     //add something here to say "not seeing"
//			Target = false;
//		}
//////////////////////////////////////AutoSucc (tm)//////////////////////////
//		if(Target && SuckB) {
//			AutoSuck = true;
//		}
//		
//		if(AutoSuck) {
//				if(x > 3) { //this is if it is to the right of the camera
//				
//					Right1.set(.5);
//					Right2.set(.5);
//					
//					Left1.set(-.5);
//					Left2.set(-.5);
//				}
//				else if(x < -3) { //this is if it is to the left of the camera
//					Right1.set(-.5);
//					Right2.set(-.5);
//					
//					Left1.set(.5);
//					Left2.set(.5);
//				}
//				else if(x <= 3 && x >= -3) { // This is when the cube is in the zone of robot convergence
//					Right1.set(.5);
//					Right2.set(.5);
//					
//					Left1.set(.5);
//					Left2.set(.5);
//					
//					LeftArm.set(-intakeSpd);
//					RightArm.set(intakeSpd);
//				}
//				else if(area > "some value") { //This is when the cube has filled enough of the screen and is deemed thoroughly sucked
//					AutoSuck = false;
//				}
//			}
//		
	}

	/**
	 * This function is called periodically during test mode.
	 */
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
	
	void controllerInput() {
			LeftStick = Xbox1.getRawAxis(1);
			RightStick = Xbox1.getRawAxis(5); //Driving
			
			in = Xbox1.getRawButton(5);
			out = Xbox1.getRawButton(6); //Intake
			
			LeftThrottle = Xbox1.getRawAxis(2);
			RightThrottle = Xbox1.getRawAxis(3); //Elevator
			
			LeftThrottle = LeftThrottle * .6;
			RightThrottle = RightThrottle * .6; //Elevator
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
	
}
