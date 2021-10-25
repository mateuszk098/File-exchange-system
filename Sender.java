//Lukasz Kordek, Mateusz Kowalczyk
/*
	Klasa z z matoda wysylajaca zapytanie do innych uzytkownikow sieci czy obsluguja rumbe.
*/

package sk;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class Sender {
	protected InetAddress group;
	protected byte[] buf = new byte[Config.BUFFER_SIZE];
	
	public void multicast(String networkInterfaceName, String Msg) throws IOException{
		/*
		Sposob wyslania pakietu danych do wszystkich uzytkownikow sieci lokalnej 
		jest inspirowany sposobem przedstawionym na stronie:
		http://www.if.pw.edu.pl/~lgraczyk/wiki/index.php/SK_Zadanie_5
		*/
		MulticastSocket mSocket = new MulticastSocket();
		NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
		mSocket.setNetworkInterface(networkInterface);
		group = InetAddress.getByName(Config.MULTICAST_ADDRESS);
		buf = Msg.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, group, Config.MULTICAST_PORT);
		mSocket.send(datagramPacket);
		mSocket.close();
	}
}
