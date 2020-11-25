package newbank.server;

import java.util.ArrayList;

public class Customer extends User {
	
	private ArrayList<Account> accounts;
	private String passwordUpdate;

	private String billingAddress;
	private String deliveryAddress;

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

	public String accountsToString() {
		StringBuilder s = new StringBuilder();
		for(Account a : accounts) {
			s.append(a.toString());
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

	public String update(String newPassword, String confirmPassword){
			if(newPassword.matches(confirmPassword)){
				setPassword(newPassword);
				return "Your password has been successfully updated";
			}

		return "Fail, Please try again";
	}

	public String moveMoney(String From, String To, Double Amount) {
		for(Account F : accounts) {
			if(F.getName().equals(From)) {
				for(Account T : accounts) {
					if(T.getName().equals(To)) {
						F.setBalance(F.getBalance()-Amount);
						T.setBalance(T.getBalance()+Amount);
						return "SUCCESS";
					}
				}
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
		return  "Your details are :" + "\n" + getFirstName() + " " + getLastName() + " " +
				"Billing Address: " + " " + billingAddress +
				"Delivery Address: " + " " + deliveryAddress;
	}
}
