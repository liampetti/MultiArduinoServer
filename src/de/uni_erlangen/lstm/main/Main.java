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
package de.uni_erlangen.lstm.main;

import gnu.io.SerialPortEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_erlangen.lstm.serial.Connection;
import de.uni_erlangen.lstm.serial.SerialReceive;
import de.uni_erlangen.lstm.serial.PortProperties;
import de.uni_erlangen.lstm.serial.Send;

/**
 * Main class is the starting point to start all server functions
 * 
 * Arguments:
 * 	-linux	Use this argument if running server on linux
 * 	-csv	Use this argument if data should be recorded to CSV file
 *  -port	Specify a device port and device name (Example: -port COM7 labanalysis_arduino)
 * 
 * @author liampetti
 *
 */
public class Main {	
	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static final int MONITOR_SIZE = 100000; // In bytes (100000 = 100 kb)
	
	public static boolean ARDUINO_LINK; // Safety switch prevents multiple links to Arduino

	/*
	 * Main function initializes logging and starts server
	 */
	public static void main(String[] args) {				
		boolean linux = false;
		boolean csv = false; 
		List<PortProperties> ports = new ArrayList<PortProperties>();
		
		if (args.length > 0) {
			for (int i=0;i<args.length;i++) {
				switch (args[i]) {
					case "-linux":		linux = true;
										break;
					case "-csv":		csv = true;
										break;
					case "-port":		ports.add(new PortProperties(args[i+1], args[i+2]));
										break;
					default:			break;
				}
			}	
		} 
		
		// Set up the connections
		List<Connection> conns = new ArrayList<Connection>();
		// Do a search if no ports specified, no db connection
		if (ports.size() == 0) {
			conns.add(new Connection(linux).initialise(2000, 9600, "SEARCH", "NONE"));
		}
		for (PortProperties port : ports) {			
			if (port.getDbName().equals("terminal")) {
				// Terminal connection for a separate device, has a different baud rate
				conns.add(new Connection(linux).initialise(2000, 38400, port.getPortName(), port.getDbName()));
			} else {
				// Search/Specify port and setup connection to Arduino device (baud = 9600, timout = 2000)
				conns.add(new Connection(linux).initialise(2000, 9600, port.getPortName(), port.getDbName()));
			}
		}
		
		// Add the senders and receivers
		for (Connection conn : conns) {	
			if (conn.isConnected()) {
				SerialReceive arduinoReceiver = new SerialReceive(linux, conn, csv, conn.getDB());
				conn.setSender(new Send(conn));
				try {
					addReceiver(arduinoReceiver, conn);
				} catch (Exception e) {
					logger.error("Unable to initiate receiver for connection on " + conn.getPort(), e);
				}
			}
		}

		ARDUINO_LINK = false;
		
		// Check if a commands list exists, send commands to Arduino
		ScheduledExecutorService commands = Executors.newScheduledThreadPool(conns.size());
		for (Connection conn : conns) {
			if (conn.getSender() != null) {
				// Do a command check every second, delay ten seconds at start
				commands.scheduleAtFixedRate(conn.getSender(), 10, 1, TimeUnit.SECONDS);
			}
		}
				
		if (conns.size() == 0) {
			logger.info("No connections made. Exiting...");
		}
	}
	
	/**
	 * Main function for reading from the serial connection
	 */
	public static void addReceiver(SerialPortEventListener receiver, Connection conn) throws TooManyListenersException {
		// add event listeners
		conn.getPort().addEventListener(receiver);
		conn.getPort().notifyOnDataAvailable(true);
	}
}
