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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes to a CSV data file
 * 
 * @author liampetti
 *
 */
public class WriteCSV {
	final Logger logger = LoggerFactory.getLogger(WriteCSV.class);
	
	private String filename;
	
	public WriteCSV(String filename) {
		this.filename = filename;
	}
	
	public void writeToFile(String filename, String input, double maxSize) {
		this.filename = filename;
		writeString(input, maxSize);
	}
	
	/**
	 * Append string to a file
	 * 
	 * @param input 	The string to append
	 * @param maxSize 	Maximum file size (specified for smaller monitor file), -1 for unlimited
	 */
	public void writeString(String input, double maxSize) {
		try {
			File f = new File(filename);
			boolean append = true;			
			if (f.length() > maxSize && maxSize != -1) {
				append = false;
				logger.info("Monitor file size has reached 1MB, file reset.");
			}
			PrintStream fileStream = new PrintStream(new FileOutputStream(filename, append));	
			fileStream.println(input);			
			fileStream.close();
		} catch (IOException e) {
			logger.error("Unable to write to file: " + filename, e);
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
