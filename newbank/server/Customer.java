package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
	
	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString();
		}
		return s;
	}

	public void addAccount(Account account) {
		accounts.add(account);		
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
