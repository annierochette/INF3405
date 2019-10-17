import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Server {
	private static ServerSocket listener;
	
	public static  void main(String[] args) throws Exception {
		int portNumber = 0;
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
				String currentDirectory = System.getProperty("user.dir");
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				out.writeUTF("Hello from server - you are client #" + clientNumber);
				boolean connected = true;
				while (connected) {
					System.out.println("reading UTF");
					String clientMessage = in.readUTF();
					System.out.println("UTF Read");
					formatClientMessage(socket, clientMessage);
					
					if (validCommand(clientMessage)) {
						String[] command = parseCommand(clientMessage);
						if (command[0].equals(validKeywords[0])) {
							//Commande cd
							if (command.length > 1) {
								currentDirectory = changeDirectory(currentDirectory, command[1]);
							 	out.writeUTF("Vous etes dans le dossier " + command[1] +".");
							}
							else {
						
								out.writeUTF("nom non trouver");
							}
						}
						else if (command[0].equals(validKeywords[1])) {
							//Commande cd ..
							currentDirectory = changeDirectory(currentDirectory, "..");
							out.writeUTF("200");
						}
						else if (command[0].equals(validKeywords[2])) {
							//Commande ls
							listeDirectoryContent(currentDirectory, out);
						}
						else if (command[0].equals(validKeywords[3])) {
							//Commande mkdir
							if (command.length > 1) {
								createDirectory(currentDirectory, command[1]);
								out.writeUTF("Le dossier " + command[1] + " a ete cree.");
							}
							else {
								out.writeUTF("nom non trouver");
							}
						}
						else if (command[0].equals(validKeywords[4])) {
							//Commande upload
							if (command.length > 1) {
								long size = in.readLong();
								
								System.out.println("In");
								upload(socket, in, currentDirectory, command[1], size);
								System.out.println("In passed");
								out.writeUTF("Le fichier " + command[1] + " a bien ete televerse");
								System.out.println("Message send");
							}
							else {
								out.writeUTF("nom non trouver");
							}
						}
						else if (command[0].equals(validKeywords[5])) {
							//Commande download
							if (command.length > 1) {
								File tmpDir = new File(currentDirectory +"\\"+command[1]);
								boolean exists = tmpDir.exists();
								if (exists) {
									out.writeUTF("OK");
									out.writeLong(getSizeByte(tmpDir));
									sendFile(socket, command[1], out, currentDirectory);
									
									String clientDSMessage = in.readUTF();
									
									System.out.println("Sending Message");
									out.writeUTF("Le fichier " + command[1] + " a bien ete telecharger");
									System.out.println("Message Sent");
								}
								else {
									System.out.println("nom non trouver");
									out.writeUTF("nom non trouver");
								}
							}
							else {
								out.writeUTF("nom non trouver");
							}
						}
						else if (command[0].equals(validKeywords[6])) {
							//Commande exit
							out.writeUTF("Vous avez ete deconnecte avec succes");
							exit(socket, in, out);
							connected = false;
						}
						else {
							out.writeUTF("Command not found");
						}
					}
					else {
						out.writeUTF("Invalid command");
					}
				}
				
			} catch(IOException e) {
				System.out.println("Error handling client #" + clientNumber + " : " + e);
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
		
		public static String changeDirectory(String currentDir, String target) {
			String newDir = "";
			if (target.equals("..")) {
				System.out.println(previousDirectory(currentDir));//devcode
				return previousDirectory(currentDir);
			}
			if (isDirValid(currentDir, target)) {
				newDir = currentDir +"\\"+target;
				System.out.println(newDir);//devcode
			}
			else {
				//nothing
			}
			return newDir;
		}
		
		public static String previousDirectory(String currentDir) {
			while (currentDir.charAt(currentDir.length()-1) != '\\' && currentDir != null && currentDir.length() > 0) {
				currentDir = currentDir.substring(0, currentDir.length() - 1);
			}
			currentDir = currentDir.substring(0, currentDir.length() - 1);
			return currentDir;
		}
		
		public static void createDirectory(String currentDir, String newDir) {
			new File(currentDir+"\\"+newDir).mkdirs();
		}
		
		public static String listeDirectoryContent(String currentDir, DataOutputStream out) {
			String anwser = "";
			System.out.println(currentDir); //dev code
			File dir = new File(currentDir);
            File childFiles[] = dir.listFiles();
			String childs[] = dir.list();
            for (int i = 0; i < childs.length; i++) {
                if (childFiles[i].isDirectory()) {
                	anwser += ("[Folder] " + childs[i] + "\n");
                }
                else {
                	anwser +=("[File] "+ childs[i] +"\n");
                }
                
            }
            try {
				out.writeUTF(anwser);
			}
            catch (IOException e) {
				e.printStackTrace();
			}
            return anwser;
		}
		
		public static boolean isDirValid(String currentDir, String targetDir) {
			String anwser = "";
			File dir = new File(currentDir);
			String childs[] = dir.list();
            for (String child: childs) {
                if (child.toString().equals(targetDir)) {
                	return true;
                }
            }
            return false;
		}
		
		public static void upload(Socket socket, DataInputStream dis, String currentDir, String fileName, long fileSize) {
			System.out.println("Uploading...");
			saveFile(socket, dis, currentDir, fileName, fileSize);
			System.out.println("Uploaded.");
			
		}
		
//		public static void download() {
//	
//		}
		
		public static void exit(Socket socket, DataInputStream in, DataOutputStream out) {
			try {
				in.close();
				out.close();
				socket.close();
			} 
			catch (IOException e) {
			}
		}
		
		public static String formatClientMessage(Socket socket, String message) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();  
			String builtMessage = "["+(socket.getInetAddress().toString()).substring(1)+":"+socket.getLocalPort()+"-"+dtf.format(now)+"]: "+message;
			System.out.println(builtMessage);
			return builtMessage;
		}
		
		public static void saveFile(Socket clientSock, DataInputStream dis, String currentDir, String fileName, long fileSize) {
			try {
				//DataInputStream dis = new DataInputStream(clientSock.getInputStream());
				FileOutputStream fos = new FileOutputStream(currentDir+"\\"+fileName);
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
				
				fos.close();
				//dis.close();
				//dis = new DataInputStream(clientSock.getInputStream());
			}
			catch (Exception e) {
				System.out.println("Exception SocketIO");
			}
		}
		
		public static void sendFile(Socket clientSocket ,String file, DataOutputStream dos, String currentDirectory) {
			try {
				//DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
				FileInputStream fis = new FileInputStream(currentDirectory + "\\" +file);
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
		
		public static long getSizeByte(File file) {
			return file.length();
		}
	}
}

