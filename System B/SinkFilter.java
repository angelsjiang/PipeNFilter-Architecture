/******************************************************************************************************************
* File:SinkFilter.java
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SinkFilter extends FilterFramework
{
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("YYYY:DD:HH:MM:SS");

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter

		String data = "";				// collecting data

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading ");

		FileWriter fw;

		try {
			fw = new FileWriter("./Outputs/OutputB.csv", true);

			fw.write("Time,");
			fw.write("Velocity,");
			fw.write("Altitude,");
			fw.write("Pressure,");
			fw.write("Temperature, \n");

			fw.flush();


			while (true)
			{
				try
				{
					/***************************************************************************
					// We know that the first data coming to this filter is going to be an ID and
					// that it is IdLength long. So we first decommutate the ID bytes.
					****************************************************************************/

					id = 0;

					for (i=0; i<IdLength; i++ )
					{
						databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

						id = id | (databyte & 0xFF);		// We append the byte on to ID...

						if (i != IdLength-1)				// If this is not the last byte, then slide the
						{									// previously appended byte to the left by one byte
							id = id << 8;					// to make room for the next byte we append to the ID

						} // if

						bytesread++;						// Increment the byte count

					} // for

					/****************************************************************************
					// Here we read measurements. All measurement data is read as a stream of bytes
					// and stored as a long value. This permits us to do bitwise manipulation that
					// is neccesary to convert the byte stream into data words. Note that bitwise
					// manipulation is not permitted on any kind of floating point types in Java.
					// If the id = 0 then this is a time value and is therefore a long value - no
					// problem. However, if the id is something other than 0, then the bits in the
					// long value is really of type double and we need to convert the value using
					// Double.longBitsToDouble(long val) to do the conversion which is illustrated.
					// below.
					*****************************************************************************/

					measurement = 0;

					for (i=0; i<MeasurementLength; i++ )
					{
						databyte = ReadFilterInputPort();
						measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

						if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
						{												// previously appended byte to the left by one byte
							measurement = measurement << 8;				// to make room for the next byte we append to the
																		// measurement
						} // if

						bytesread++;									// Increment the byte count

					} // if

					/****************************************************************************
					// Here we look for an ID of 0 which indicates this is a time measurement.
					// Every frame begins with an ID of 0, followed by a time stamp which correlates
					// to the time that each proceeding measurement was recorded. Time is stored
					// in milliseconds since Epoch. This allows us to use Java's calendar class to
					// retrieve time and also use text format classes to format the output into
					// a form humans can read. So this provides great flexibility in terms of
					// dealing with time arithmetically or for string display purposes. This is
					// illustrated below.
					****************************************************************************/

					if ( id == 0 ) // time
					{
	//					TimeStamp.setTimeInMillis(measurement);
//						System.out.print("Time = " + TimeStampFormat.format(measurement));
						data = TimeStampFormat.format(measurement) + ",";
						fw.write(data);
						fw.flush();

					} // if

					/****************************************************************************
					// Here we pick up a measurement (ID = 4 in this case), but you can pick up
					// any measurement you want to. All measurements in the stream are
					// decommutated by this class. Note that all data measurements are double types
					// This illustrates how to convert the bits read from the stream into a double
					// type. Its pretty simple using Double.longBitsToDouble(long value). So here
					// we print the time stamp and the data associated with the ID we are interested
					// in.
					****************************************************************************/

					if ( id == 1)   // this will be velocity measurement
					{
//						System.out.print("Velocity = " + Double.longBitsToDouble(measurement) + ",");
						data = Double.longBitsToDouble(measurement) + ",";
						fw.write(data);
						fw.flush();

					}

					if (id == 2)   // this will be altitude measurement
					{
						double altitude = Double.longBitsToDouble(measurement);
//						System.out.println("Altitude:::: " + altitude);
						if(altitude < 0) {
							altitude = Math.abs(altitude);
						}
						data = altitude + "*,";
//						System.out.print("Altitude = " + data);

						fw.write(data);
						fw.flush();

					}

					if (id == 3)   // this will be pressure measurement
					{
//						System.out.print("Pressure = " + Double.longBitsToDouble(measurement) + ",");
						data = Double.longBitsToDouble(measurement) + ",";
						fw.write(data);
						fw.flush();

					}

					if ( id == 4 )   // temperature
					{
//						System.out.print("Temperature = " + Double.longBitsToDouble(measurement));
						data = Double.longBitsToDouble(measurement) + ", \n";
						fw.write(data);
						fw.flush();

						//					System.out.print( TimeStampFormat.format(TimeStamp.getTime()) + " ID = " + id + ", Temperature = " + Double.longBitsToDouble(measurement));

					} // if


					System.out.print( "\n" );


				} // try

				/*******************************************************************************
				*	The EndOfStreamExeception below is thrown when you reach end of the input
				*	stream (duh). At this point, the filter ports are closed and a message is
				*	written letting the user know what is going on.
				********************************************************************************/

				catch (EndOfStreamException e)
				{
					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;

				} // catch

			} // while

			fw.close(); // close FileWriter

		} catch (IOException e) {

			e.printStackTrace();
			return;
		}


	} // run

} // SingFilter