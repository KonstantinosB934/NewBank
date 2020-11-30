package newbank.server;

import java.io.PrintWriter;
import java.util.HashMap;

public class NewBank {

	private static final NewBank bank = new NewBank();
	private final HashMap<String,User> users;

	private NewBank() {
		users = new HashMap<>();
		addTestData();
	}

	private void addTestData() {
		Customer bhagy = new Customer("123", "Bhagyashree", "Patil");
		bhagy.addAccount(new Account("Main", 1000.0));
		bhagy.addAccount(new Account("Savings", 2000.0));
		users.put("Bhagy", bhagy);

		Customer christina = new Customer("555", "Christina", "Keating");
		christina.addAccount(new Account("Savings", 1500.0));
		users.put("Christina", christina);

		Customer john = new Customer("101", "John", "Benardis");
		john.addAccount(new Account("Checking", 250.0));
		users.put("John", john);

		BankEmployee max = new BankEmployee("123456", "Max", "Powers");
		users.put("Max", max);
	}
	
	public static NewBank getBank() { return bank; }


	public synchronized UserID checkLogInDetails(String userName, String password) {
		if(users.containsKey(userName)) {
			User user = users.get(userName);
			if(user.getPassword().equals(password)){
				return new UserID(userName);
			}
		}
		return null;
	}

	// commands from the NewBank user are processed in this method
	public synchronized String processRequest(UserID userId, String request) throws Exception {
		if(users.containsKey(userId.getKey())) {
			User user = users.get(userId.getKey());

			if (user instanceof Customer) {
				//all customer related protocols
				Customer customer = (Customer)user;
				if (request.startsWith("MOVE ")) {
					return moveMoney(customer, request);
				}

				if (request.startsWith("UPDATE ")) {
					return update(customer, request);
				}

				if (request.startsWith("NEWACCOUNT ")) {
					return createAcc(customer, request);
				}
				
				if (request.startsWith("DELETEACCOUNT ")) {
					return deleteAccount(customer, request);
				}

				if (request.startsWith("PAY ")) {
					return payAmountToOtherCustomer(customer, request);
				}

				if ("SHOWMYACCOUNTS".equals(request)) {
					return showMyAccounts(customer);
				}
				return "FAIL";

			} else if (user instanceof BankEmployee) {
				//all bank employee related protocols
				BankEmployee employee = (BankEmployee)user;
				if(request.startsWith("DELETECUSTOMER ")) {
					return deleteCustomer(request);
				} else if(request.startsWith("NEWCUSTOMER")) {
					return addCustomer(request);
				}
			}
		}
		return "FAIL";
	}

	private String showMyAccounts(Customer customer) throws Exception {
		try {
			return customer.accountsToString();
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to show your account");
		}
	}

	private String moveMoney(Customer customer, String request) throws Exception {
		String[] movecommand = request.split(" ");
		try {
			String From = movecommand[2];
			String To = movecommand[3];
			Double Amount = Double.parseDouble(movecommand[1]);
			return customer.moveMoney(From, To, Amount);
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to move money");
		}
	}

	private String update(Customer customer, String request) throws Exception{
		String[] updateCommand = request.split(" ");
		try {
			String newPassword = updateCommand[1];
			String confirmPassword = updateCommand[2];
			return customer.update(newPassword,confirmPassword);
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to update the password");
		}
	}

	private String createAcc(Customer customer, String request) throws Exception{
		String[] createCommand = request.split(" ");
		try {
			String accountName = createCommand[1];
			double openingBalance = Double.parseDouble(createCommand[2]);

			return customer.addAccount(new Account (accountName, openingBalance));
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to create an account");
		}
	}

	private String deleteCustomer(String request) throws Exception {
		String[] deleteCommand = request.split(" ");
		try {
			String customerName = deleteCommand[1];
			if(users.containsKey(customerName)) {
				User user = users.get(customerName);
				if (user instanceof Customer) {
					users.remove(customerName);
					return "SUCCESS";
				} else {
					return "FAIL";
				}
			} else {
				return "FAIL";
			}
		} catch (Exception e)  {
			throw new Exception("Something went wrong when trying to delete the customer name");
		}

	}

	private String deleteAccount(Customer customer, String request) throws Exception {
		String[] deleteCommand = request.split(" ");
		try {
			String myAccount = deleteCommand[1];
			return customer.delete(myAccount);
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to delete the account");
		}

	}

