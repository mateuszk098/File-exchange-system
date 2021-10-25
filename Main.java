//Lukasz Kordek, Mateusz Kowalczyk
/*
	Klasa glowna z metoda main.
	Zawiera obiekt klasy Data.
	W mainie na poczatku program karze wybrac network i folder, w ktorym
	beda zapisywane pobrane przez nas pliki.
	Potem jest watek nasluchujacy czy sa obecnie inni uzytkownicy.
	Nastepnie znajduje sie petla while, w ktorej jest menu switch-case.
	Na koncu sa funkcje pomocnicze zwracajce sciezke do pliku/folderu wybranego przez uzytkownika 
	za pomoca JFileChoosera, a takze wysylajace wiadomosci i zadania do innych uzytkownikow.
*/

package sk;
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import javax.swing.JFileChooser;

public class Main {
	//stworzenia obiektu Data
	public static Data data = new Data();
	
	public static void main(String[] args) throws Exception {

		//pytanie o wybor networka
		System.out.println("Dostepne networkInterface:");
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		for(NetworkInterface networkInterface: Collections.list(networkInterfaces)) {
			System.out.println(networkInterface.getName());
		}
		System.out.println("Wybierz interface sieciowy");
		String networkInterfaceName;
		Scanner input = new Scanner(System.in);
		networkInterfaceName = input.next();

		//stworzenie i uruchomienie watkow nasluchujacych i odpowiadajacych i realizujacych zadania uzytkownikow 
		ServerSocket serverSocket = new ServerSocket(0);
		data.serverSocket = serverSocket;
		Receiver receiver = new Receiver();
		receiver.start();
		ReplyClass multiThreaded = new ReplyClass();
		multiThreaded.start();
		Sender sender = new Sender();
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					try {
						data.ipAddresses.clear();
						data.ports.clear();
						sender.multicast(networkInterfaceName, "DoUEvenRumba?"+Integer.toString(data.serverSocket.getLocalPort()));
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
				
			}
		});
		t.start();
		
		//wybor folderu, gdzie beda zapisywane pobane pliki
		System.out.println("Podaj folder zapisu pobieranych plikow.");
		data.SaveFolder = pickFolder();
		String selection = null;
		while(true) {
			selection = input.next();
			//swich-case z wyborem opcji przez uzytkownika
			switch(selection){
				case "exit":
					System.exit(0);

				//pokazani listy uzytkownikow korzystajacych z rumba
				case "showusers":
					if(!data.ipAddresses.isEmpty()) {
						String allUsers = "Lista wszystkich uzytkownikow rumba:";
						for(int index = 0; index < data.ipAddresses.size(); index++) {
							allUsers = allUsers +  "\nUzytkownik nr " + Integer.toString(index) + " ip: " +data.ipAddresses.get(index) + " port: " + Integer.toString(data.ports.get(index));
						}
					System.out.println(allUsers);
					}else {
						System.out.println("Nie ma na liscie uzytkownikow obslugujacych rumba.");
					}
					break;
				
				//pokazuje uzytkownikow a potem liste plikow udostepnianych przez wybranego uzytkownika
				case "showlist":
					if(!data.ipAddresses.isEmpty()) {
						String allUsers = "Lista uzytkownikow rumba:";
						for(int i = 0; i < data.ipAddresses.size(); i++) {
							allUsers = allUsers +  "\nUzytkownik nr " + Integer.toString(i) + " ip: " +data.ipAddresses.get(i) + " port: " + Integer.toString(data.ports.get(i));
						}
						System.out.println(allUsers+"\nPodaj nr uzytkownika od ktorego chcesz uzyskac liste udostepnianych plikow.");
						int index = Integer.parseInt(input.next());
						String listaPlikow = getList(InetAddress.getByName(data.ipAddresses.get(index)), data.ports.get(index));
						System.out.println("\n"+listaPlikow);
					}else {
						System.out.println("Nie mam na liscie nikogo korzystajacego z rumba.");
					}
					break;

				//pobranie pliku
				//pokazuje uzytkownikow a potem liste sciezek do plikow udostepnianych przez innego wybranego uzytkownika
				//po wpisaniu sciezki do pliku uzytkownik go pobiera
				case "download":
					if(!data.ipAddresses.isEmpty()) {
						String allUsers = "Lista uzytkownikow rumba:";
						for(int i = 0; i < data.ipAddresses.size(); i++) {
							allUsers = allUsers +  "\nUzytkownik nr " + Integer.toString(i) + " ip: " +data.ipAddresses.get(i) + " port: " + Integer.toString(data.ports.get(i));
						}
					System.out.println(allUsers+"\nPodaj nr uzytkownika od ktorego chcesz pobrac plik.");
					int index = Integer.parseInt(input.next());
					String listaPlikow = getList(InetAddress.getByName(data.ipAddresses.get(index)), data.ports.get(index));
					System.out.println("\n"+listaPlikow+"\nWpisz sciezke do pliku, ktory chcesz pobrac:");
					String sciezkaDoPliku = input.next();

					//funkcja pobierajaca plik
					getFile(InetAddress.getByName(data.ipAddresses.get(index)), data.ports.get(index), sciezkaDoPliku, data.SaveFolder);
					}else {
						System.out.println("Nie mam na liscie nikogo korzystajacego z rumba.");
					}
					break;

				//pokazuje pliki udostepniane przez uzytkownika
				case "showmylist":
					String sharedFiles = "Udostepniasz pliki:\n";
					ArrayList <String> allSharedFiles = data.getAllShareableFiles();
					for(int index = 0; index < allSharedFiles.size(); index++) {
						sharedFiles = sharedFiles + "\nPlik nr " + index +" \""+ allSharedFiles.get(index)+"\"";
					}
					System.out.println(sharedFiles);
					break;

				//dodaje plik do listy udostepnianych przeze mnie plikow
				case "addfile":
					data.files.add(pickFile());
					break;
				
				//dodaje folder do listy udostepnianych przeze mnie plikow
				case "addfolder":
					data.folders.add(pickFolder());
					break;

				default:
					System.out.println("unknown command");
			}
		}
		
	}
	
	//wysyla zadanie do innego uzytkownika o pobranie pliku i go pobiera
	public static void getFile(InetAddress inetAddress, int port, String path, String savefolder) throws Exception{
		Socket socket = new Socket(inetAddress, port);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		bw.write("GiveMeAFile"+path+"\n");
		bw.flush();
		String line = br.readLine();
		if(line.contains("IAmSendingTheFile")) {
			/*
			Sposob uzyty do pobrania pliku jest wzorowany na sposobie przedstawionym na 
			filmie: https://www.youtube.com/watch?v=WeaB8pAGlDw
			 */
			byte[] b = new byte[Config.BUFFER_SIZE];
			File file = new File(path);
			String fileName = file.getName();
			
			FileOutputStream fos = new FileOutputStream(savefolder+"/"+fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = socket.getInputStream();
			//pobranie pliku
			is.read(b, 0, b.length);
        	fos.write(b, 0, b.length);
			bos.flush();
			System.out.println("Pobrano plik");
		}else if(line.contains("ThisFileIsNotShareable")) {
			System.out.println("Nie mozna pobrac pliku.");
		}
		socket.close();
	}

	//wysyla zadanie do innego uzytkownika o pobranie listy udostepnianych przez niego plikow
	public static String getList(InetAddress inetAddress, int port) throws Exception{
		Socket socket = new Socket(inetAddress, port);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bw.write("GiveMeAListOfShareableFiles\n");
		bw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line = br.readLine();
		String listOfFiles = "";
		int index = 0;
		if(!line.contains("IHaveNoFilesToShare")) {
			while(!line.contains("EndOfList")) {
				listOfFiles = listOfFiles + "\nPlik nr " + index + " \"" + line+ "\"";
				line = br.readLine();
				index++;
			}
			socket.close();
		}else{
			listOfFiles = "Nie ma plikow do udostepnienia";
		}
		return listOfFiles;
	}

	//wyslanie wiadomosci do uzytkonika
	//wykorzystywana funkcja przy wysylaniu zapytania kto obsluguje protokol rumba
	public static void sendMsg(InetAddress inetAddress, int port, String message) throws IOException {
		Socket socket = new Socket(inetAddress, port);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bw.write(message+"\n");
		bw.flush();
		socket.close();
	}
	
	//funkcja zwraca sciezke do pliku
		public static String pickFile() {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.showOpenDialog(null);
			return(chooser.getSelectedFile().getAbsolutePath());
		}
		//funkcja zwraca sciezke do folderu
		public static String pickFolder() {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.showOpenDialog(null);
			return(chooser.getSelectedFile().getAbsolutePath());
		}

}


