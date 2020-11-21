package newbank.server;

import java.util.ArrayList;

public class Customer extends User {
	
	private ArrayList<Account> accounts;
	private String passwordUpdate;
	private String lastName;
	private String address;
	private String dateOfBirth;

	public Customer(String password, String lastName, String address, String dateOfBirth){
		super(password);
		accounts = new ArrayList<Account>();
		this.passwordUpdate = password;
		this.lastName = lastName;
		this.address = address;
		this.dateOfBirth = dateOfBirth;
	}


	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
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



	public void getBalance(Account account) {
		account.getBalance();
	}


	public String update(String newPassword, String confirmPassword){

			if(newPassword.matches(confirmPassword)){
				setPassword(newPassword);
				return "Your password has been successfully updated"+getPassword();
			}

		return "Fail, Please try again"+getPassword();
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
