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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_erlangen.lstm.main.Main;

/**
 * Sends data to the Arduino
 * 
 * @author liampetti
 *
 */
public class Send implements Runnable {
	final Logger logger = LoggerFactory.getLogger(Send.class);
	
	private Connection conn;
	
	public Send(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * Send data to Arduino by reading line from "command_devicename.txt" if it exists
	 * 
	 * @param addr 	Address
	 * @param i		Value
	 */
	public void serialWrite(int addr, int i) {
		byte[] b = new byte[2];
		b[0] = (byte) addr;
		b[1] = (byte) i;
		try {
			conn.getOutput().write(b);
			System.out.println("Sending: " + (b[1] & 0xFF) + " to: " + b[0]);
		} catch (Exception e) {
			logger.error("Unable to send command on port "+conn.getPort());
		}
	}
	
	@Override
	public void run() {
		File commands;
		commands = new File("command_"+
				conn.getDB()+
				".txt");
		
		// Only allow commands to be sent if the Arduino is open for communication
		if (!Main.ARDUINO_LINK && commands.isFile()) {						
			Main.ARDUINO_LINK = true;
			String line = "";
			BufferedReader br = null;
			// Load command list from command file
			try { 
				br = new BufferedReader(new FileReader(commands));
				while ((line = br.readLine()) != null) {	 
				    // if command exists on this line send it to the Arduino
					String[] curCmd = line.split(";");	
					if (curCmd.length == 2) {
						serialWrite(Integer.parseInt(curCmd[0]), 
								Integer.parseInt(curCmd[1]));
					} else {
						logger.error("Command length is of incorrect size. Command String: " + line);
					}
				}	 
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println(e.toString());
						e.printStackTrace();
					}
				}
			}
			// Delete the commands file once it has been read
			if (!commands.delete()) {
				logger.error("Command file was unable to be removed!");
			}
			Main.ARDUINO_LINK = false;
		} else if (Main.ARDUINO_LINK && commands.isFile()) {
			logger.warn("Communication link is currently in use, waiting before sending commands.");
		}
	}
	
	public String portName() {
		return conn.portName();
	}
}
