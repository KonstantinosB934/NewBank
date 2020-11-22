package newbank.server;

import java.util.ArrayList;

public class Customer extends User {
	
	private ArrayList<Account> accounts;
	private String passwordUpdate;

	private String foreName;
	private String lastName;
	private String address;
	private String dateOfBirth;

	public Customer(String password){
		super(password);
		accounts = new ArrayList<>();
		this.passwordUpdate = password;
	}

	public Customer(String password, String foreName, String lastName, String address, String dateOfBirth){
		this(password);

		this.foreName = foreName;
		this.lastName = lastName;
		this.address = address;
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

	public String getForeName() {
		return foreName;
	}

	public void setForeName(String foreName) {
		this.foreName = foreName;
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

	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString();
			s += "\n";
		}
		return s.isEmpty() ? "none" : s;
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
}
