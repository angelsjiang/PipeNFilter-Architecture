import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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

		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("YYYY:DD:HH:MM:SS");

		String fileNameA = "SubSetA.dat";	// Input data file.
		String fileNameB = "SubSetB.dat";
		int intRead = 0;					// Number of bytes read from the input file.
		int intWritten = 0;				// Number of bytes written to the stream.
		DataInputStream inA;
		DataInputStream inB;
//		SequenceInputStream ss;

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
		int data = 0;
		int i;

		byte[] byteIdArrA;
//		byte[] byteMeasArrA;
		byte[] byteIdArrB;
//		byte[] byteMeasArrB;

		ArrayList<Long> measurementsA;
		ArrayList<Long> measurementsB;


		try
		{
			/***********************************************************************************
			 *	Here we open the file and write a message to the terminal.
			 ***********************************************************************************/

			inA = new DataInputStream(new FileInputStream(fileNameA));
			inB = new DataInputStream(new FileInputStream(fileNameB));
			System.out.println("\n" + this.getName() + "::Source reading file..." );

			measurementsA = new ArrayList<>();
			measurementsB = new ArrayList<>();

			boolean ASmaller = true;
			boolean BSmaller = true;

			int count = 0;

			/***********************************************************************************
			 *	Here we read the data from the file and send it out the filter's output port one
			 * 	byte at a time. The loop stops when it encounters an EOFExecption.
			 ***********************************************************************************/

			while(true)
			{

//				System.out.println(":::::::::::::::: ::::::::::::::::");

				if(ASmaller) {
					// get data from File A
					idA = 0;
					byteIdArrA = new byte[IdLength];
					for (i = 0; i < IdLength; i++) {

						databyteA = inA.readByte();    // This is where we read the byte from the stream...
						byteIdArrA[i] = databyteA;
						idA = idA | (databyteA & 0xFF);        // We append the byte on to ID...

						if (i != IdLength - 1)                // If this is not the last byte, then slide the
						{                                    // previously appended byte to the left by one byte
							idA = idA << 8;                    // to make room for the next byte we append to the ID

						} // if

						bytesread++;                        // Increment the byte count

					} // for


					// Grabbing measurement from File A
					measurementA = 0;
//				byteMeasArrA = new byte[MeasurementLength];
					for (i = 0; i < MeasurementLength; i++) {

						databyteA = inA.readByte();
//					byteMeasArrA[i] = databyteA;

						measurementA = measurementA | (databyteA & 0xFF);    // We append the byte on to measurement...

						if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
						{                                                // previously appended byte to the left by one byte
							measurementA = measurementA << 8;                // to make room for the next byte we append to the

						} // if

						bytesread++;                        // Increment the byte count

					} // if


				}

//				System.out.println("ID A : " + idA);
//				System.out.println("Measurement A : " + Double.longBitsToDouble(measurementA));


				if(BSmaller) {
					// get data from File B
					idB = 0;
					byteIdArrB = new byte[IdLength];
					for (i = 0; i < IdLength; i++) {

						databyteB = inB.readByte();    // This is where we read the byte from the stream...
						byteIdArrB[i] = databyteB;
						idB = idB | (databyteB & 0xFF);        // We append the byte on to ID...

						if (i != IdLength - 1)                // If this is not the last byte, then slide the
						{                                    // previously appended byte to the left by one byte
							idB = idB << 8;                    // to make room for the next byte we append to the ID

						} // if

						bytesread++;                        // Increment the byte count

					} // for


					// Grabbing measurement from File B
					measurementB = 0;
//				byteMeasArrB = new byte[MeasurementLength];
					for (i = 0; i < MeasurementLength; i++) {

						databyteB = inB.readByte();
//					byteMeasArrB[i] = databyteB;

						measurementB = measurementB | (databyteB & 0xFF);    // We append the byte on to measurement...

						if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
						{                                                // previously appended byte to the left by one byte
							measurementB = measurementB << 8;                // to make room for the next byte we append to the

						} // if

						bytesread++;                        // Increment the byte count

					} // if

				}

//				System.out.println("ID B : " + idB);
//				System.out.println("Measurement B : " + Double.longBitsToDouble(measurementB));



					if (measurementA < measurementB) {

//						System.out.println("SourceMergerFilter in loop A :::::  Got here");

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

						int countSentTimes = 0;
						for(int j = 0; j < 60; j++) {

							databyteA = inA.readByte();
							bytesread++;
							WriteFilterOutputPort(databyteA);
							byteswritten++;
							countSentTimes++;

						}

//						System.out.println("Count sent times: " + countSentTimes);


						ASmaller = true;
						BSmaller = false;

					}

					else {

//						System.out.println("SourceMergerFilter in loop B :::::  Got here");

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
						int countSentTimes = 0;
						for(int j = 0; j < 60; j++) {

							databyteB = inB.readByte();
							bytesread++;
							WriteFilterOutputPort(databyteB);
							byteswritten++;
							countSentTimes++;

						}

//						System.out.println("Count sent times: " + countSentTimes);

						ASmaller = false;
						BSmaller = true;

					}



//
				// ----------  Storing data from both file ---------- //

//				if (idA == 0) // Time A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 0) // Time B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//
//				if (idA == 1) // Velocity A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 1) // Velocity B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//
//				if (idA == 2) // altitude A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 2) // altitude B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//				if (idA == 3) // Pressure A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 3) // Pressure B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//
//				if (idA == 4) // Temperature A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 4) // Temperature B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//
//				if (idA == 5) // Pitch A
//				{
//					measurementsA.add(measurementA);
//				} // if
//
//				if (idB == 5) // Pitch B
//				{
//					measurementsB.add(measurementB);
//				} // if
//
//
//				// -------------- End of Storing data from both files --------------- //
//
//				if(measurementsA.get(0) <= measurementsB.get(0)) {	// comparing timestamp in long
//
//					for(int j = 0; j < byteIdArrA.length; j++) {
//						WriteFilterOutputPort(byteIdArrA[j]);
//						byteswritten++;
//
//					}
//
//					// send data from A first
//					for(int j = 0; j < measurementsA.size(); j++) {
//
//						// convert each measurements from long to byte[] & send it bit by bit
//						byte[] bytes = ByteBuffer.allocate(8).putLong(measurementsA.get(j)).array();
//
//						for(int k = 0; k < bytes.length; k++) {
//
//							WriteFilterOutputPort(bytes[k]);
//							byteswritten++;
//
//						}
//
//					}
//
//				}
//
//				else {
//
//					// else send data B
//					for(int j = 0; j < byteIdArrB.length; j++) {
//						WriteFilterOutputPort(byteIdArrB[j]);
//						byteswritten++;
//
//					}
//
//					// send data from B first
//					for(int j = 0; j < measurementsB.size(); j++) {
//
//						// convert each measurements from long to byte[] & send it bit by bit
//						byte[] bytes = ByteBuffer.allocate(8).putLong(measurementsB.get(j)).array();
//
//						for(int k = 0; k < bytes.length; k++) {
//
//							WriteFilterOutputPort(bytes[k]);
//							byteswritten++;
//
//						}
//
//					}
//
//
//				}


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
//				ss.close();
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