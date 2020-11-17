package newbank.server;

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
			s += "\n";
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
}
