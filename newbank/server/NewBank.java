package newbank.server;

import java.util.HashMap;

public class NewBank {

  private static final NewBank bank = new NewBank();
  private final HashMap<String, User> users;

  private NewBank() {
    users = new HashMap<>();
    addTestData();
  }

  public static NewBank getBank() {
    return bank;
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
  public synchronized String processRequest(UserID userId, String request) {
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
          return payAmountToOtherCustomer(customer, request);
        }

        if ("SHOWMYACCOUNTS".equals(request)) {
          return showMyAccounts(customer);
        }

        if ("BITCOINMOVE".equals(request.substring(0, "BITCOINMOVE".length()))) {
          return bitcoinMove(customer, request);
        }

        if ("BITCOINPAY".equals(request.substring(0, "BITCOINPAY".length()))) {
          return bitcoinPay(customer, request);
        }

        return "FAIL";

      } else if (user instanceof BankEmployee) {
        //all bank employee related protocols
        BankEmployee employee = (BankEmployee) user;
        if (request.startsWith("DELETECUSTOMER ")) {
          return deleteCustomer(request);
        } else if (request.startsWith("NEWCUSTOMER")) {
          return addCustomer(request);
        }
      }
    }
    return "FAIL";
  }

  private String showMyAccounts(Customer customer) {
    return customer.accountsToString();
  }

  private String moveMoney(Customer customer, String request) {
    String[] movecommand = request.split(" ");

    String From = movecommand[2];
    String To = movecommand[3];
    Double Amount = Double.parseDouble(movecommand[1]);

    return customer.moveMoney(From, To, Amount);
  }

  private String update(Customer customer, String request) {
    String[] updateCommand = request.split(" ");

    String newPassword = updateCommand[1];
    String confirmPassword = updateCommand[2];

    return customer.update(newPassword, confirmPassword);
  }

  private String createAcc(Customer customer, String request) {
    String[] createCommand = request.split(" ");

    String accountName = createCommand[1];
    double openingBalance = Double.parseDouble(createCommand[2]);

    return customer.addAccount(new Account(accountName, openingBalance));
  }

  private String deleteCustomer(String request) {
    String[] deleteCommand = request.split(" ");

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
  }

  private String deleteAccount(Customer customer, String request) {
    String[] deleteCommand = request.split(" ");
    String myAccount = deleteCommand[1];
    return customer.delete(myAccount);
  }

  /**
   * Pay an amount to another customer of the bank. The outgoing account should be specified and if
   * the receiving customer or account cannot be identified or if there are no sufficient funds,
   * then the request should be rejected
   *
   * @param customer The customer that the pay amount is paid from
   * @param request  The pay request as recorded from the CLI interface
   * @return "SUCCESS" if the pay request has been completed successfully. An error message will be
   * returned otherwise
   */
  private String payAmountToOtherCustomer(Customer customer, String request) {
    String[] requestParameterArr = request.split(" ");
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
    // Finally if all checks are OK, the amount can be transferred
    account.setBalance(account.getBalance() - amount);
    recipientAccount.setBalance(recipientAccount.getBalance() + amount);
    return "SUCCESS";
  }

  private String addCustomer(String request) {
    String[] addCommand = request.split(" ");

    String customerName = addCommand[1];
    String password = addCommand[2];

    if (!users.containsKey(customerName)) {
      Customer customer = new Customer(password);
      users.put(customerName, customer);

      return "You have successfully added '" + customerName + "'";
    } else {
      return "Sorry, customer already exists";
    }

  }

  private String bitcoinMove(Customer customer, String request) {
    if (customer != null && request != null) {
      String[] parameters = request.split(" ");
      //check request format
      if (parameters.length != 3) {
        return String.format(
            "Expected the following format for the BITCOINMOVE command:\n\n" +
                "BITCOINMOVE <SourceCustomerAccount> <Amount>\n\n" +
                "but the number of parameters found after BITCOINMOVE is %d",
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
}
