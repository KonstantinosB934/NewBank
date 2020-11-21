package newbank.server;

import java.util.HashMap;

public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,User> users;
	
	private NewBank() {
		users = new HashMap<>();
		addTestData();
	}
	
	private void addTestData() {
		Customer bhagy = new Customer("123");
		bhagy.addAccount(new Account("Main", 1000.0));
		bhagy.addAccount(new Account("Savings", 2000.0));
		users.put("Bhagy", bhagy);


		Customer christina = new Customer("555");
		christina.addAccount(new Account("Savings", 1500.0));
		users.put("Christina", christina);

		Customer john = new Customer("101");
		john.addAccount(new Account("Checking", 250.0));
		users.put("John", john);

		BankEmployee max = new BankEmployee("123456");
		users.put("Max", max);
	}
	
	public static NewBank getBank() {
		return bank;
	}


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
	public synchronized String processRequest(UserID userId, String request) {
		if(users.containsKey(userId.getKey())) {
			User user = users.get(userId.getKey());

			if (user instanceof Customer) {
				Customer customer = (Customer)user;
				if (request.startsWith("MOVE")) {
					return moveMoney(customer, request);
				}

				if (request.startsWith("UPDATE")) {
					return update(customer, request);
				}

				if (request.startsWith("NEWACCOUNT")) {
					return createAcc(customer, request);
				}
				
				if (request.startsWith("DELETEACCOUNT")) {
					return deleteAccount(customer, request);
				}

				switch (request) {
					case "SHOWMYACCOUNTS":
						return showMyAccounts(customer);
					default:
						return "FAIL";
				}
			} else if (user instanceof BankEmployee) {
				//todo: bank employee protocol
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

	private String update(Customer customer, String request){
		String[] updateCommand = request.split(" ");

		String newPassword = updateCommand[1];
		String confirmPassword = updateCommand[2];

		return customer.update(newPassword,confirmPassword);
	}

	private String createAcc(Customer customer, String request){
		String[] createCommand = request.split(" ");

		String accountName = createCommand[1];
		double openingBalance = Double.parseDouble(createCommand[2]);

		return customer.addAccount(new Account (accountName, openingBalance));
	}
	
	private String deleteAccount(Customer customer, String request) {
		String[] deleteCommand = request.split(" ");
		String myAccount = deleteCommand[1];

		return customer.delete(myAccount);
	}
}