	/**
	 * Pay an amount to another customer of the bank. The outgoing account should be specified and
	 * if the receiving customer or account cannot be identified or if there are no sufficient funds,
	 * then the request should be rejected
	 *
	 * @param customer	The customer that the pay amount is paid from
	 * @param request	The pay request as recorded from the CLI interface
	 * @return 	"SUCCESS" if the pay request has been completed successfully. An error message will be
	 * 			returned otherwise
	 */
	private String payAmountToOtherCustomer(Customer customer, String request) throws Exception{
		String[] requestParameterArr = request.split(" ");

		try {
			// expected request format:
			// PAY <SourceCustomerAccount> <Destination-Person/Company> <DestinationAccount> <Amount>
			if (requestParameterArr.length != 5) {
				return String.format(
						"Expected the following format for the pay command:\n\n" +
								"PAY <SourceCustomerAccount> <Destination-Person/Company> <DestinationAccount> <Amount>\n\n" +
								"but the number of parameters found after PAY is %d",
						requestParameterArr.length - 1
				);
			}
			// Check if account exists
			String accountName = requestParameterArr[1];
			Account account = customer.getAccount(accountName);
			if (account == null) {
				return String.format("Account \"%s\" was not found", accountName);
			}
			// Check if recipient user and accounts exist
			String recipientName = requestParameterArr[2];
			User recipient;
			if (users.containsKey(recipientName)) {
				recipient = users.get(recipientName);
			} else {
				return String.format("Recipient \"%s\" not identified in NewBank customer records", recipientName);
			}
			if (!(recipient instanceof Customer)) {
				return String.format("Recipient \"%s\" not identified in NewBank customer records", recipientName);
			}
			Customer recipientCustomer = (Customer) recipient;
			String recipientAccountName = requestParameterArr[3];
			Account recipientAccount = recipientCustomer.getAccount(recipientAccountName);
			if (recipientAccount == null) {
				return String.format(
						"Account \"%s\" was not found for customer \"%s\"", recipientAccountName, recipientName
				);
			}
			// Check if there are sufficient funds
			double amount;
			try {
				amount = Double.parseDouble(requestParameterArr[4]);
			} catch (NumberFormatException ignored) {
				return String.format("Invalid value for transfer amount: \"%s\"", requestParameterArr[4]);
			}
			if (amount > account.getBalance()) {
				return String.format("Insufficient balance for this transfer: %.2f", account.getBalance());
			}
			// Finally if all checks are OK, the amount can be transferred
			account.setBalance(account.getBalance() - amount);
			recipientAccount.setBalance(recipientAccount.getBalance() + amount);
			return "SUCCESS";

		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to pay money to another customer");
		}
	}

	private String addCustomer(String request) throws Exception {
		String[] addCommand = request.split(" ");

		try {
			String customerName = addCommand[1];
			String password = addCommand[2];

			if(!users.containsKey(customerName)) {
				Customer customer = new Customer(password);
				users.put(customerName, customer);

				return "You have successfully added '" + customerName + "'";
			}
			else
				return "Sorry, customer already exists";
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to add a customer");
		}

	}

	public void help (UserID userId, PrintWriter out) {
		if (users.containsKey(userId.getKey())) {
			User user = users.get(userId.getKey());
			out.println("What do you want to do? (LOGOFF to logoff)");
			if (user instanceof Customer) {
				out.println("SHOWMYACCOUNTS " + " : shows the balance in different accounts. To view this: ");
				out.println("Type SHOWMYACCOUNTS in capital letters exactly as shown");
				out.println();

				out.println("MOVE " + " : allows money to be transferred from one account to another account. To use this function: ");
				out.println("Type MOVE in capital letters, followed by a space, followed by the amount in decimal figures," +
						"followed the account name from which you are taking money out, and followed by the account name in which " +
						"are transferring money into");
				out.println();

				out.println("UPDATE " + " : allows password to be updated. To do this:  ");
				out.println("Type UPDATE in capital letters, followed by a space, followed by the new password, and followed by " +
						"the new password again to confirm it");
				out.println();

				out.println("NEWACCOUNT " + " : allows a new account to be created. To do this:  " );
				out.println("Type NEWACCOUNT in capital letters as shown, followed by a space, followed by account name, and " +
						"followed by the amount you want to deposit into the account in decimal number");
				out.println();

				out.println("PAY " + " : allows you to transfer money from your account into another customer account. To do this :");
				out.println("Type PAY in capital letters as shown, followed by a space, followed by your account name, followed by" +
						"the recipient name, followed by the recipient account name, and followed by the amount in decimal number.");
				out.println();

				out.println("DELETEACCOUNT " + " : allows you to delete an account. To do this: ");
				out.println("Type DELETEACCOUNT in capital letters as shown, followed by a space, followed by the account name " +
						"you would like to delete.");
				out.println();

			} else {
				out.println("NEWCUSTOMER " + " : allows you to create a new customer on the system. To do this: ");
				out.println("Type NEWCUSTOMER in capital letters as shown, followed by a space, followed by the customer name, " +
						"and followed by a password");
				out.println();

				out.println("DELETECUSTOMER " + " : allows you to delete a customer on the system. To do this :");
				out.println("Type DELETECUSTOMER in capital letters as shown, followed by a space, followed by the customer name");
				out.println();
			}

		}
	}

}
