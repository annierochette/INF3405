import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Stack;

/** Le client doit tre capable de lire un fichier texte et dÕenvoyer son contenu au serveur qui retransmettra aussit™t son contenu au client. Ce dernier devra intercepter le contenu du fichier texte. Une fois la rception termine, le serveur devra inverser le contenu du fichier de sorte ˆ ce que la premire ligne reue soit la dernire ligne envoye vers le client. **/
//L'implémentation du serveur n'est pas multi-threaded. Ainsi, la connection de
//plusieurs clients en même temps au serveur ne fonctionnera pas! A vous de threader le serveur
//pour qu'il puisse avoir la capacité d'accepter plusieurs clients.
public class Server {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		while (true) {
			ServerSocket serverSocket = null;
			Socket socket = null;
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			try {
				// Création du socket du serveur en utilisant le port 5000.
				serverSocket = new ServerSocket(5000);
				// Ici, la fonction accept est bloquante! Ainsi, l'exécution du serveur s'arrête
				// ici et attend la connection d'un client avant de poursuivre.
				socket = serverSocket.accept();
				// Création d'un input stream. Ce stream contiendra les données envoyées par le
				// client.
				in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				// La fonction readObject est bloquante! Ainsi, le serveur arrête son exécution
				// et attend la réception de l'objet envoyé par le client!
				List<String> strings = (List<String>) in.readObject();
				Stack<String> stackOfLines = new Stack<String>();
				// Remplissage de la stack avec les lignes. La première ligne entrée sera la
				// dernière à ressortir.
				for (int i = 0; i < strings.size(); i++) {
					stackOfLines.push(strings.get(i));
				}
				// Création du output stream. Ce stream contiendra les données qui seront
				// envoyées au client.
				out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				// Écriture des données dans la pile.
				out.writeObject(stackOfLines);
				// Envoi des données vers le client.
				out.flush();
			} finally {
				serverSocket.close();
				socket.close();
			}
		}
	}
}
