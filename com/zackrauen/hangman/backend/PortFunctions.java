package com.zackrauen.hangman.backend;
import gnu.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PortFunctions {
	
	public static void listPorts() {
		 Enumeration<?> pList = CommPortIdentifier.getPortIdentifiers();
		    // Process the list.
		    while (pList.hasMoreElements()) {
		    	
		      CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
		      System.out.print("Port " + cpi.getName() + " ");
		      if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		        System.out.println("is a Serial Port: " + cpi);
		      } else if (cpi.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
		        System.out.println("is a Parallel Port: " + cpi);
		      } else {
		        System.out.println("is an Unknown Port: " + cpi);
		      }
		    }
	}
	
	public static List<CommPortIdentifier> listSerialPorts() {
		List<CommPortIdentifier> serialList = new ArrayList<CommPortIdentifier>();
		 Enumeration<?> pList = CommPortIdentifier.getPortIdentifiers();
		    // Process the list.
		    while (pList.hasMoreElements()) {
		      CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
		      if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		    	serialList.add(cpi);
		      }
		    }
		    return serialList;
	}

	public static void connectSerialPort(SerialPort port) {
		try {
			port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Boolean isPortInUse(String portName) {
        CommPortIdentifier portIdentifier = null;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if ( portIdentifier.isCurrentlyOwned() ) {
        	return true;
        }
        else {
        	return false;
        }
	}
	
	public static SerialPort getSerialPort(String portName) {
		try {
			return (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(portName,2000);
		} catch (PortInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void connectSerialPort(SerialPort port,int baudRate,int dataSize, int stopBits, int parityBit) {
		try {
			port.setSerialPortParams(baudRate, dataSize, stopBits, parityBit);
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}





