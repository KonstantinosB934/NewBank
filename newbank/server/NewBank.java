package newbank.server;

import java.util.HashMap;

public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;
	
	private NewBank() {
		customers = new HashMap<>();
		addTestData();
	}
	
	private void addTestData() {
		Customer bhagy = new Customer("123");
		bhagy.addAccount(new Account("Main", 1000.0));
		bhagy.addAccount(new Account("Savings", 2000.0));
		customers.put("Bhagy", bhagy);

		
		Customer christina = new Customer("555");
		christina.addAccount(new Account("Savings", 1500.0));
		customers.put("Christina", christina);

		Customer john = new Customer("101");
		john.addAccount(new Account("Checking", 250.0));
		customers.put("John", john);
	}
	
	public static NewBank getBank() {
		return bank;
	}

	
	public synchronized CustomerID checkLogInDetails(String userName, String password) {
		if(customers.containsKey(userName)) {
			Customer customer = customers.get(userName);
			if(customer.getPassword().equals(password)){
				return new CustomerID(userName);
			}
		}
		return null;
	}

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(CustomerID customer, String request) {
		if(customers.containsKey(customer.getKey())) {

			if(request.startsWith("MOVE")) {
				return moveMoney(customer, request);
			}

			if(request.startsWith("UPDATE")){
				return update(customer, request);
			}

			if(request.startsWith("NEWACCOUNT")){
				return createAcc(customer, request);
			}

			switch(request) {
			case "SHOWMYACCOUNTS" : return showMyAccounts(customer);

			default : return "FAIL";
			}
		}
		return "FAIL";
	}
	
	private String showMyAccounts(CustomerID customer) {
		return (customers.get(customer.getKey())).accountsToString();
	}
	
	private String moveMoney(CustomerID customer, String request) {
		String[] movecommand = request.split(" ");
		
		String From = movecommand[2];
		String To = movecommand[3];
		Double Amount = Double.parseDouble(movecommand[1]);
				
		return (customers.get(customer.getKey())).moveMoney(From, To, Amount);
	}

	private String moveMoney(CustomerID customer, String request) {
		String[] movecommand = request.split(" ");

		String From = movecommand[2];
		String To = movecommand[3];
		Double Amount = Double.parseDouble(movecommand[1]);

		return (customers.get(customer.getKey())).moveMoney(From, To, Amount);
	}

	private String update(CustomerID customer, String request){
		String[] updateCommand = request.split(" ");

		String newPassword = updateCommand[1];
		String confirmPassword = updateCommand[2];


		return (customers.get(customer.getKey())).update(newPassword,confirmPassword);
	}

	private String createAcc(CustomerID customer, String request){
		String[] createCommand = request.split(" ");

		String accountName = createCommand[1];
		double openingBalance = Double.parseDouble(createCommand[2]);


		return(customers.get(customer.getKey())).addAccount(new Account (accountName, openingBalance));
	}

}
