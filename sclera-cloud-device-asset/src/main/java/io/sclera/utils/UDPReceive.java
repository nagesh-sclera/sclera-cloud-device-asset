package io.sclera.utils;

import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import io.sclera.service.APICallService;
import io.sclera.service.touchscreen.MonitorService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

@Component
public class UDPReceive {

	@Autowired
	MonitorService monitorService;

	@Autowired
	APICallService apiCallService;

	public void monitorInit() {

		try {
			UDP4J udp = new UDP4J(2222, 1111, "127.0.0.1");
			UDPreceive rcv = new UDPreceive(udp.getDs());
			rcv.start();
			System.out.println("UDP SOCKET RUNNING");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
	}

	public class UDPreceive implements Runnable {

		Thread thread;
		DatagramSocket datagramSocket;
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;

		public UDPreceive() {
			super();
			// TODO Auto-generated constructor stub
		}

		public UDPreceive(DatagramSocket ds) {
			datagramSocket = ds;
		}

		public void run() {
			//			int i = 0;
			while (true) {
				DpReceive = new DatagramPacket(receive, receive.length);

				try {
					datagramSocket.receive(DpReceive);
					//					i++;
					//					System.out.println("count"+ i);
					//					System.out.println("Client:-" + data(receive));
					try {
						String jsonResponse = data(receive).toString();
						System.out.println("PRINTT" + jsonResponse);
						if(jsonResponse != null && !jsonResponse.isBlank() && !jsonResponse.isEmpty()) {
							JSONObject json = new JSONObject(jsonResponse);

							if (json.getJSONArray("devices") != null) {
								List<DeviceMonitorDTO> deviceMonitors = apiCallService.getJSONArrayFromJSONString(
										json.getJSONArray("devices").toString(), DeviceMonitorDTO.class);
								if (deviceMonitors != null && json.getString("docker_name") != null) {
									monitorService.deviceUpsertbyId(json.getString("docker_name"), deviceMonitors,"all");
								}
							}
							if (json.getJSONArray("logs") != null) {
								List<DeviceHistoryDTO> devicesHistory = apiCallService.getJSONArrayFromJSONString(
										json.getJSONArray("logs").toString(), DeviceHistoryDTO.class);
								if (devicesHistory != null && json.getString("docker_name") != null) {
									monitorService.insertDevicesHistory(json.getString("docker_name"),
											devicesHistory);
								}
							}
						}

					} catch (Exception e) {
						System.out.println(e);
					}

					


				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(e);
				}

				receive = new byte[65535];
			}

		}

		public StringBuilder data(byte[] a) {
			if (a == null)
				return null;
			StringBuilder ret = new StringBuilder();
			int i = 0;
			while (a[i] != 0) {
				ret.append((char) a[i]);
				i++;
			}
			return ret;
		}

		public void start() {
			System.out.println("Thread started");
			if (thread == null) {
				thread = new Thread(this, "recv");
				thread.start();
			}

		}

	}

}
