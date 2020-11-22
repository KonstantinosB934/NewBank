package newbank.server;

import java.util.HashMap;


public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,User> users ;
	
	private NewBank() {
		users = new HashMap<>();
		addTestData();
	}
	
	private void addTestData() {
		Customer bhagy = new Customer("123", "Patil", "12 street", "140285");
		bhagy.addAccount(new Account("Main", 1000.0));
		bhagy.addAccount(new Account("Savings", 2000.0));
		users.put("Bhagy", bhagy);


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

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(UserID userId, String request) {
		if(users.containsKey(userId.getKey())) {

			User user = users.get(userId.getKey());

			if (user instanceof Customer) {
				Customer customer = (Customer) user;

				if (request.startsWith("MOVE")) {
					return moveMoney(customer, request);
				}

				if (request.startsWith("UPDATE")) {
					return update(customer, request);
				}

				if (request.startsWith("NEWACCOUNT")) {
					return createAcc(customer, request);
				}



				switch (request) {
					case "SHOWMYACCOUNTS":
						return showMyAccounts(customer);
					default:
						return "FAIL";
				}

			} else if (user instanceof BankEmployee) {
				BankEmployee employee = (BankEmployee)user;
				if(request.startsWith("NEWCUSTOMER")) {
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
		String[] moveCommand = request.split(" ");

		String From = moveCommand[2];
		String To = moveCommand[3];
		Double Amount = Double.parseDouble(moveCommand[1]);

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


	private String addCustomer(String request){
		String[] addCommand = request.split(" ");

		String customerName = addCommand[1];
		String customerSurname = addCommand[2];
		String customerAddress = addCommand[3];
		String customerDOB = addCommand[4];
		String password = addCommand[5];
		String accountName = addCommand[6];
		double amountToDeposit  =  Double.parseDouble(addCommand[7]);



		if(!users.containsKey(customerName)) {
			Customer customer = new Customer(password, customerSurname, customerAddress, customerDOB);
			customer.addAccount(new Account(accountName, amountToDeposit));
			users.put(customerName, customer);

			return "You have successfully added "+customerName+" "+customerSurname;
		}
		else
			return "Sorry, customer already exists";



	}

}
