import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + "at" + socket);
		}
		
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Hello from server - you are client#" + clientNumber);
				
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
	}
}

