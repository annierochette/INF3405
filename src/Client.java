import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Le client doit être capable de lire un fichier texte et d’envoyer son contenu au serveur qui retransmettra aussitôt son contenu au client. Ce dernier devra intercepter le contenu du fichier texte. Une fois la réception terminée, le serveur devra inverser le contenu du fichier de sorte à ce que la première ligne reçue soit la dernière ligne envoyée vers le client. **/


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		try {
			// CrÈation d'un socket client vers le serveur. Ici 127.0.0.1 est indicateur que
			// le serveur s'exÈcute sur la machine locale. Il faut changer 127.0.0.1 pour
			// l'adresse IP du serveur si celui-ci ne s'exÈcute pas sur la mÍme machine. Le port est 5000.
			clientSocket = new Socket("127.0.0.1", 5000);
			ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			// Ici, on suppose que le fichier que vous voulez inverser se nomme text.txt
			List<String> linesToSend = readFile("text.txt");
			// …criture de l'objet ‡ envoyer dans le output stream. Attention, la fonction
			// writeObject n'envoie pas l'objet vers le serveur! Elle ne fait qu'Ècrire dans
			// le output stream.
			objectOutput.writeObject(linesToSend);
			// Envoi des lignes du fichier texte vers le serveur sous forme d'une liste.
			objectOutput.flush();
			// CrÈation du input stream, pour recevoir les donnÈes traitÈes du serveur.
			ObjectInputStream obj = new ObjectInputStream(clientSocket.getInputStream());
			// NotÈ bien que la fonction readObject est bloquante! Ainsi, l'exÈcution du
			// client s'arrÍte jusqu'‡ la rÈception du rÈsultat provenant du serveur!
			Stack<String> receivedStack = (Stack<String>) obj.readObject();
			// …criture du rÈsultat dans un fichier nommÈe FichierInversee.txt
			writeToFile(receivedStack, "FichierInversee.txt");
		} finally {
			// Fermeture du socket.
			clientSocket.close();
		}
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

	// Fonction permettant d'Ècrire dans un fichier les donnÈes contenues dans la
	// stack reÁu du serveur.
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
