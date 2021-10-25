//Lukasz Kordek, Mateusz Kowalczyk
/*
	Klasa bedaca jednoczesnie watkiem ciagle nasluchujacym na pytanie od innych uzytkownikow czy obsluguje rumbe.
	Za kazdym razem jesli dostanie takie pytanie odpowiada, ze obsluguje.
*/
package sk;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class Receiver extends Thread{
	protected MulticastSocket mSocket = null;
	protected byte[] buf = new byte[Config.BUFFER_SIZE];
	
	public void run() {
		try {
			mSocket = new MulticastSocket(Config.MULTICAST_PORT);
			InetAddress group = InetAddress.getByName(Config.MULTICAST_ADDRESS);
			mSocket.joinGroup(group);
			while(true) {
				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
				mSocket.receive(datagramPacket);
				String received = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
				if(received.contains("DoUEvenRumba?")) {
					InetAddress inetAddress = datagramPacket.getAddress();
					Main.sendMsg(inetAddress, Integer.parseInt(received.substring(13)), "IDoRumba"+Integer.toString(Main.data.serverSocket.getLocalPort()) + "xxxIPxxx" + Main.data.getMyIp());
				}
			}
		}catch(IOException e) {
		}catch (NumberFormatException e) {
		}catch (Exception e) {
		}
	}
}
