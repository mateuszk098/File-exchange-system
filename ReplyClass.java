//Lukasz Kordek, Mateusz Kowalczyk
/*
	Klasa bedaca jednoczesnie watkiem ciagle nasluchujacym na pytanie od innych uzytkownikow 
	o mozliwosc pobrania jakiegos pliku lub pokazania listy udostepnianych plikow.
	Zawiera metode, ktora jest uruchamiana w watku.

*/

package sk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.Config;

public class ReplyClass extends Thread{
	
	//Watek
	public void run() {
		try {
			Reply();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void Reply() throws IOException{
		ServerSocket serverSocket = Main.data.serverSocket;
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		while(true) {
			final Socket socket = serverSocket.accept();
			Runnable connection = new Runnable() {
					@Override
					public void run() {
						
						try {
							//odczytanie wiadomosci przychodzacej
							BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
							String line = br.readLine();
							
							//dodanie do listy uzytkowniko kozystajcej z rumba, jesli nie jest jeszcze dodany
							if(line.contains("IDoRumba")) {
								Integer givenPort = Integer.parseInt(line.substring(8,line.indexOf("xxxIPxxx")));
								String givenIp = line.substring(line.indexOf("xxxIPxxx")+8);
								
								boolean w = true;
								int n = Main.data.ipAddresses.size();
								for(int i=0;i<n;i++){
									if( givenIp.equals(Main.data.ipAddresses.get(i)) ){
										w=false;
									}
								}
								if(w){
									Main.data.ports.add(givenPort);
									Main.data.ipAddresses.add(givenIp);
								}

							//zadanie pobrania pliku
							//w odpowiedzi jest wyslana wiadomosc o wyslaniu pliku i wyslanie pliku
							}else if(line.contains("GiveMeAFile")) {
								String absoluteFilePath = line.substring(11);
								if(Main.data.getAllShareableFiles().contains(absoluteFilePath)) {
									/*
									Sposob uzyty do wyslanie i pobrania pliku jest wzorowany na sposobie 
									przedstawionym na filmie: https://www.youtube.com/watch?v=WeaB8pAGlDw
			 						*/
									bw.write("IAmSendingTheFile\n");
									bw.flush();
									File file = new File(absoluteFilePath);
									FileInputStream fis= new FileInputStream(file);
									BufferedInputStream bis = new BufferedInputStream(fis);
                                    OutputStream os = socket.getOutputStream();
                                    byte[] b= new byte[Config.BUFFER_SIZE];
                                    fis.read(b, 0, b.length);
                                    os.write(b, 0, b.length);
									socket.close();
								}else {
									bw.write("ThisFileIsNotShareable");
									bw.flush();
									socket.close();
								}
							
							//zadanie wyslania listy udostepnianych plikow
							}else if(line.contains("GiveMeAListOfShareableFiles")) {
								if(Main.data.getAllShareableFiles().isEmpty()) {
									bw.write("IHaveNoFilesToShare");
									bw.flush();
									socket.close();
								}else {
									ArrayList <String> allSharedFiles = Main.data.getAllShareableFiles();
									for(int index = 0; index < allSharedFiles.size(); index++) {
										bw.write(allSharedFiles.get(index)+"\n");
										bw.flush();
									}
									bw.write("EndOfList\n");
									bw.flush();
									socket.close();	
								}
							}
					}catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			executorService.submit(connection);
		}
	}
}

