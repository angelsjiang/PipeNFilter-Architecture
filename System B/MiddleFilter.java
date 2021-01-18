import java.io.FileWriter;
import java.io.IOException;

/******************************************************************************************************************
* File:MiddleFilter.java
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

public class MiddleFilter extends FilterFramework
{
	public void run()
    {


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		long measurement;
		double data;
		double prev = 0;
		double prevPrev = 0;
		boolean wildJump;

		int IdLength = 4;				// This is the length of IDs in the byte stream
		int id;
		int i;

		FileWriter fw;
		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Middle Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{
//				databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

				fw = new FileWriter("./WildPoint.csv", true);

				// set up this to grab id = 2
				id = 0;
				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...
					WriteFilterOutputPort(databyte);

					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for


				// set up to grab Altitude measurement
				measurement = 0;

				for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort();
					WriteFilterOutputPort(databyte);

					measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

					if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
					{												// previously appended byte to the left by one byte
						measurement = measurement << 8;				// to make room for the next byte we append to the
						// measurement
					} // if

					bytesread++;									// Increment the byte count

				} // if


				wildJump = false; // default this value to false;
				data = 0; // reset data variable

//				if (id == 2)   // this will be altitude measurement
//				{
//					// 1 foot = 0.3048 meters
//
//					data = Double.longBitsToDouble(measurement);
//					data = data *  0.3048;
//					System.out.println("Altitude = " + data);
//
//					// and compare with prev data
//					if(prev != 0 && Math.abs(prev - data) > 100) {
//
//						// if there is a wild jump aka difference of >100m
//						// pass the data to WildSinkFilter || write to wildoutput
//						System.out.println("Wild jump data: " + data);
//						fw.write(data + ",\n");
//						fw.flush();
//
//						wildJump = true;
//					}
//
//					// pass down to store the values as prevPrev and prev
//					prevPrev = prev;
//					prev = data;
//
//					// if there is a wildJump, then need to modify output data
//					if(wildJump) {
//
//						// get the average of the previous 2 altitudes
//						// if prePrev == 0, which means it's the 2nd frame
//
//						if(prevPrev == 0) {
//
//							// then set data to the prev data
//							data = prev;
//						}
//						else {
//
//							// else take the average of the previous 2 altitudes
//							data = (prevPrev + prev) / 2.00000;
//
//						}
//
//					}
//
////					databyte = (byte)data;
//				}

//				WriteFilterOutputPort(databyte);
				byteswritten++;

			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

			catch (IOException e) {
				ClosePorts();
				e.printStackTrace();
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;
			}

		} // while

   } // run

} // MiddleFilter