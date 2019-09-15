import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Server {
	private static ServerSocket listener;
	
	public static  void main(String[] args) throws Exception {
		int portNumber = 5000;
		boolean validPort = false;
		
        while(!validPort) {
        	System.out.print("Enter a port number : ");
        	Scanner scanner = new Scanner(System.in);
        	String inputPort = scanner.nextLine();

        	try {
        		portNumber = Integer.parseInt(inputPort);
        	} catch(Exception e) {
        		System.out.println("Port number must be between 5000 and 5050");
        		portNumber = 0;
        	}
        	if(portNumber >= 5000 && portNumber <= 5050) {
        		validPort = true;
        	}
        	if(portNumber < 5000 || portNumber > 5050) {
        		System.out.println("Port number must be between 5000 and 5050");
        	}
        }
		
		int clientNumber = 0;
		
		String serverAddress = "127.0.0.1";
		int serverPort = portNumber;
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		
		try {
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		finally {
			listener.close();
		}
		
	}
	

	private static class ClientHandler extends Thread {
		private Socket socket;
		private int clientNumber;
		private static String[] validKeywords = {"cd", "cd..", "ls", "mkdir", "upload", "download", "exit"};
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + "at" + socket);
		}
		
		public void run() {
			try {
				String currentDirectory = "";
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				out.writeUTF("Hello from server - you are client#" + clientNumber);
				boolean connected = true;
				while (connected) {
					String clientMessage = in.readUTF();
					System.out.println("Client says: "+clientMessage);
					if (validCommand(clientMessage)) {
						String[] command = parseCommand(clientMessage);
						//listeDirectoryContent();
					}
					else {
						out.writeUTF("Invalid command");
					}
				}
				
			} catch(IOException e) {
				System.out.println("Error handling client#" + clientNumber + ":" + e);
			} finally {
				try {
					socket.close();
				}
				catch(IOException e) {
					System.out.println("Couldn't close a socket, what's going on ?");
					
				}
				System.out.println("Connection with client#" + clientNumber + "closed");
			}
		}
		public static boolean validCommand(String command) {
			try {
				if ( command == null || command.isEmpty() ) {
					return false;
				}
				String[] splitedCommand = command.split("\\s+");
				boolean containValidKeyword = false;
				String keyword;
				for	(int i = 0; i<validKeywords.length; i++) {
					if (validKeywords[i].equals(splitedCommand[0])) {
						containValidKeyword = true;
						keyword = validKeywords[i];
					}
				}
				if (!containValidKeyword) {
					return false;
				}
				
				return true;
			}
			catch (Exception e) {
				return false;
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
		public static void changeDirectory() {
			
		}
		public static String listeDirectoryContent(ObjectOutputStream out) {
			String anwser = "";
			File dir = new File(System.getProperty("user.dir") + "/../tcp");
            String childs[] = dir.list();
            for (String child: childs) {
                if (child.contains(".")) {
                	anwser += ("[File] " + child + "\n");
                }
            }
            try {
				out.writeObject(anwser);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return anwser;
		}
		public static void upload() {
			
		}
		public static void download() {
	
		}
		public static void exit(Socket socket) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		public static String formatClientMessage(String message) {
			//[132.207.29.107:42975 - 2019-09-15@13:02:01] : upload allo.docx
			String builtMessage = "["+" - "+"@"+"] : "+message;
			return "";
		}
	}
}

