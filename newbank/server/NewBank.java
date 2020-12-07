package newbank.server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.UUID;

public class NewBank {

	private static final NewBank bank = new NewBank();
	private final HashMap<String, User> users;
	private final HashMap<UUID, MicroLoan> microLoans;

	private NewBank() {
		users = new HashMap<>();
		microLoans = new HashMap<>();
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

		// Add new microloans from Bhagy and Christina
		offerMicroLoan(bhagy, "OFFERMICROLOAN Main 200");
		offerMicroLoan(christina, "OFFERMICROLOAN Savings 100");
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

				if ("BUYBITCOIN".equals(request.substring(0, "BUYBITCOIN".length()))) {
					return buyBitcoin(customer, request);
				}

				if ("BITCOINPAY".equals(request.substring(0, "BITCOINPAY".length()))) {
					return bitcoinPay(customer, request);
				}

				if (request.startsWith("OFFERMICROLOAN ")) {
					return offerMicroLoan(customer, request);
				}

				if (request.equals("SEARCHMICROLOAN")) {
					return showMicroLoans(customer);
				}

				if (request.startsWith("TAKEMICROLOAN ")) {
					return takeMicroLoan(customer, request);
				}
				
				if (request.startsWith("CHANGECUSTOMER ")) {
					return changeCustomer(customer, request);
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
				} else if(request.startsWith("FREEZECUSTOMER")) {
					return freezeCustomer(request);
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

	private String changeCustomer(Customer customer, String request) throws Exception{
		String[] changeCommand = request.split(" ");
		try {
			String firstName = changeCommand[1];
			String lastName = changeCommand[2];
			customer.setFirstName(firstName);
			customer.setLastName(lastName);
			return "Name changed to " + customer.getFirstName() + " " + customer.getLastName();
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to update the customer name");
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
	
	private String freezeCustomer(String request) throws Exception {
		String[] freezeCommand = request.split(" ");

		try {
			String customerName = freezeCommand[1];

			if(users.containsKey(customerName)) {
				Customer customer = (Customer) users.get(customerName);
				if(customer.freeze()) {
					return customerName + " frozen";
				} else {
					return customerName + " unfrozen";
				}
			} else {
				return "Account was not found for " + customerName;
			}
		} catch (Exception e) {
			throw new Exception("Something went wrong when trying to freeze the customer account");
		}

	}

	public void help (UserID userId, PrintWriter out) {
		if (users.containsKey(userId.getKey())) {
			User user = users.get(userId.getKey());
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
				
				out.println("CHANGECUSTOMER " + " : allows you to change your name. To do this: ");
				out.println("Type CHANGECUSTOMER in capital letters as shown, followed by a space, followed by the new firstname " +
						", followed by a space, followed by the new surname.");
				out.println();

			} else {
				out.println("NEWCUSTOMER " + " : allows you to create a new customer on the system. To do this: ");
				out.println("Type NEWCUSTOMER in capital letters as shown, followed by a space, followed by the customer name, " +
						"and followed by a password");
				out.println();

				out.println("DELETECUSTOMER " + " : allows you to delete a customer on the system. To do this :");
				out.println("Type DELETECUSTOMER in capital letters as shown, followed by a space, followed by the customer name");
				out.println();
				
				out.println("FREEZECUSTOMER " + " : allows you to toggle whether a customer's accounts are frozen. To do this :");
				out.println("Type FREEZECUSTOMER in capital letters as shown, followed by a space, followed by the customer name");
				out.println();
			}

		}
	}

	/**
	 * Allows a customer to buy bitcoin with money from one of its accounts. If the customer
	 * previously did not have a bitcoin wallet, one is created.
	 * If the specified account is not valid or does not hold enough money no transaction is
	 * performed.
	 *
	 * @param customer The customer who executes the command and wants to buy bitcoin
	 * @param request The complete CLI request which also contains all parameters
	 * @return A message which indicates the transaction has been completed successfully or an error
	 * message
	 */
	private String buyBitcoin(Customer customer, String request) {
		if (customer != null && request != null) {
			String[] parameters = request.split(" ");
			//check request format
			if (parameters.length != 3) {
				return String.format(
						"Expected the following format for the BUYBITCOIN command:\n\n" +
								"BUYBITCOIN <SourceCustomerAccount> <Amount>\n\n" +
								"but the number of parameters found after BUYBITCOIN is %d",
						parameters.length - 1
				);
			}

			//check if account exists
			String accountName = parameters[1];
			Account account = customer.getAccount(accountName);
			if (account == null) {
				return String.format("Account \"%s\" was not found", accountName);
			}

			//check if the customer already has a bitcoin wallet
			if (customer.getBtcWallet() == null) {
				customer.createBtcWallet();
			}

			//check the amount and verify account balance
			double amount;
			try {
				amount = Double.parseDouble(parameters[2]);
				if (amount < 0.0) {
					return String
							.format("Invalid (negative) value for transfer amount: \"%s\"", parameters[2]);
				}
			} catch (NumberFormatException ignored) {
				return String.format("Invalid value for transfer amount: \"%s\"", parameters[2]);
			}
			if (amount > account.getBalance()) {
				return String.format("Insufficient balance for this transfer: %.2f", account.getBalance());
			}

			//perform the move
			if (customer.getBtcWallet().changeBitcoin(customer.getBtcWallet().getBtcEquivalent(amount))) {
				account.setBalance(account.getBalance() - amount);
			} else {
				return "Error performing the transaction: Could not move the specified amount.";
			}
			return "Transaction complete: Account(" + account.toString() + "), BitcoinWallet(" + customer
					.getBtcWallet().toString() + ")";
		}
		return "Error processing request: Customer or request are invalid.";
	}

	/**
	 * Allows a customer to transfer money from its own bitcoin wallet into another.
	 *
	 * @param customer The customer who executes the command and wants to pay via bitcoin into a
	 *                 bitcoin wallet
	 * @param request The complete CLI request which also contains all parameters
	 * @return A message which indicates the transaction has been completed successfully or an error
	 * message
	 */
	private String bitcoinPay(Customer customer, String request) {
		if (customer != null && request != null) {
			String[] parameters = request.split(" ");
			//check request format
			if (parameters.length != 3) {
				return String.format(
						"Expected the following format for the BITCOINPAY command:\n\n" +
								"BITCOINPAY <BitcoinAmount> <DestinationWallet> \n\n" +
								"but the number of parameters found after BITCOINPAY is %d\n\n" +
								"* <Destination-Person/Company>: NewBank.{NewBankUserName} for this bank's customers",
						parameters.length - 1
				);
			}

			//check if the customer has a bitcoin wallet
			if (customer.getBtcWallet() == null) {
				return "The current customer does not have a bitcoin wallet.";
			}

			//check the amount and verify bitcoin wallet balance
			double amount;
			try {
				amount = Double.parseDouble(parameters[1]);
				if (amount < 0.0) {
					return String
							.format("Invalid (negative) value for transfer amount: \"%s\"", parameters[2]);
				}
			} catch (NumberFormatException ignored) {
				return String.format("Invalid value for transfer amount: \"%s\"", parameters[2]);
			}
			if (amount > customer.getBtcWallet().getBitcoins()) {
				return String.format("Insufficient balance for this transfer: %.8f",
						customer.getBtcWallet().getBitcoins());
			}

			//check if destination person/company is also a customer of this bank
			String recipientName = parameters[2];
			Customer recipientCustomer = null;
			if (recipientName.length() >= "NEWBANK.".length() && recipientName
					.substring(0, "NEWBANK.".length()).toUpperCase().equals("NEWBANK.")) {
				recipientName = recipientName.substring("NEWBANK.".length());
				User recipient;
				if (users.containsKey(recipientName)) {
					recipient = users.get(recipientName);
				} else {
					return String
							.format("Recipient \"%s\" not identified in NewBank user records", recipientName);
				}
				if (!(recipient instanceof Customer)) {
					return String
							.format("Recipient \"%s\" not identified in NewBank customer records", recipientName);
				}
				recipientCustomer = (Customer) recipient;
			}

			//perform the move
			if (recipientCustomer != null) {
				if (recipientCustomer.getBtcWallet() == null) {
					recipientCustomer.createBtcWallet();
				}
				if (!recipientCustomer.getBtcWallet().changeBitcoin(amount)) {
					return "Error transferring bitcoins to recipient.";
				}
				if (!customer.getBtcWallet().changeBitcoin(-amount)) {
					return "Error removing the bitcoins from the sender.";
				}
			} else {
				if (!customer.getBtcWallet().changeBitcoin(-amount)) {
					return "Error removing the bitcoins from the sender.";
				}
			}
			return "Transaction complete: BitcoinWallet(" + customer
					.getBtcWallet().toString() + ")";
		}
		return "Error processing request: Customer or request are invalid.";
	}

	/**
	 * Offer a microloan of a specific amount from a customer to the other customers of the bank.
	 * If there are no sufficient funds, then the request should be rejected
	 *
	 * @param customer	The customer that the microloan is created from
	 * @param request	The offer microloan request as recorded from the CLI interface
	 * @return 	"SUCCESS" if the pay request has been completed successfully. An error message will be
	 * 			returned otherwise
	 */
	private String offerMicroLoan(Customer customer, String request) {
		String[] requestParameterArr = request.split(" ");
		// expected request format: OFFERMICROLOAN <Account> <Amount>
		if (requestParameterArr.length != 3) {
			return String.format(
					"Expected the following format for the offer micro loan command:\n\n" +
							"OFFERMICROLOAN <Account> <Amount>\n\n" +
							"but the number of parameters found after OFFERMICROLOAN is %d",
					requestParameterArr.length - 1
			);
		}
		// Check if account exists
		String accountName = requestParameterArr[1];
		Account account = customer.getAccount(accountName);
		if (account == null) {
			return String.format("Account \"%s\" was not found", accountName);
		}
		// Check if there are sufficient funds
		double amount;
		try {
			amount = Double.parseDouble(requestParameterArr[2]);
		} catch (NumberFormatException ignored) {
			return String.format("Invalid value for microloan amount: \"%s\"", requestParameterArr[2]);
		}
		if (amount > account.getBalance()) {
			return String.format("Insufficient balance for this microloan: %.2f", account.getBalance());
		}
		// Add the microloan in the market place
		MicroLoan microLoan = new MicroLoan(customer, amount);
		this.microLoans.put(microLoan.microLoanID.getKey(), microLoan);
		account.setBalance(account.getBalance() - amount);
		return "Microloan added successfully";
	}

	/** Helper that returns a string with all the available microloans and their details */
	private String showMicroLoans(Customer customer) {
		StringBuilder loansStrBuild = new StringBuilder("Available Microloans\n");
		loansStrBuild.append(new String(new char[loansStrBuild.length()-1]).replace("\0", "#"));
		loansStrBuild.append("\nFrom\t||\tAmount\t||\tMicroLoanID\n");
		for (UUID microLoanID : microLoans.keySet()) {
			MicroLoan microLoan = microLoans.get(microLoanID);
			if (!microLoan.getAvailability() || customer.equals(microLoan.getOwner())) {
				continue;
			}
			loansStrBuild.append(String.format(
					"%s %s\t||\t%.2f\t||\t%s\n",
					microLoan.getOwner().getFirstName(),
					microLoan.getOwner().getLastName(),
					microLoan.getAmount(),
					microLoan.getID().toString()
			));
		}
		return loansStrBuild.toString();
	}

	/**
	 * Borrow the full or partial amount from a microloan.
	 * If the amount of the offered microloan is less than the requested amount, then the request should be rejected
	 *
	 * @param customer	The customer that the microloan is created from
	 * @param request	The take microloan request as recorded from the CLI interface
	 * @return 	"SUCCESS" if the pay request has been completed successfully. An error message will be
	 * 			returned otherwise
	 */
	private String takeMicroLoan(Customer customer, String request) {
		String[] requestParameterArr = request.split(" ");
		// expected request format: TAKEMICROLOAN <Account> <MicroloanID> (<Amount>)
		if (requestParameterArr.length < 3 || requestParameterArr.length > 4) {
			return String.format(
					"Expected the following format for the offer micro loan command:\n\n" +
							"TAKEMICROLOAN <Account> <MicroloanID> (<Amount>)\n\n" +
							"but the number of parameters found after OFFERMICROLOAN is %d",
					requestParameterArr.length - 1
			);
		}
		// Check if account exists
		String accountName = requestParameterArr[1];
		Account account = customer.getAccount(accountName);
		if (account == null) {
			return String.format("Account \"%s\" was not found", accountName);
		}
		// Check if microloan exists
		String microLoanID = requestParameterArr[2];
		MicroLoan microLoan = microLoans.get(UUID.fromString(microLoanID));
		if (microLoan == null) {
			return String.format("Microloan with ID: \"%s\" was not found", microLoanID);
		}
		double amount;
		if (requestParameterArr.length == 4) {
			try {
				amount = Double.parseDouble(requestParameterArr[3]);
			} catch (NumberFormatException ignored) {
				return String.format("Invalid value for microloan amount: \"%s\"", requestParameterArr[2]);
			}
			// Check if there are sufficient funds in the microloan offering
			if (amount > microLoan.getAmount()) {
				return String.format("Insufficient microloan amount for this microloan: %.2f", microLoan.getAmount());
			}
		} else {
			amount = microLoan.getAmount();
		}
		// Add a new microloan for the remainder if the full amount is not claimed
		double remainder = microLoan.getAmount() - amount;
		if (remainder > 0.001) {
			MicroLoan microLoanRemainder = new MicroLoan(microLoan.getOwner(), remainder);
			this.microLoans.put(microLoanRemainder.microLoanID.getKey(), microLoanRemainder);
		}
		// Claim the amount from the microloan
		account.setBalance(account.getBalance() + amount);
		microLoan.setAmount(amount);
		microLoan.assignToReceiver(customer);
		return String.format("Successfully claimed a microloan for %.2f", amount);
	}

}
