import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WildPitchPressureFilter extends FilterFramework{

    public void run() {

        int bytesread = 0;					// Number of bytes read from the input file.
        int byteswritten = 0;				// Number of bytes written to the stream.
        byte databyte = 0;					// The byte of data read from the file

        int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
        long measurement;
        double data;
        double prevPitch = 0;
        double prevPress = 0;
        double prevPrevPitch = 0;
        double prevPrevPress = 0;
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

                fw = new FileWriter("./WildPitchPressurePoint.csv", true);

                // set up this to grab id = 2
                id = 0;
                byte[] byteArr = new byte[IdLength];
                for (i=0; i<IdLength; i++ )
                {
                    databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...
                    byteArr[i] = databyte;
                    id = id | (databyte & 0xFF);		// We append the byte on to ID...

                    if (i != IdLength-1)				// If this is not the last byte, then slide the
                    {									// previously appended byte to the left by one byte
                        id = id << 8;					// to make room for the next byte we append to the ID

                    } // if

                    bytesread++;						// Increment the byte count

                } // for

                // pass the stored byte values to the next filter
                for(int j = 0; j < byteArr.length; j++) {
                    WriteFilterOutputPort(byteArr[j]);
                }


                measurement = 0;
                byte[] byteArr2 = new byte[MeasurementLength];
                for (i=0; i<MeasurementLength; i++ )
                {
                    databyte = ReadFilterInputPort();
                    byteArr2[i] = databyte;

                    bytesread++;									// Increment the byte count

                } // if


                wildJump = false; // default this value to false;
                data = 0; // reset data variable

                if (id == 3)   // this will be pressure measurement
                {

                    data = ByteBuffer.wrap(byteArr2).getDouble();
                    System.out.println("Pressure = " + data);

                    // and compare with prev data
                    if(data < 45 || data > 90) {

                        System.out.println("Wild jump data: " + data);
                        fw.write(data + ",\n");
                        fw.flush();

                        wildJump = true;
                    }

                    // pass down to store the values as prevPrev and prev
                    prevPrevPress = prevPress;
                    prevPress = data;

                    // if there is a wildJump, then need to modify output data
                    if(wildJump) {

                        // get the average of the previous 2 altitudes
                        // if prePrev == 0, which means it's the 2nd frame

                        if(prevPrevPress == 0) {

                            // then set data to the prev data
                            data = prevPress;
                        }
                        else {

                            // else take the average of the previous 2 altitudes
                            data = (prevPrevPress + prevPress) / 2.00000;
                        }

                        // convert data back to byte array
                        data = (-1) * data;

                    }

                    byteArr2 = ByteBuffer.allocate(8).putDouble(data).array();

                }

                for(int j = 0; j < byteArr2.length; j++) {
                    WriteFilterOutputPort(byteArr2[j]);
                }

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


    }
}
