/*
 * MultiArduinoServer -- Multiple serial device listener for Arduino projects
 * ===============================================================
 *
 * Copyright 2016 Liam Pettigrew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************************************
 */
package de.uni_erlangen.lstm.serial;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_erlangen.lstm.file.WriteCSV;
import de.uni_erlangen.lstm.main.Main;

/**
 * Receives data from the Arduino and writes to a CSV file
 * 
 * @author liampetti
 *
 */
public class SerialReceive implements SerialPortEventListener {
	final Logger logger = LoggerFactory.getLogger(SerialReceive.class);
	
	public static long LAST_RECEIVE;
	private WriteCSV monitorWriter;
	private WriteCSV outputWriter;
	private String db;
	
	// Serial connection information
	private boolean csv;
	private Connection conn;
	
	private long receivedAt;
	private Timestamp time;
	
	public SerialReceive(boolean linux, Connection conn, boolean csv, String db) {
		this.csv = csv;
		this.conn = conn;
		this.outputWriter = new WriteCSV("output.csv");
		this.receivedAt = System.currentTimeMillis();
		this.time = new Timestamp(receivedAt);
		this.db = db;
		
		// Creates a smaller sized text file for quick data monitoring
		this.monitorWriter = new WriteCSV("monitor_"+
				"_"+conn.getDB()+
				".txt");
	}

	/*
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			receivedAt = System.currentTimeMillis();
			time.setTime(receivedAt);
			String stime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(time);
			String inputLine = "";
			try {					
				inputLine = conn.getInput().readLine();
				SerialReceive.LAST_RECEIVE = receivedAt;
			} catch (Exception e) {
				logger.error("Unable to get input.");			
			}
			
			// Write the received data to the monitor file
			if (inputLine.length() > 0) {
				monitorWriter.writeString(stime + ";" + 
						inputLine, Main.MONITOR_SIZE);
			}

			// Terminal connections with German devices can use commas instead decimal points
			if (db.equals("terminal")) {
				// Replace commas with decimal points
				inputLine = inputLine.replace(",", ".");
			}
			
			// Write received data to csv file
			String[] inputArray = inputLine.split(";");
			String[] data = new String[inputArray.length];
			// Any Arduino output that should be saved to CSV must be proceeded by !!!
			// All terminal device output is automatically written to CSV
			if (inputArray[0].equals("!!!") || db.equals("terminal")) {			
				// Set first entry to the current time			
				data[0] = stime;
				for(int i=1;i<inputArray.length;i++) {
					data[i] = inputArray[i];
				}
				
				if (data.length > 0) {
					if (csv) {
						String csvOut = "";
						for (int i=0;i<data.length;i++) {
							csvOut += data[i]+";";
						}					
						outputWriter.writeToFile("output_"+db+".csv",csvOut, -1);
					}
				}
			} 
		}
	}
}
