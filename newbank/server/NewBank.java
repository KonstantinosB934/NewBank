package newbank.server;

import java.util.HashMap;

public class NewBank {

	private static final NewBank bank = new NewBank();
	private final HashMap<String,User> users;

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

				if (request.startsWith("PAY")) {
					return payAmountToOtherCustomer(customer, request);
				}

				if ("SHOWMYACCOUNTS".equals(request)) {
					return showMyAccounts(customer);
				}
				return "FAIL";

			} else if (user instanceof BankEmployee) {
				BankEmployee employee = (BankEmployee)user;
				if(request.startsWith("DELETECUSTOMER")) {
					return deleteCustomer(request);
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
	
	private String deleteCustomer(String request) {
		String[] deleteCommand = request.split(" ");
		
		String customerName = deleteCommand[1];
		
		if(users.containsKey(customerName)) {
			User user = users.get(customerName);
			
			if (user instanceof Customer) {
				users.remove(customerName);
				return "SUCCESS";
			}else {
				return "FAIL";
			}
		}else {
			return "FAIL";
		}
	}
  
	private String deleteAccount(Customer customer, String request) {
		String[] deleteCommand = request.split(" ");
		String myAccount = deleteCommand[1];
		return customer.delete(myAccount);
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
	private String payAmountToOtherCustomer(Customer customer, String request) {
		// todo: Get all parameters from the request
		// todo: Check if receiving customer and account exists and fail if not
		// todo: Check if the account has sufficient funds and fail if not
		// todo: Finally, if all is OK, then just move amount from one account into the account of the
		return "SUCCESS";
	}

}
