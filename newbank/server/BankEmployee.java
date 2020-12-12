package newbank.server;

public class BankEmployee extends User {

  public BankEmployee(String password) {
    super(password);
  }

  public BankEmployee(String password, String firstName, String lastName) {
    super(password, firstName, lastName);
  }
}
