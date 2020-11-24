package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Customer extends User {
	
	private ArrayList<Account> accounts;
	private String passwordUpdate;

	public Customer(String password){
		super(password);
		accounts = new ArrayList<>();
		this.passwordUpdate = password;
	}

	@Override
	public String getPassword(){
		return passwordUpdate;
	}

	@Override
	public void setPassword(String newPassword){
		passwordUpdate = newPassword;
	}

	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString();
		}
		return s;
	}

	public String addAccount(Account account) {
		accounts.add(account);
		return "New Account successfully created.";
	}

	public String update(String newPassword, String confirmPassword){
			if(newPassword.matches(confirmPassword)){
				setPassword(newPassword);
				return "Your password has been successfully updated";
			}

		return "Fail, Please try again";
	}
	
	public void getBalance(Account account) {
		account.getBalance();
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
