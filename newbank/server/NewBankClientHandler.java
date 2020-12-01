package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewBankClientHandler extends Thread{
	
	private NewBank bank;
	private BufferedReader in;
	private PrintWriter out;
	private List<String> commands = Arrays.asList(
			"MOVE",
			"UPDATE",
			"NEWACCOUNT",
			"DELETEACCOUNT",
			"DELETEACCOUNT",
			"PAY",
			"SHOWMYACCOUNTS",
			"BUYBITCOIN",
			"BITCOINPAY",
			"OFFERMICROLOAN",
			"SEARCHMICROLOAN",
			"TAKEMICROLOAN"
	);
	
	public NewBankClientHandler(Socket s) throws IOException {
		bank = NewBank.getBank();
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);
	}
	
	public void run() {
		// keep getting requests from the client and processing them
		try {
			while (true) {
				// ask for user name
				out.println("Enter Username");
				String userName = in.readLine();
				// ask for password
				out.println("Enter Password");
				String password = in.readLine();
				out.println("Checking Details...");
				// authenticate user and get user ID token from bank for use in subsequent requests
				UserID user = bank.checkLogInDetails(userName, password);
				// if the user is authenticated then get requests from the user and process them
				if (user != null) {
					out.println("Log In Successful");

					while (true) {
						bank.help(user, out);
						String request = in.readLine();
						try {
							String[] requestCommand = request.split(" ");
							if (commands.contains(requestCommand[0])) {
								out.println("Request from " + user.getKey());
								String response = bank.processRequest(user, request);
								out.println(response);
							} else if (request.trim().equalsIgnoreCase("logoff")) {
								break;
							} else if (request.trim().isEmpty()) {
								continue;
							}
						} catch (Exception e) {
							out.println("Your request has failed: " + request);
							out.println(e.getLocalizedMessage());
							bank.help(user, out);
						}

					}
				} else {
					out.println("Log In Failed");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				out.flush();
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

}
