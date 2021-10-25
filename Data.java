//Lukasz Kordek, Mateusz Kowalczyk
/*
	Klasa odpowiada za przechowywanie uzytkownikow obslugujacych protokol rumba, a takze
	za sciezki do plikow i folderow, ktore uzytkownik udostepnia. 
*/

package sk;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class Data {
	public ArrayList<String> files = new ArrayList<String>();
	public ArrayList<String> folders = new ArrayList<String>();
	public ArrayList<String> ipAddresses = new ArrayList<String>();
	public ArrayList<Integer> ports = new ArrayList<Integer>();
	
	public ServerSocket serverSocket;
	String SaveFolder = null;
	
	//funkcja zwraca IP
	public String getMyIp() throws Exception {
		try(final DatagramSocket socket = new DatagramSocket()){
			socket.connect(InetAddress.getByName("8.8.8.8"),10002);
			return socket.getLocalAddress().getHostAddress();
		}
	}

	//funkcja zwraca liste udostepnianych przez uzytkownika plikow
	public ArrayList <String> getAllShareableFiles() throws IOException{
		ArrayList<String> allFiles = new ArrayList<String>();
		ArrayList<Path> paths = new ArrayList<Path>();
		for(int i = 0; i < folders.size(); i++) {
			paths.add(Paths.get(folders.get(i)));
			while(!paths.isEmpty()) {
				Path path = paths.remove(0);
				if(Files.isDirectory(path)) {
					paths.addAll(Files.list(path).collect(Collectors.toList()));
				}else {
					allFiles.add(path.toFile().getAbsolutePath());
				}
			}
		}
		allFiles.addAll(files);
		return allFiles;
	}

}
