package newbank.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NewBank {

  private static NewBank bank;

  static {
    try {
      bank = new NewBank();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private final HashMap<String, User> users;
  private final HashMap<UUID, MicroLoan> microLoans;
  private final HashMap<String, Double> transferLimit;
  private final List<String> paysPendingApproval;
	private final String customersCSVPath = "bankData/customers.csv";
	private final String employeesCSVPath = "bankData/employees.csv";
	private final String accountsCSVPath = "bankData/accounts.csv";
	private final String microloansCSVPath = "bankData/microloans.csv";

  private NewBank() throws IOException {
    users = new HashMap<>();
    microLoans = new HashMap<>();
    transferLimit = new HashMap<>();
    paysPendingApproval = new ArrayList<>();
    readBankData();
  }

  public static NewBank getBank() {
    return bank;
  }

	/**
	 * Helper to read a CSV file and return the rows as a list of string arrays. The headers are ignored
	 * @param pathToCsv The path to the CSV to read
	 * @return The rows of the CSV file as a list of string arrays
	 * @throws IOException
	 */
  private List<String[]> readCSV (String pathToCsv) throws IOException {
		List<String[]> rows = new LinkedList<>();
		BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
		String row;
		int rowCount = 0;

		while ((row = csvReader.readLine()) != null) {
			String[] data = row.split(",");
			if (rowCount++ > 0) {
				rows.add(data);
			}
		}

		csvReader.close();
		return rows;
	}

	/**
	 * Helper to save list of string arrays to a CSV file.
	 * @param outputRows The rows of the CSV file
	 * @param pathToCsv The path to the CSV file
	 * @throws IOException
	 */
	private void saveCSV (List<String[]> outputRows, String pathToCsv) throws IOException {
		FileWriter csvWriter = new FileWriter(pathToCsv);

		for (String[] rowData : outputRows) {
			csvWriter.append(String.join(",", rowData));
			csvWriter.append("\n");
		}

		csvWriter.flush();
		csvWriter.close();
	}

  private void readBankData() throws IOException {
		// Read the customer data
  	List<String[]> customerRows = readCSV(customersCSVPath);
  	for (String[] customerRow : customerRows) {
			users.put(customerRow[0], new Customer(customerRow[1], customerRow[2], customerRow[3]));
		}
  	// Read the employee data
		List<String[]> employeeRows = readCSV(employeesCSVPath);
		for (String[] employeeRow : employeeRows) {
			users.put(employeeRow[0], new BankEmployee(employeeRow[1], employeeRow[2], employeeRow[3]));
		}
		// Read the account data
		List<String[]> accountRows = readCSV(accountsCSVPath);
		for (String[] accountRow : accountRows) {
			((Customer) users.get(accountRow[0])).addAccount(new Account(accountRow[1], Double.parseDouble(accountRow[2])));
		}
		// Read the microloan data
		List<String[]> microloanRows = readCSV(microloansCSVPath);
		for (String[] microloanRow : microloanRows) {
			MicroLoanID microLoanID = new MicroLoanID(microloanRow[0]);
			double amount = Double.parseDouble(microloanRow[1]);
			Customer owner = (Customer) users.get(microloanRow[2]);
			Customer receiver;
			if (microloanRow[3].length() > 0) {
				receiver = (Customer) users.get(microloanRow[3]);
			} else {
				receiver = null;
			}
			boolean isAvailable = microloanRow[4].equals("1");
			MicroLoan microLoan = new MicroLoan(microLoanID, owner, amount, isAvailable, receiver);
			this.microLoans.put(microLoan.microLoanID.getKey(), microLoan);
		}
  }

  private void saveBankData() throws IOException {
		// Save the customer, employee and account data
		List<String[]> customerRows = new LinkedList<>();
		customerRows.add(new String[]{"UserName", "Password", "FirstName", "LastName"});
		List<String[]> employeeRows = new LinkedList<>();
		employeeRows.add(new String[]{"UserName", "Password", "FirstName", "LastName"});
		List<String[]> accountRows = new LinkedList<>();
		accountRows.add(new String[]{"UserName", "AccountName", "Balance"});
		for (String userName : this.users.keySet()) {
			User user = this.users.get(userName);
			if (user instanceof Customer) {
				customerRows.add(new String[]{userName, user.getPassword(), user.getFirstName(), user.getLastName()});
				for (Account account : ((Customer) user).getAllAccounts()) {
					accountRows.add(new String[]{userName, account.getName(), String.valueOf(account.getBalance())});
				}
			} else {
				employeeRows.add(new String[]{userName, user.getPassword(), user.getFirstName(), user.getLastName()});
			}
		}
		saveCSV(customerRows, this.customersCSVPath);
		saveCSV(employeeRows, this.employeesCSVPath);
		saveCSV(accountRows, this.accountsCSVPath);
	}

  public synchronized UserID checkLogInDetails(String userName, String password) {
    if (users.containsKey(userName)) {
      User user = users.get(userName);
      if (user.getPassword().equals(password)) {
        return new UserID(userName);
      }
    }
    return null;
  }

  // commands from the NewBank user are processed in this method
  public synchronized String processRequest(UserID userId, String request) throws Exception {
    if (users.containsKey(userId.getKey())) {
      User user = users.get(userId.getKey());

      if (user instanceof Customer) {
        //all customer related protocols
        Customer customer = (Customer) user;
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
          return payAmountToOtherCustomer(customer, request, userId.getKey(), false);
        }

        if ("BUYBITCOIN".equals(request.substring(0, "BUYBITCOIN".length()))) {
          return buyBitcoin(customer, request);
        }

        if ("SELLBITCOIN".equals(request.substring(0, "SELLBITCOIN".length()))) {
          return sellBitcoin(customer, request);
        }

        if ("BITCOINPAY".equals(request.substring(0, "BITCOINPAY".length()))) {
          return bitcoinPay(customer, request);
        }

        if (request.startsWith("REVOKEMICROLOAN ")) {
          return revokeMicroLoan(customer, request);
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

				if (request.startsWith("DONATE")) {
					return donateMoney(customer, request);
				}

	                        if (request.startsWith("HISTORY")) {
                                      return history(customer, request);
                                 }

        if ("SHOWMYACCOUNTS".equals(request)) {
          return showMyAccounts(customer);
        }
        return "FAIL";

      } else if (user instanceof BankEmployee) {
        //all bank employee related protocols
        BankEmployee employee = (BankEmployee) user;
        if (request.startsWith("DELETECUSTOMER ")) {
          return deleteCustomer(request);
        } else if (request.startsWith("NEWCUSTOMER")) {
          return addCustomer(request);
        } else if (request.startsWith("FREEZECUSTOMER")) {
          return freezeCustomer(request);
        } else if (request.startsWith("PAY ")) {
          //special transfer: a bank employee may transfer more than the transfer limit in behalf of a customer
          return bankEmployeePayAdapter(employee, request);
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

      private String history(Customer customer, String request) throws Exception {
              String[] historyCommand = request.split(" ");
              try {
                    String accountName = historyCommand[1];
                    return customer.transactionRecord(accountName);
             } catch (Exception e) {
                      throw new Exception("Something went wrong when trying to show your account");
             }
       }

  private String donateMoney(Customer customer, String request) throws Exception {
    String[] donateCommand = request.split(" ");
    try {
      String from = donateCommand[1];
      Double amount = Double.parseDouble(donateCommand[2]);
      return customer.donateMoney(from, amount);
    } catch (Exception e) {
      throw new Exception("Something went wrong when trying donate");
    }
  }

  private String update(Customer customer, String request) throws Exception {
    String[] updateCommand = request.split(" ");
    try {
      String newPassword = updateCommand[1];
      String confirmPassword = updateCommand[2];
      return customer.update(newPassword, confirmPassword);
    } catch (Exception e) {
      throw new Exception("Something went wrong when trying to update the password");
    }
  }

  private String changeCustomer(Customer customer, String request) throws Exception {
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

  private String createAcc(Customer customer, String request) throws Exception {
    String[] createCommand = request.split(" ");
    try {
      String accountName = createCommand[1];
      double openingBalance = Double.parseDouble(createCommand[2]);

      return customer.addAccount(new Account(accountName, openingBalance));
    } catch (Exception e) {
      throw new Exception("Something went wrong when trying to create an account");
    }
  }

  private String deleteCustomer(String request) throws Exception {
    String[] deleteCommand = request.split(" ");
    try {
      String customerName = deleteCommand[1];
      if (users.containsKey(customerName)) {
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
    } catch (Exception e) {
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
   * Wraps the pay command method for customers so that an bank employee can specify the customer in
   * behalf of whom the transaction is made, for example to make payments that exceed the daily
   * transfer limit.
   *
   * @param bankEmployee The bank employee authorised to perform the transaction
   * @param request      The request which is processed
   * @return Messages indicating the success or failure of the command
   * @throws Exception
   */
  private String bankEmployeePayAdapter(User bankEmployee, String request) throws Exception {
    try {
      String[] command = request.split(" ");
      String onBehalfOf = "";
      if (command.length > 1) {
        onBehalfOf = command[1];
        User onBehalfOfUser = users.get(onBehalfOf);
        if (onBehalfOfUser != null && onBehalfOfUser instanceof Customer) {
          Customer onBehalfOfCustomer = (Customer) onBehalfOfUser;
          request = request.replaceFirst(" " + onBehalfOf, "");
          return payAmountToOtherCustomer(onBehalfOfCustomer, request, onBehalfOf, true);
        }
      }
      return String.format(
          "Invalid command, specify the affected customer as an additional parameter directly after the PAY command.");
    } catch (Exception e) {
      throw new Exception("Something went wrong with a bank employee pay command.");
    }
  }

  /**
   * Pay an amount to another customer of the bank. The outgoing account should be specified and if
   * the receiving customer or account cannot be identified or if there are no sufficient funds,
   * then the request should be rejected
   *
   * @param customer             The customer that the pay amount is paid from
   * @param request              The pay request as recorded from the CLI interface
   * @param userId               Id used to check for transfer limits
   * @param specialAuthorisation Indicates if this transfer is specially authorised (no transfer
   *                             limits respected)
   * @return "SUCCESS" if the pay request has been completed successfully. An error message will be
   * returned otherwise
   */
  private String payAmountToOtherCustomer(Customer customer, String request, String userId,
      boolean specialAuthorisation) throws Exception {
    String[] requestParameterArr = request.split(" ");

    try {
      // expected request format:
      // PAY <SourceCustomerAccount> <Destination-Person/Company> <DestinationAccount> <Amount>
      if (requestParameterArr.length != 5) {
        return String.format(
            "Expected the following format for the pay command:\n\n" +
                "PAY <SourceCustomerAccount> <Destination-Person/Company> <DestinationAccount> <Amount>\n\n"
                +
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
        return String
            .format("Recipient \"%s\" not identified in NewBank customer records", recipientName);
      }
      if (!(recipient instanceof Customer)) {
        return String
            .format("Recipient \"%s\" not identified in NewBank customer records", recipientName);
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

      //check transfer limit
      String checkKey = userId
          .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString());
      Double todayTransfers = transferLimit.get(checkKey);

      if (!specialAuthorisation) {
        if (todayTransfers == null && amount <= 10000) {
          transferLimit.put(checkKey, amount);
          todayTransfers = amount;
        } else if (todayTransfers != null && todayTransfers + amount <= 10000) {
          todayTransfers += amount;
          transferLimit.put(checkKey, todayTransfers);
        } else {
          return String.format(
              "Payment would exceed the daily limit of £10,000: \"%.2f\". DENIED. Ask an employee to do the transfer for you.",
              todayTransfers);
        }
      }

      // Finally if all checks are OK, the amount can be transferred
      account.addTransaction("Payment made: "+ -amount);
      recipientAccount.addTransaction("Payment received: " + amount);
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

      if (!users.containsKey(customerName)) {
        Customer customer = new Customer(password);
        users.put(customerName, customer);

        return "You have successfully added '" + customerName + "'";
      } else {
        return "Sorry, customer already exists";
      }
    } catch (Exception e) {
      throw new Exception("Something went wrong when trying to add a customer");
    }

  }

  private String freezeCustomer(String request) throws Exception {
    String[] freezeCommand = request.split(" ");

    try {
      String customerName = freezeCommand[1];

      if (users.containsKey(customerName)) {
        Customer customer = (Customer) users.get(customerName);
        if (customer.freeze()) {
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

  public void help(UserID userId, PrintWriter out) {
    if (users.containsKey(userId.getKey())) {
      User user = users.get(userId.getKey());
      if (user instanceof Customer) {
        out.println(
            "SHOWMYACCOUNTS " + " : shows the balance in different accounts. To view this: ");
        out.println("Type SHOWMYACCOUNTS in capital letters exactly as shown");
        out.println();

        out.println("MOVE "
            + " : allows money to be transferred from one account to another account. To use this function: ");
        out.println(
            "Type MOVE in capital letters, followed by a space, followed by the amount in decimal figures,"
                +
                "followed the account name from which you are taking money out, and followed by the account name in which "
                +
                "are transferring money into");
        out.println();

        out.println("UPDATE " + " : allows password to be updated. To do this:  ");
        out.println(
            "Type UPDATE in capital letters, followed by a space, followed by the new password, and followed by "
                +
                "the new password again to confirm it");
        out.println();

        out.println("NEWACCOUNT " + " : allows a new account to be created. To do this:  ");
        out.println(
            "Type NEWACCOUNT in capital letters as shown, followed by a space, followed by account name, and "
                +
                "followed by the amount you want to deposit into the account in decimal number");
        out.println();

        out.println("PAY "
            + " : allows you to transfer money from your account into another customer account. To do this :");
        out.println(
            "Type PAY in capital letters as shown, followed by a space, followed by your account name, followed by"
                +
                "the recipient name, followed by the recipient account name, and followed by the amount in decimal number.");
        out.println();

				out.println("DONATE " + " : allows you to donate money from your account towards philanthropic initiatives. To do this : ");
				out.println("Type DONATE in capital letters as shown, followed by a space, followed by your account name," +
						" followed by the amount you wish to donate in decimal number");
				out.println();

				out.println("HISTORY " + " : allows you to view transaction history of account. To do this : ");
                                out.println("Type HISTORY in capital letters as shown, followed by a space, followed by your account name.");
                                out.println();

        out.println("DELETEACCOUNT " + " : allows you to delete an account. To do this: ");
        out.println(
            "Type DELETEACCOUNT in capital letters as shown, followed by a space, followed by the account name "
                +
                "you would like to delete.");
        out.println();

        out.println("CHANGECUSTOMER " + " : allows you to change your name. To do this: ");
        out.println(
            "Type CHANGECUSTOMER in capital letters as shown, followed by a space, followed by the new firstname "
                +
                ", followed by a space, followed by the new surname.");
        out.println();

      } else {
        out.println(
            "NEWCUSTOMER " + " : allows you to create a new customer on the system. To do this: ");
        out.println(
            "Type NEWCUSTOMER in capital letters as shown, followed by a space, followed by the customer name, "
                +
                "and followed by a password");
        out.println();

        out.println(
            "DELETECUSTOMER " + " : allows you to delete a customer on the system. To do this :");
        out.println(
            "Type DELETECUSTOMER in capital letters as shown, followed by a space, followed by the customer name");
        out.println();

        out.println("FREEZECUSTOMER "
            + " : allows you to toggle whether a customer's accounts are frozen. To do this :");
        out.println(
            "Type FREEZECUSTOMER in capital letters as shown, followed by a space, followed by the customer name");
        out.println();
      }

    }
  }

  /**
   * Allows a customer to buy bitcoin with money from one of its accounts. If the customer
   * previously did not have a bitcoin wallet, one is created. If the specified account is not valid
   * or does not hold enough money no transaction is performed.
   *
   * @param customer The customer who executes the command and wants to buy bitcoin
   * @param request  The complete CLI request which also contains all parameters
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
                "BUYBITCOIN <SourceAccount> <Amount>\n\n" +
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
				account.addTransaction("Bitcoin purchase: "+ -amount);
			} else {
				return "Error performing the transaction: Could not move the specified amount.";
			}
			return "Transaction complete: Account(" + account.toString() + "), BitcoinWallet(" + customer
					.getBtcWallet().toString() + ")";
		}
		return "Error processing request: Customer or request are invalid.";
	}

  /**
   * Allows a customer to sell bitcoin, receiving money into one of its accounts. If the customer
   * does not have a bitcoin wallet, the transactions fails. If the specified bitcoin amount is not
   * available, the transactions fails.
   *
   * @param customer The customer who executes the command and wants to sell bitcoin
   * @param request  The complete CLI request which also contains all parameters
   * @return A message which indicates the transaction has been completed successfully or an error
   * message
   */
  private String sellBitcoin(Customer customer, String request) {
    if (customer != null && request != null) {
      String[] parameters = request.split(" ");
      //check request format
      if (parameters.length != 3) {
        return String.format(
            "Expected the following format for the SELLBITCOIN command:\n\n" +
                "SELLBITCOIN <DestinationAccount> <Amount>\n\n" +
                "but the number of parameters found after SELLBITCOIN is %d",
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
        return "You do not have a bitcoin wallet.";
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
      if (amount > customer.getBtcWallet().getBitcoins()) {
        return String.format("Insufficient balance for this transfer: %.2f", account.getBalance());
      }

			//perform the move
			if (customer.getBtcWallet().changeBitcoin(-amount)) {
				account.setBalance(account.getBalance() + customer.getBtcWallet().getEquivalentToBtc(amount));
				account.addTransaction("Bitcoin sale: " + amount);
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
   * @param request  The complete CLI request which also contains all parameters
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
   * Offer a microloan of a specific amount from a customer to the other customers of the bank. If
   * there are no sufficient funds, then the request should be rejected
   *
   * @param customer The customer that the microloan is created from
   * @param request  The offer microloan request as recorded from the CLI interface
   * @return "SUCCESS" if the pay request has been completed successfully. An error message will be
   * returned otherwise
   */
  private String offerMicroLoan(Customer customer, String request) {
    String[] requestParameterArr = request.split(" ");
    // expected request format: OFFERMICROLOAN <SourceCustomerAccount> <Amount>
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
    account.addTransaction("MicroLoan on offer: " + amount);
    return "Microloan added successfully";
  }

  /**
   * Helper that returns a string with all the available microloans and their details
   */
  private String showMicroLoans(Customer customer) {
    StringBuilder loansStrBuild = new StringBuilder("Available Microloans\n");
    loansStrBuild.append(new String(new char[loansStrBuild.length() - 1]).replace("\0", "#"));
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
		account.addTransaction("MicroLoan: " + amount);
		microLoan.setAmount(amount);
		microLoan.assignToReceiver(customer);
		return String.format("Successfully claimed a microloan for %.2f", amount);
	}

  /**
   * Revokes a microloan the specified customer has offered and which has not been taken yet.
   *
   * @param customer The customer the microloan offered
   * @param request  The revoke microloan request as recorded from the CLI interface
   * @return "SUCCESS" if the pay request has been completed successfully. An error message will be
   * returned otherwise
   */
  private String revokeMicroLoan(Customer customer, String request) {
    String[] requestParameterArr = request.split(" ");
    //expected request format: REVOKEMICROLOAN <SourceCustomerAccount> <Amount>
    if (requestParameterArr.length != 3) {
      return String.format(
          "Expected the following format for the offer micro loan command:\n\n" +
              "REVOKEMICROLOAN <MicroLoanId> <Account>\n\n" +
              "but the number of parameters found after REVOKEMICROLOAN is %d",
          requestParameterArr.length - 1
      );
    }

    //check if account exists
    String accountName = requestParameterArr[2];
    Account account = customer.getAccount(accountName);
    if (account == null) {
      return String.format("Account \"%s\" was not found", accountName);
    }

    //check if the micro loan is actually offered by the customer and is not yet taken
    String uuidString = requestParameterArr[1];
    UUID uuid = null;
    try {
      uuid = UUID.fromString(uuidString);
    } catch (IllegalArgumentException iae) {

    }
    MicroLoan loan = null;
    if (uuid != null) {
      loan = this.microLoans.getOrDefault(uuid, null);
    }
    if (loan == null) {
      return String.format("Micro loan was not found: \"%s\"", uuid);
    }
    if (!loan.isAvailable) {
      return String.format("Micro loan is already taken by: \"%s\"", loan.getReceiver());
    }

    //revoke the micro loan
    account.setBalance(account.getBalance() + loan.getAmount());
    this.microLoans.remove(uuid);

    return "SUCCESS";
  }
}
