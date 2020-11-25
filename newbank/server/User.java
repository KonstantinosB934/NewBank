package newbank.server;

public abstract class User {

  protected String password;
  private String firstName;
  private String lastName;

  public User(String password){
    this.password = password;
  }

  public User(String password, String firstName, String lastName){
    this(password);
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getPassword(){
    return password;
  }

  public void setPassword(String newPassword){
    password = newPassword;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}
