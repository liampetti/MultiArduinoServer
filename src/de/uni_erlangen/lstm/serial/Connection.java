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

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the connection on the serial ports
 * 
 * Allows for multiple connections from different devices, works on Linux and Windows
 * 
 * @author liampetti
 *
 */
public class Connection {
	final Logger logger = LoggerFactory.getLogger(Connection.class);
	
	private boolean connected;
	private boolean linux;
	private String curr_Port;
	private String db;
	
	private Send sender;
	
	private SerialPort serialPort;
	
	private static final String PORT_NAMES[] = { 
			"/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyACM3", "/dev/ttyACM4", "/dev/ttyACM5", "/dev/ttyACM6",	// Linux
			"/dev/ttyS0",	"/dev/ttyS1", // Linux
			"COM3", "COM4","COM5","COM6","COM7","COM8", "COM9", "COM10" // Windows
	};
	
	private BufferedReader input;
	private OutputStream output;
	
	public Connection(boolean linux) {
		this.linux = linux;
	}

	/**
	 * Initialise a connection with a device
	 * 
	 * @param time_out		Time out
	 * @param data_rate		Data rate
	 * @param com_port		Com port
	 * @param db			Device name for saving to database
	 * @return
	 */
	public Connection initialise(int time_out, int data_rate, String com_port, String db) {
		connected = false;
		this.db = db;
		
		// Required to access serial ports in Linux
		if (linux) {
			System.setProperty("gnu.io.rxtx.SerialPorts", 
					"/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3:/dev/ttyACM4:/dev/ttyACM5:/dev/ttyACM6:/dev/ttyS0:/dev/ttyS1");
		}
		
		
		CommPortIdentifier portId = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();

		System.out.println("===================");
		if (!com_port.equals("SEARCH")) {
			System.out.println("Attempting to connect to "+com_port);
		}
		// Search through PORT_NAMES for possible connections
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			System.out.println("Device found on port "+currPortId.getName());
			if (com_port.equals("SEARCH")) {
				// If not pre-defined then search all ports and take the first one found
				for (String portName : PORT_NAMES) {
					if (currPortId.getName().equals(portName)) {
						portId = currPortId;
						curr_Port = portName;
						System.out.println("Connected to "+curr_Port);
						logger.info("Search and connected to "+curr_Port);
						break;
					}
				}
			} else {
				// Only allow for the pre-defined port to be selected
				if (currPortId.getName().equals(com_port)) {
					portId = currPortId;
					curr_Port = com_port;
					System.out.println("Connected to "+curr_Port+" as "+db);
					logger.info("Connected to "+curr_Port+" as "+db);
					break;
				} 
			}
		}
		if (portId == null) {
			System.out.println("\nCould not find COM port.");	
		} else {
			try {
				// Open serial port, and use class name for the appName.
				serialPort = (SerialPort) portId.open(this.getClass().getName(),
						time_out);
	
				// Set port parameters
				serialPort.setSerialPortParams(data_rate,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
	
				// Open the streams
				input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				output = serialPort.getOutputStream();
				connected = true;
			} catch (Exception e) {
				logger.error("Error opening serial port.", e);
			}
		}
		System.out.println("===================");
		return this;
	}

	public synchronized void close() {
		// Used to prevent port locking on Linux
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	public BufferedReader getInput() {
		return input;
	}
	
	public OutputStream getOutput() {
		return output;
	}
	
	public SerialPort getPort() {
		return serialPort;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public String portName() {
		return curr_Port;
	}
	
	public void setSender(Send sender) {
		this.sender = sender;
	}
	
	public Send getSender() {
		return sender;
	}
	
	public String getDB() {
		return db;
	}
}
