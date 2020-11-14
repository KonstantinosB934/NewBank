package newbank.server;

public class Account {
	
	private String accountName;
	private double openingBalance;
	private double currentBalance;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.openingBalance = openingBalance;
		this.currentBalance = openingBalance;
	}
	
	public String toString() {
		return (accountName + ": " + currentBalance+"\n");
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

}


