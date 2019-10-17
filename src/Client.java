import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.sun.nio.sctp.SendFailedNotification;

/** Le client doit etre capable de lire un fichier texte et dÕenvoyer son contenu au serveur qui retransmettra aussitot son contenu au client. Ce dernier devra intercepter le contenu du fichier texte. Une fois la rŽception terminŽe, le serveur devra inverser le contenu du fichier de sorte ˆ ce que la premi�re ligne re�ue soit la derni�re ligne envoyŽe vers le client. **/


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		int port = 0;
		String serverAddress = "";
		boolean isPortValid = false;
		boolean isIPAddressValid = false;
		boolean isConnected = false;
		Scanner input = new Scanner(System.in);
		DataInputStream in = null;
		DataOutputStream out = null;
		
		while(true) {
			while (!isPortValid) {
				System.out.println("Entrer un port entre 5000 et 5050:");
				String inputPort = input.nextLine();
				try {
					port = Integer.parseInt(inputPort);
				}
				catch (Exception e) {
					System.out.print(port +" is a invalid port");
					port = 0;
				}
				if (port <= 5050 && port >= 5000) {
						isPortValid = true;
				}
				if (!isPortValid) {
						System.out.println(port +" is a invalid port");
				}
			}
			
			while (!isIPAddressValid) {
				System.out.println("Entrer IP Address");
				String inputAddress = input.nextLine();
					
				if (!checkIPAddressValide(inputAddress)) {
						System.out.println(serverAddress +" is a invalid IP Address");
				}
				else {
					isIPAddressValid = true;
					serverAddress = inputAddress;
				}
			}
				
			// Création d'un socket client vers le serveur. Ici 127.0.0.1 est indicateur que
			// le serveur s'exécute sur la machine locale. Il faut changer 127.0.0.1 pour
			// l'adresse IP du serveur si celui-ci ne s'exécute pas sur la même machine. Le port est 5000.
			if (!isConnected) {
				clientSocket = new Socket(serverAddress, port);
				isConnected = true;	
				System.out.format("The Server is running on %s:%d%n", serverAddress, port);
						
				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());
				String helloMessageFromServer = in.readUTF();
				System.out.println(helloMessageFromServer);
			}
					
			System.out.println("Awaiting command...");
			String inputPort = input.nextLine();
			System.out.println(inputPort+ " scanned");
			String[] command = parseCommand(inputPort);
				
			if (inputPort.contains("upload ")) {
				if (command.length > 1) {
					File tmpDir = new File(command[1]);
					boolean exists = tmpDir.exists();
					if (exists) {
						out.writeUTF(inputPort);
						System.out.println(inputPort+ " sent");
							
						out.writeLong(getSizeByte(tmpDir));
							
						sendFile(clientSocket, command[1], out);
						System.out.println("File send");
							
						String serverMessage = in.readUTF();
						System.out.println(serverMessage);
					}
						else {
							System.out.println("File does not exists");
						}
					}
				}
			else if (inputPort.contains("download ")) {
				if (command.length > 1) {
					out.writeUTF(inputPort);
					String serverMessage = in.readUTF();
					if (serverMessage.equals("OK")) {
						long fileSize = in.readLong();
						System.out.println(inputPort+ " sent");
						saveFile(clientSocket, in, command[1], fileSize);
							
						out.writeUTF("OK");
							
						System.out.println("Awaiting Message");
						if(in.available() > 0) {
							serverMessage = in.readUTF();
						}
						else { serverMessage = "No message"; }
						System.out.println(serverMessage);
						}
					else {
						System.out.println("File does not exists on server");
					}
				}
			}
			else if (!inputPort.contains("exit")) { //fix
				out.writeUTF(inputPort);
				System.out.println(inputPort+ " sent");
					
				String serverMessage = in.readUTF();
				System.out.println(serverMessage);
					
			}
			else if (inputPort.contains("exit")) {
				out.writeUTF(inputPort);
				System.out.println(inputPort+ " sent");
					
				String serverMessage = in.readUTF();
				System.out.println(serverMessage);
				isConnected = false;
				isPortValid = false;
				isIPAddressValid = false;
				clientSocket = null;
			}
				
			System.out.println("client reach end of current loop");
		}
	}

	// Fonction permettant de lire un fichier et de stocker son contenu dans une liste.
//	private static List<String> readFile(String nomFichier) throws IOException {
//		List<String> listOfLines = new ArrayList<String>();
//		String line = null;
//		FileReader fileReader = null;
//		BufferedReader bufferedReader = null;
//		try {
//			fileReader = new FileReader(nomFichier);
//
//			bufferedReader = new BufferedReader(fileReader);
//
//			while ((line = bufferedReader.readLine()) != null) {
//				listOfLines.add(line);
//			}
//		} finally {
//			fileReader.close();
//			bufferedReader.close();
//		}
//		return listOfLines;
//	}

//	// Fonction permettant d'écrire dans un fichier les données contenues dans le stack reçu du serveur.
//	private static void writeToFile(Stack<String> myStack, String nomFichier) throws IOException {
//		BufferedWriter out = null;
//		try {
//			out = new BufferedWriter(new FileWriter(nomFichier));
//			while (!myStack.isEmpty()) {
//				out.write(myStack.pop() + "\n");
//			}
//		} finally {
//			out.close();
//		}
//	}
	
	public static boolean checkIPAddressValide (String IPAddress) {
	    try {
	    	//null check
	        if ( IPAddress == null || IPAddress.isEmpty() ) {
	            return false;
	        }
	        //address format is always the following x.x.x.x where x can be a value between 0-255
	        String[] subaddress = IPAddress.split( "\\." );
	        if ( subaddress.length != 4 ) {
	            return false;
	        }
	        //check if x is between 0-255 for each section
	        for ( String s : subaddress ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        //must finish with a number
	        if ( IPAddress.endsWith(".") ) {
	            return false;
	        }
	        return true;
	    } catch (Exception e) {
	    	// if any exception happens, IP is not valid
	        return false;
	    }
	}
	
	public static void sendFile(Socket clientSocket ,String file, DataOutputStream dos) {
		try {
			//DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			while (fis.read(buffer) > 0) {
				dos.write(buffer);
			}
			fis.close();
			//dos.close();
		}
		catch (Exception e) {
			System.out.println("Exception SocketIO");
		}		
	}
	
	public static void saveFile(Socket clientSock, DataInputStream dis, String fileName, long fileSize) {
		try {
			//DataInputStream dis = new DataInputStream(clientSock.getInputStream());
			FileOutputStream fos = new FileOutputStream(fileName);
			byte[] buffer = new byte[4096];
			
			int filesize = (int) fileSize; // Send file size in separate msg
			int read = 0;
			int totalRead = 0;
			int remaining = filesize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				remaining -= read;
				System.out.println("read " + totalRead + " bytes.");
				fos.write(buffer, 0, read);
			}
			System.out.println("done saving");
			fos.close();
			//dis.close();
			//dis = new DataInputStream(clientSock.getInputStream());
		}
		catch (Exception e) {
			System.out.println("Exception SocketIO");
		}
	}
	
	public static String[] parseCommand(String command) {
		try {
			String[] splitedCommand = command.split("\\s+");
			return splitedCommand;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static long getSizeByte(File file) {
		return file.length();
	}

}
