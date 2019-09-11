import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
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

/** Le client doit �tre capable de lire un fichier texte et dÕenvoyer son contenu au serveur qui retransmettra aussit™t son contenu au client. Ce dernier devra intercepter le contenu du fichier texte. Une fois la rŽception terminŽe, le serveur devra inverser le contenu du fichier de sorte ˆ ce que la premi�re ligne re�ue soit la derni�re ligne envoyŽe vers le client. **/


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		int port = 5000;
		boolean isPortValid = false;
		Scanner input = new Scanner(System.in);
		while (!isPortValid) {
			System.out.println("Entrer un port entre 5000 et 5050");
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
		String serverAddress = "127.0.0.1";
		// Création d'un socket client vers le serveur. Ici 127.0.0.1 est indicateur que
		// le serveur s'exécute sur la machine locale. Il faut changer 127.0.0.1 pour
		// l'adresse IP du serveur si celui-ci ne s'exécute pas sur la même machine. Le port est 5000.
		clientSocket = new Socket(serverAddress, port);
			
		System.out.format("The Server is running on $s:$d$n", serverAddress, port);
			
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
			
		clientSocket.close();
	}

	// Fonction permettant de lire un fichier et de stocker son contenu dans une liste.
	private static List<String> readFile(String nomFichier) throws IOException {
		List<String> listOfLines = new ArrayList<String>();
		String line = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(nomFichier);

			bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				listOfLines.add(line);
			}
		} finally {
			fileReader.close();
			bufferedReader.close();
		}
		return listOfLines;
	}

	// Fonction permettant d'écrire dans un fichier les données contenues dans la
	// stack reçu du serveur.
	private static void writeToFile(Stack<String> myStack, String nomFichier) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(nomFichier));
			while (!myStack.isEmpty()) {
				out.write(myStack.pop() + "\n");
			}
		} finally {
			out.close();
		}
	}
}
