import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

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
		FileInputStream inA;
		FileInputStream inB;
		SequenceInputStream ss;
		int data = 0;					// The byte of data read from the file

		try
		{
			/***********************************************************************************
			 *	Here we open the file and write a message to the terminal.
			 ***********************************************************************************/

			inA = new FileInputStream(fileNameA);
			inB = new FileInputStream(fileNameB);
			ss = new SequenceInputStream(inA, inB);
			System.out.println("\n" + this.getName() + "::Source reading file..." );

			/***********************************************************************************
			 *	Here we read the data from the file and send it out the filter's output port one
			 * 	byte at a time. The loop stops when it encounters an EOFExecption.
			 ***********************************************************************************/

			data = ss.read();
			while(data != -1)
			{
				intRead++;
				WriteFilterOutputPort((byte)data);
				intWritten++;
				data = ss.read();

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