package newbank.server;

import java.util.ArrayList;

public class Customer extends User {
	
	private ArrayList<Account> accounts;
	private String passwordUpdate;

	private String billingAddress;
	private String deliveryAddress;

	private boolean frozen = false;

	//bitcoin wallet
	private BitcoinWallet btcWallet;

	public Customer(String password){
		super(password);
		accounts = new ArrayList<>();
		this.passwordUpdate = password;
	}

	public Customer(String password, String firstName, String lastName){
		super(password, firstName, lastName);
		accounts = new ArrayList<>();
		this.passwordUpdate = password;
	}

	@Override
	public String getPassword(){ return passwordUpdate; }

	@Override
	public void setPassword(String newPassword){ passwordUpdate = newPassword; }

	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public boolean freeze() {
		this.frozen = !this.frozen;
		return this.frozen;
	}

	public String accountsToString() {
		StringBuilder s = new StringBuilder();
		for(Account a : accounts) {
			s.append(a.toString());
			s.append("\n");
		}
		if (this.getBtcWallet() != null) {
			s.append(this.getBtcWallet().toString());
			s.append("\n");
		}
		return s.toString();
	}

	public String addAccount(Account account) {
		accounts.add(account);
		return "New Account successfully created.";
	}

	/**
	 * Search for account by name and return if found.
	 * @param accountName The account name to search for
	 * @return The account if found, null otherwise
	 */
	public Account getAccount(String accountName) {
		for (Account account : accounts) {
			if (account.getName().equals(accountName)) {
				return account;
			}
		}
		return null;
	}

	/** Return all the customer accounts*/
	public ArrayList<Account> getAllAccounts() { return this.accounts; }


	public String update(String newPassword, String confirmPassword){
			if(newPassword.matches(confirmPassword)){
				setPassword(newPassword);
				return "Your password has been successfully updated";
			}

		return "Fail, Please try again";
	}

	public String moveMoney(String From, String To, Double Amount) {

		if(this.frozen) {
			return "Your account is frozen";
		} else {
			for(Account F : accounts) {
				if(F.getName().equals(From)) {
					for(Account T : accounts) {
						if(T.getName().equals(To)) {
							F.setBalance(F.getBalance()-Amount);
							T.setBalance(T.getBalance()+Amount);
							return "Money moved successfully";
						}
					}
				}
			}
			return "Money has not been moved";
		}
	}

	public String donateMoney(String from, Double amount){
		for(Account f: accounts){
			if(f.getName().equals(from)){
				if(amount < f.getBalance()) {
					f.setBalance(f.getBalance() - amount);
					return String.format("Your donation of %.2f has been successfully processed thank you!", amount);
				} else
					return "Insufficient funds for this transaction: "+f.getBalance();
			}
		}
		return "FAIL";
	}

	public String delete(String myAccount) {
		
		for(Account A : accounts) {
			if(A.getName().equals(myAccount)) {
				accounts.remove(A);
				return "SUCCESS";
			}
		}

		return "FAIL";
	}

	@Override
	public String toString() {
		return  "{Firstname: " + getFirstName() + ", Lastname: " + getLastName() +
				", Billing Address: " + billingAddress +
				", Delivery Address: " + deliveryAddress + "}";
	}

	/**
	 * Returns the bitcoin wallet or null when there is none.
	 *
	 * @return The bitcoin wallet property
	 */
	public BitcoinWallet getBtcWallet() {
		return this.btcWallet;
	}

	/**
	 * Creates a new bitcoin wallet
	 */
	public void createBtcWallet() {
		this.btcWallet = new GBPBitcoinWallet();
	}
}
