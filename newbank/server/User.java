package newbank.server;

import java.util.ArrayList;

public abstract class User {
  protected String password;

  public User(String password){
    this.password = password;
  }

  public String getPassword(){
    return password;
  }

  public void setPassword(String newPassword){
    password = newPassword;
  }
}
