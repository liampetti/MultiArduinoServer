# MultiArduinoServer
Multiple serial device listener for Arduino projects.

Based on the Arduino and Java RXTX Sample Code found at http://playground.arduino.cc/Interfacing/Java

Program is designed to be able to be compiled and run from a USB stick on Windows or Linux.

This work has been created at:

Lehrstuhl für Strömungsmechanik,  
Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU)


# Interface and Usage
A command line interface can be used to connect with multiple serial devices, also works with any device that sends output to terminal:

* -linux		  
  * Server is running on Linux
* -csv			
  * Write all device outputs to csv file
* -port "port name" "device name"	
  * Specify a port and name for a given device

For example,  a multiple device listener for one Arduino and a separate terminal device can be run using the command

> 				java -jar ArduinoServer.jar -linux -csv -port COM7 labanalysis_arduino -port COM10 terminal

Commands can be sent to a device using "command_devicename.txt"


# Dependencies
* Requires the RXTX Java Library found at http://rxtx.qbang.org/wiki/index.php/Main_Page


# Credits

The Arduino forums and community at https://www.arduino.cc/


# License
>Copyright 2016 Liam Pettigrew
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at


>			http://www.apache.org/licenses/LICENSE-2.0        


> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and limitations under the License.

