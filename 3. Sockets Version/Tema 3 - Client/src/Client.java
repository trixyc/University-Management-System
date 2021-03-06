/*
 * Aplicatii Integrate pentru Intreprinderi
 * 
 * Stefan-Dobrin Cosmin
 * 342C4
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import aii.arhiva.Arhiva;
import aii.jurnal.Jurnal;
import aii.protocol.MessageParser;
import aii.protocol.MessageStructure;
import aii.protocol.MessageStructure.Sender;
import aii.rad.RegistruActivitatiDidactice;

/**
 * The Class Client.
 */
public class Client {

	private static final int JURNAL = 0;
	private static final int ARHIVA = 1;
	private static final int RAD = 2;

	private static ConnectionInfo[] connections = new ConnectionInfo[3];

	/**
	 * Connects to a given server.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @return the connection info
	 */
	private static ConnectionInfo connectToServer(String host, int port) {
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + host);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: localhost.");
			System.exit(-1);
		}

		return new ConnectionInfo(socket, out, in);

	}

	/**
	 * Testing unit.
	 *
	 * @param mode the mode
	 * @param fromSocket the from socket
	 * @param toSocket the to socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void testingUnit(int mode, BufferedReader fromSocket, PrintWriter toSocket)
			throws IOException {
		// TEST Arhiva
		if (mode == ARHIVA) {
			String response;
			String message;
			int testCount = 0;
			String testName = "ARHIVA";

			// TEST Disciplina
			message = "stabilire_plan_de_invatamant#6#"
					+ "adaugare#987~Inteligenta Artificiala~Obligatoriu~5~6~Examen~4~1~0#" + // true
					"stergere#987#" + // true
					"editare#987~Aplicatii Integrate pentru Intreprinderi~Optional~5~6~Colocviu~4~1~0#" + // false
					"stergere#988#" + // true, daca mai exista (ar trebui sa
										// existe de la rularea de test
										// anterioara)
					"adaugare#988~Inteligenta Artificiala~Obligatoriu~5~6~Examen~4~1~0#" + // true
					"editare#988~Aplicatii Integrate pentru Intreprinderi~Optional~5~6~Colocviu~4~1~0"; // true
			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Note Student
			message = "solicitare_note#444#3#" + 
					"1#" + // nu are nota
					"9#" + // are nota si e disciplina lui
					"988"; // nu e disciplina lui

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Situatie Scolara
			message = "solicitare_situatie_scolara#444#4"; // situatia scolara
															// pentru anul 4
			// raspunsul are: "medieGenerala", "medieAritmetica",
			// "puncteCredit", "restante", "medieSemestrul1", "medieSemestrul2"

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Discipline Urmate
			message = "solicitare_discipline_urmate#444#4#1"; 	// disciplinele
																// pentru anul
																// 2, semestrul
																// 1

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Stabilire nota
			message = "stabilire_nota#222#3#" + 
					"7~444~9~17-01-2012#" + // deja
											// are o
											// nota
											// ->
											// intoarce
											// -2
					"3~444~6~18-02-2012#" + // la prima executie a testului o sa
											// scrie nota 6 (si va intoarce 1),
											// dupa care va face o suprascriere
											// (vezi readme) si va intoarce 2
					"10~444~988~12-01-2012"; // nu e materia cadrului respectiv
			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));
		}

		// TEST RAD
		if (mode == RAD) {
			String response;
			String message;
			int testCount = 0;
			String testName = "RAD";

			// TEST Activitati
			message = "repartizare_cadre_didactice#3#" + "1876398642864~988~Curs#" + // Repartizare valida
					"666~988~Laborator#" + // Intrare valida
					"2890508035278~983328~Seminar"; // Repartizare nevalida,
													// pentru ca nu exista acea
													// disciplina

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Orar
			message = "stabilire_orar#2#" +
				"Vineri~14~EC105~2~24~343C4~Saptamanal#" +
					// Adaug o noua intrare in orar, Vineri, intre orele 14-16, in
					// EC105, Saptamanal, pentru 343C4,
					// pentru activitatea 24 (ar trebui sa fie activitatea de
					// curs adaugata mai sus, pentru baza de date data ca
					// exemplu).
				"Joi~10~EG106~2~25~341C4~Saptamanal";
					// Adaug o noua intrare in orar, Joi, intre orele 10-12, in EG106,
					// Saptamanal, pentru 341C4,
					// pentru activitatea 25 (este activitatea de Laborator adaugata mai
					// sus, pentru baza de date data ca exemplu).

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Examen
			message = "stabilire_calendar_examene#4#" + 
					"stergere#988~341C4#" + // true/ false - depinde daca exista sau nu
					"editare#12-02-2012~12~EC101~988~341C4#" + // false
					"adaugare#09-02-2012~8~EC108~988~341C4#" + // true
					"editare#12-02-2012~12~EC101~988~341C4"; // true

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Orar Student
			message = "solicitare_orar#444#1"; // orarul pentru anul 4, semestrul 1

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Examene Student
			message = "solicitare_calendar_examene#444"; // calendarul examenelor pentru anul
															// 4, semestrul 1
															// (semestrul
															// curent)

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Examene Student - Fail
			message = "solicitare_calendar_examene#222"; // examenele pentru anul 4, semestrul 1 (semestrul curent)

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Cadru disciplina
			message = "cadru_pentru_disciplina#1876398642864#2#" + 
					"988#" + // true, dupa cum se vede de mai sus
					"3"; // false

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Stabilire grupa
			message = "stabilire_formatie_de_studiu#341C4#3#" + 
					"444#" + // va reusi
					"777#" + // va reusi
					"222"; // nu e student, deci nu va reusi

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));
		}

		// TEST Jurnal
		if (mode == JURNAL) {
			String response;
			String message;
			int testCount = 0;
			String testName = "JURNAL";

			// TEST Schimbare grupa
			message = "schimbare_grupa#444#342C4";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Schimbare grupa - eroare, ca exista deja o cerere
			message = "schimbare_grupa#444#343C4";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Contestare nota
			message = "contestare_nota#444#222#3";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Contestare nota - invalid, mai exista deja
			message = "contestare_nota#444#222#3";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Contestare nota
			message = "contestare_nota#444#666#7";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Contestare nota
			message = "contestare_nota#444#222#7";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Raspuns cereri student
			message = "rezultat_solicitari#444";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Cereri asteptare cadru didactic
			message = "cereri_in_asteptare_cadru_didactic#222";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Rezolvare cereri cadru didactic
			message = "rezolvare_cereri_cadru_didactic#2#" + 
					"2~1#" + // prima cerere catre 222 de mai sus
					"5~-1"; // a doua cerere catre 222 de mai sus

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Raspuns cereri student
			message = "rezultat_solicitari#444";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Cereri asteptare secretar
			message = "cereri_in_asteptare_secretar#333";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Rezolvare cereri secretar
			message = "rezolvare_cereri_secretar#1#" + 
				"0~1#"; // cererea de schimbare grupa de mai sus

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

			// TEST Raspuns cereri student
			message = "rezultat_solicitari#444";

			toSocket.println(message);
			System.out.println(String.format("__________________\nTest %d pentru %s: %s", ++testCount,
					testName, message));
			response = fromSocket.readLine();
			System.out.println(String.format("Raspuns %d pentru %s: %s", testCount, testName, response));

		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {

		Sender mode = Sender.STUDENT;
		BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));
		String line;
		Boolean done = false;
		Boolean testingMode = false;

		// Ne conectam la componente
		connections[JURNAL] = connectToServer(Jurnal.SERVER_ADDRESS, Jurnal.SERVER_PORT);
		connections[ARHIVA] = connectToServer(Arhiva.SERVER_ADDRESS, Arhiva.SERVER_PORT);
		connections[RAD] = connectToServer(RegistruActivitatiDidactice.SERVER_ADDRESS,
				RegistruActivitatiDidactice.SERVER_PORT);

		System.out.println("Ne-am conectat la cele 3 servere.");

		System.out.println("Introduceti modul de pornire al aplicatiei: 0 - Testing, 1 - Utilizare normala");
		while (!done) {
			try {
				System.out.print("> ");
				line = keyboardInput.readLine();
				switch (Integer.parseInt(line)) {
				case 0:
					System.out.println("Mod selectat:Testare");
					testingMode = true;
					done = true;
					break;
				case 1:
					testingMode=false;
					System.out.println("Mod selectat: Utilizare normala");
					done = true;
					break;
				default:
					System.err.println("Mod necorespunzator. Incercati din nou:");
				}
			} catch (Exception e1) {
				System.err.println("Mod necorespunzator. Incercati din nou:");
				e1.printStackTrace();
			}
		}
		
		//Facem testare
		if(testingMode)
		{
			try {
				System.out.println("\n---------TESTE ARHIVA------");
				testingUnit(ARHIVA, connections[ARHIVA].fromServer, connections[ARHIVA].toServer);
				System.out.println("\n---------TESTE RAD------");
				testingUnit(RAD, connections[RAD].fromServer, connections[RAD].toServer);
				System.out.println("\n---------TESTE JURNAL------");
				testingUnit(JURNAL, connections[JURNAL].fromServer, connections[JURNAL].toServer);
				
				//Cleanup
				keyboardInput.close();
				connections[JURNAL].fromServer.close();
				connections[JURNAL].toServer.close();
				connections[JURNAL].socket.close();
				connections[RAD].fromServer.close();
				connections[RAD].toServer.close();
				connections[RAD].socket.close();
				connections[ARHIVA].fromServer.close();
				connections[ARHIVA].toServer.close();
				connections[ARHIVA].socket.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else
		{
			System.out.println("Introduceti tipul de utilizator cu care doriti sa va conectati:"
					+ "0 - Student, 1 - Cadru Didactic, 2 - Sef de Catedra, 3 - Secretar");
			done=false;
			while (!done) {
				try {
					System.out.print("> ");
					line = keyboardInput.readLine();
					switch (Integer.parseInt(line)) {
					case 0:
						mode = Sender.STUDENT;
						System.out.println("Mod selectat: Student");
						done = true;
						break;
					case 1:
						mode = Sender.CADRU_DIDACTIC;
						System.out.println("Mod selectat: Cadru Didactic");
						done = true;
						break;
					case 2:
						mode = Sender.SEF_CATEDRA;
						System.out.println("Mod selectat: Sef Catedra");
						done = true;
						break;
					case 3:
						mode = Sender.SECRETAR;
						System.out.println("Mod selectat: Secretar");
						done = true;
						break;
					default:
						System.err.println("Mod necorespunzator. Incercati din nou:");
					}
				} catch (Exception e1) {
					System.err.println("Mod necorespunzator. Incercati din nou:");
					e1.printStackTrace();
				}
			}
	
			String fromServer = null;
			String fromClient;
	
			//Citim mesajele de la clienti si le trimitem la server
			try {
				do {
					//Citire mesaj
					fromClient = keyboardInput.readLine();
					if(fromClient.trim().isEmpty())
						continue;
					//Check messsage locally
					MessageStructure structure=MessageParser.getMessageStructure(fromClient);
					
					//No corresponding message structure was found
					if(structure==null)
					{
						System.err.println("Unrecognized message type!");
						continue;
					}
					//Check message sender type
					if(mode!=structure.sender && structure.sender!=Sender.ANY)
						if(!(mode==Sender.SEF_CATEDRA && structure.sender==Sender.CADRU_DIDACTIC))
						{
							System.err.println("Mesaj ilegal de transmis de catre tipul de utilizator "+mode);
							continue;
						}
					
					//Transmitem mesajul componentei corespunzatoare
					int sentTo=0;
					switch(structure.type)
					{
					case ARHIVA:
						System.out.println("Mesaj client pentru arhiva: " + fromClient);
						connections[ARHIVA].toServer.println(fromClient);
						sentTo=ARHIVA;
						break;
					case JURNAL:
						System.out.println("Mesaj client pentru jurnal: " + fromClient);
						connections[JURNAL].toServer.println(fromClient);
						sentTo=JURNAL;
						break;
					case RAD:
						System.out.println("Mesaj client pentru rad: " + fromClient);
						connections[RAD].toServer.println(fromClient);
						sentTo=RAD;
						break;
					}
					
					//Primire raspuns
					fromServer = connections[sentTo].fromServer.readLine();
					if (fromServer == null)
						break;
					//Afisare raspuns
					System.out.println("Mesaj de la server: " + fromServer);
					if (fromServer.equals("end_connection"))
						break;
				} while (true);
	
				//Cleanup
				keyboardInput.close();
				connections[JURNAL].fromServer.close();
				connections[JURNAL].toServer.close();
				connections[JURNAL].socket.close();
				connections[RAD].fromServer.close();
				connections[RAD].toServer.close();
				connections[RAD].socket.close();
				connections[ARHIVA].fromServer.close();
				connections[ARHIVA].toServer.close();
				connections[ARHIVA].socket.close();
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to: localhost.");
			}
		}
		
	}
}
