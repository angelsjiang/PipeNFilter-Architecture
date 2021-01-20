import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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

public class SourceMergeFilter extends FilterFramework
{
	public void run()
	{

		String fileNameA = "SubSetA.dat";	// Input data file.
		String fileNameB = "SubSetB.dat";
		int intRead = 0;					// Number of bytes read from the input file.
		int intWritten = 0;				// Number of bytes written to the stream.
		DataInputStream inA;
		DataInputStream inB;

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyteA = 0;
		byte databyteB = 0;
		int bytesread = 0;
		int byteswritten = 0;
		long measurementA = 0;
		long measurementB = 0;
		int idA = 0;
		int idB = 0;
		int i;


		try
		{
			/***********************************************************************************
			 *	Here we open the file and write a message to the terminal.
			 ***********************************************************************************/

			inA = new DataInputStream(new FileInputStream(fileNameA));
			inB = new DataInputStream(new FileInputStream(fileNameB));
			System.out.println("\n" + this.getName() + "::Source reading file..." );

			boolean ASmaller = true;
			boolean BSmaller = true;


			/***********************************************************************************
			 *	Here we read the data from the file and send it out the filter's output port one
			 * 	byte at a time. The loop stops when it encounters an EOFExecption.
			 ***********************************************************************************/

			while(true)
			{



				if(ASmaller) {
					// get data from File A
					idA = 0;
					for (i = 0; i < IdLength; i++) {

						databyteA = inA.readByte();    // This is where we read the byte from the stream...
						idA = idA | (databyteA & 0xFF);        // We append the byte on to ID...

						if (i != IdLength - 1)                // If this is not the last byte, then slide the
						{                                    // previously appended byte to the left by one byte
							idA = idA << 8;                    // to make room for the next byte we append to the ID

						} // if

						bytesread++;                        // Increment the byte count

					} // for


					// Grabbing measurement from File A
					measurementA = 0;
					for (i = 0; i < MeasurementLength; i++) {

						databyteA = inA.readByte();

						measurementA = measurementA | (databyteA & 0xFF);    // We append the byte on to measurement...

						if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
						{                                                // previously appended byte to the left by one byte
							measurementA = measurementA << 8;                // to make room for the next byte we append to the

						} // if

						bytesread++;                        // Increment the byte count

					} // if


				}


				if(BSmaller) {
					// get data from File B
					idB = 0;
					for (i = 0; i < IdLength; i++) {

						databyteB = inB.readByte();    // This is where we read the byte from the stream...
						idB = idB | (databyteB & 0xFF);        // We append the byte on to ID...

						if (i != IdLength - 1)                // If this is not the last byte, then slide the
						{                                    // previously appended byte to the left by one byte
							idB = idB << 8;                    // to make room for the next byte we append to the ID

						} // if

						bytesread++;                        // Increment the byte count

					} // for


					// Grabbing measurement from File B
					measurementB = 0;
					for (i = 0; i < MeasurementLength; i++) {

						databyteB = inB.readByte();

						measurementB = measurementB | (databyteB & 0xFF);    // We append the byte on to measurement...

						if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
						{                                                // previously appended byte to the left by one byte
							measurementB = measurementB << 8;                // to make room for the next byte we append to the

						} // if

						bytesread++;                        // Increment the byte count

					} // if

				}


					if (measurementA < measurementB) {

						// send id data
						byte[] idBytes = ByteBuffer.allocate(4).putInt(idA).array();
						for(int j = 0; j < idBytes.length; j++) {
							WriteFilterOutputPort(idBytes[j]);
							byteswritten++;
						}

						// send time stamp
						byte[] measBytes = ByteBuffer.allocate(8).putLong(measurementA).array();
						for(int j = 0; j < measBytes.length; j++) {
							WriteFilterOutputPort(measBytes[j]);
							byteswritten++;
						}

						// and additional data in the same frame

						for(int j = 0; j < 60; j++) {

							databyteA = inA.readByte();
							bytesread++;
							WriteFilterOutputPort(databyteA);
							byteswritten++;

						}


						ASmaller = true;
						BSmaller = false;

					}

					else {

						// send id data
						byte[] idBytes = ByteBuffer.allocate(4).putInt(idB).array();
						for(int j = 0; j < idBytes.length; j++) {
							WriteFilterOutputPort(idBytes[j]);
							byteswritten++;
						}

						// send time stamp
						byte[] measBytes = ByteBuffer.allocate(8).putLong(measurementB).array();
						for(int j = 0; j < measBytes.length; j++) {
							WriteFilterOutputPort(measBytes[j]);
							byteswritten++;
						}


						// and additional data in the same frame
						for(int j = 0; j < 60; j++) {

							databyteB = inB.readByte();
							bytesread++;
							WriteFilterOutputPort(databyteB);
							byteswritten++;

						}


						ASmaller = false;
						BSmaller = true;

					}

			} // while

		} //try

		/***********************************************************************************
		 *	The following exception is raised when we hit the end of input file. Once we
		 * 	reach this point, we close the input file, close the filter ports and exit.
		 ***********************************************************************************/

		catch ( EOFException eoferr )
		{
			System.out.println("\n" + this.getName() + "::End of file reached..." );
			try
			{
				ClosePorts();
				System.out.println( "\n" + this.getName() + "::Read file complete, bytes read::" + intRead + " bytes written: " + intWritten );

			}
			/***********************************************************************************
			 *	The following exception is raised should we have a problem closing the file.
			 ***********************************************************************************/
			catch (Exception closeerr)
			{
				System.out.println("\n" + this.getName() + "::Problem closing input data file::" + closeerr);

			} // catch

		} // catch

		/***********************************************************************************
		 *	The following exception is raised should we have a problem openinging the file.
		 ***********************************************************************************/

		catch ( IOException iox )
		{
			System.out.println("\n" + this.getName() + "::Problem reading input data file::" + iox );

		} // catch

	} // run

} // MiddleFilter