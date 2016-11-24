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
package de.uni_erlangen.lstm.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a CSV data file for processing
 * 
 * @author liampetti
 *
 */
public class ReadCSV {
	final Logger logger = LoggerFactory.getLogger(ReadCSV.class);
	
	// File data
	private String filename;
	private BufferedReader br = null;
	private String splitter;
	
	// Default settings
	public ReadCSV() {
		filename = "input.csv";
		splitter = ";";
	}
	
	public ReadCSV(String filename, String splitter) {
		this.filename = filename;
		this.splitter = splitter;
	}
	
	public String[] getStrings() {
		String[] currentList = new String[0];
		String line = "";
		
		// Load our input list from the CSV file (reload)
		try { 
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {	 
				currentList = line.split(splitter); 
			}	 
		} catch (FileNotFoundException e) {
			logger.error("File not found: " + filename, e);
		} catch (IOException e) {
			logger.error("Unable to read file: " + filename, e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Error closing file reader.", e);
				}
			}
		}
		
		return currentList;
	}

}
