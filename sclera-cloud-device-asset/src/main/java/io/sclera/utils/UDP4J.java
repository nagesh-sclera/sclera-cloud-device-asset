package io.sclera.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Lightweight UDP helper that binds a datagram socket to a source port and sends data to a fixed
 * destination IP and port.
 */
public class UDP4J {
	final private int srcPort;
	final private int dstPort;
	final private String dstIP;
	final private DatagramSocket ds;

	/**
	 * Creates a UDP sender bound to the given source port and targeting the given destination IP and port.
	 */
	public UDP4J(int srcPort, int dstPort, String dstIP) throws SocketException {
		super();
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.dstIP = dstIP;
		ds = new DatagramSocket(this.srcPort);
		
	}
	/**
	 * Sends the given string as a UDP datagram to the configured destination.
	 */
	public void send(String data) throws IOException {
		DatagramPacket DpSend = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(dstIP), dstPort);
		ds.send(DpSend);
	}
	/**
	 * Sends the given byte array as a UDP datagram to the configured destination.
	 */
	public void send(byte data[]) throws IOException {
		DatagramPacket DpSend = new DatagramPacket(data, data.length, InetAddress.getByName(dstIP), dstPort);
		ds.send(DpSend);
		
	}
	public DatagramSocket getDs() {
		return ds;
	}
	
}
