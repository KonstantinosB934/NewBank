package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	private String password;
	private String passwordUpdate;


	public Customer(String password){
		accounts = new ArrayList<>();
		this.password = password;
		this.passwordUpdate = password;
	}
	public String getPassword(){
		return passwordUpdate;
	}

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



	public void getBalance(Account account) {
		account.getBalance();
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
}
