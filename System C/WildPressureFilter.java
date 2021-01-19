import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

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


public class WildPressureFilter extends FilterFramework{

    public void run() {

        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("YYYY:DD:HH:MM:SS");

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

        System.out.print( "\n" + this.getName() + "::WilPressureFilter Reading ");

        try {

            int countingWild = 0;
            int countingInWrite = 0;

            fw = new FileWriter("./WildPressureData.csv", true);

            fw.write("Time,");
            fw.write("Velocity,");
            fw.write("Altitude,");
            fw.write("Pressure,");
            fw.write("Temperature,");
            fw.write("Pitch, \n");

            String wildPointsData = "";
            wildJump = false; // default this value to false;


            while (true) {
                /*************************************************************
                 *	Here we read a byte and write a byte
                 *************************************************************/


                try {

                    // set up this to grab id = 2
                    id = 0;
                    byte[] byteArr = new byte[IdLength];
                    for (i = 0; i < IdLength; i++) {
                        databyte = ReadFilterInputPort();    // This is where we read the byte from the stream...
                        byteArr[i] = databyte;
                        id = id | (databyte & 0xFF);        // We append the byte on to ID...

                        if (i != IdLength - 1)                // If this is not the last byte, then slide the
                        {                                    // previously appended byte to the left by one byte
                            id = id << 8;                    // to make room for the next byte we append to the ID

                        } // if

                        bytesread++;                        // Increment the byte count

                    } // for


                    // set up to grab Altitude measurement
                    measurement = 0;
                    byte[] byteArr2 = new byte[MeasurementLength];
                    for (i = 0; i < MeasurementLength; i++) {
                        databyte = ReadFilterInputPort();
                        byteArr2[i] = databyte;

                        measurement = measurement | (databyte & 0xFF);    // We append the byte on to measurement...

                        if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
                        {                                                // previously appended byte to the left by one byte
                            measurement = measurement << 8;                // to make room for the next byte we append to the
                            // measurement
                        } // if

                        bytesread++;                        // Increment the byte count

                    } // if


                    if (id == 0) // time
                    {
                        String value = TimeStampFormat.format(measurement) + ",";
//                        System.out.println("Time : " + value);
                        wildPointsData += value;

                    } // if


                    if (id == 1)   // this will be velocity measurement
                    {
                        String value = Double.longBitsToDouble(measurement) + ",";
//                        System.out.println("Velocity : " + value);

                        wildPointsData += value;
                    }


                    if (id == 2)   // this will be altitude measurement
                    {

                        String value = Double.longBitsToDouble(measurement) + ",";
//                        System.out.println("Altitude : " + value);

                        wildPointsData += value;

                    }


                    if (id == 3)   // this will be Pressure measurement
                    {

                        data = Double.longBitsToDouble(ByteBuffer.wrap(byteArr2).getLong());
//                        System.out.println("Pressure : " + data + ",");
                        String value = data + ",";
                        wildPointsData += value;

                        if (data < 45 || data > 90) {

                            // change the wildJump value to true, so it will notify the printer to print
                            wildJump = true;
                            countingWild++;
                        }

                        // if there is a wildJump, then need to modify output data
                        if (wildJump) {

                            // get the average of the previous 2 altitudes
                            // if prePrev == 0, which means it's the 2nd frame

                            // second frame
                            if (prevPrev == 0 && prev != 0) {

                                // then set data to the prev data
                                data = prev;
                            }

                            // all the following frames
                            else if (prevPrev != 0 && prev != 0) {

                                // else take the average of the previous 2 altitudes
                                data = (prevPrev + prev) / 2.00000;
                            }


                            // convert data back to byte array
                            data = (-1) * data;

                        }

                        // pass down to store the values as prevPrev and prev
                        prevPrev = prev;
                        prev = Math.abs(data);

                        byteArr2 = ByteBuffer.allocate(8).putLong(Double.doubleToLongBits(data)).array();

                    }


                    if (id == 4)   // this will be Temperature measurement
                    {
                        String value = Double.longBitsToDouble(measurement) + ",";
//                        System.out.println("Temperature : " + value);

                        wildPointsData += value;

                    }

                    if (id == 5)    // this will be pitch measurement
                    {
                        String value = Double.longBitsToDouble(measurement) + ",\n";
//                        System.out.println("Pitch : " + value);

                        wildPointsData += value;

                        if(wildJump) {

//                            System.out.println(" :::::::::::::::           ::::::::::::::::");
//                            System.out.println("WILD POINT DATA = " + wildPointsData);
//                            System.out.println(" :::::::::::::::           ::::::::::::::::");

                            fw.write(wildPointsData);
                            fw.flush();
                            countingInWrite++;

                        }

                        wildPointsData = "";
                        wildJump = false;

                    }


                    for (int j = 0; j < byteArr.length; j++) {

                        WriteFilterOutputPort(byteArr[j]);
                        byteswritten++;

                    }


                    for (int j = 0; j < byteArr2.length; j++) {

                        WriteFilterOutputPort(byteArr2[j]);
                        byteswritten++;

                    }

                } // try

                catch (EndOfStreamException e) {
                    ClosePorts();
                    System.out.print("\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten);
                    break;

                } // catch

                catch (IOException e) {
                    ClosePorts();
                    e.printStackTrace();
                    System.out.println("\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten);
                    break;
                }

            } // while

            System.out.println(" :::::::::::::::           ::::::::::::::::");
            System.out.println("Counting wild = " + countingWild);
            System.out.println(" :::::::::::::::           ::::::::::::::::");

            System.out.println(" :::::::::::::::           ::::::::::::::::");
            System.out.println("Counting in Write = " + countingInWrite);
            System.out.println(" :::::::::::::::           ::::::::::::::::");


        } // try

        catch (Exception e) {

            e.printStackTrace();

        } // catch

    }
}
