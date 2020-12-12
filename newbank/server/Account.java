package newbank.server;

public class Account {
	
	private String accountName;
	private double openingBalance;
	private double currentBalance;
	private ArrayList<String> transactions;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.openingBalance = openingBalance;
		this.currentBalance = openingBalance;
		this.transactions = new ArrayList<String>();
		addTransaction("opening balance deposit: " + openingBalance);
	}
	
	public String toString() {
		return String.format("{ accountName: %s, currentBalance: %s }", accountName, currentBalance);
	}

	public double getBalance() {
		return currentBalance;
	}

	public void setBalance(double newBalance) {
		currentBalance = newBalance;
	}
	
	public String getName() {
		return accountName;
	} 
	
	public void addTransaction(String amount) {
		this.transactions.add(amount);
	}

	public ArrayList<String> getTransactions() {
		return transactions;
	}

}


