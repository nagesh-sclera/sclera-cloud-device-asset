package io.sclera.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP4J {
	final private int srcPort;
	final private int dstPort;
	final private String dstIP;
	final private DatagramSocket ds;
	
	public UDP4J(int srcPort, int dstPort, String dstIP) throws SocketException {
		super();
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.dstIP = dstIP;
		ds = new DatagramSocket(this.srcPort);
		
	}
	public void send(String data) throws IOException {
		DatagramPacket DpSend = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(dstIP), dstPort);
		ds.send(DpSend);
	}
	public void send(byte data[]) throws IOException {
		DatagramPacket DpSend = new DatagramPacket(data, data.length, InetAddress.getByName(dstIP), dstPort);
		ds.send(DpSend);
		
	}
	public DatagramSocket getDs() {
		return ds;
	}
	
}
