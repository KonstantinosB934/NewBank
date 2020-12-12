package newbank.server;

import java.util.UUID;

/**
 * Implements functionality of a unique ID for the microloan class. Essentially a wrapper around
 * UUID, but allows for customisation according to microloan specific behaviour
 */
public class MicroLoanID {

  private final UUID key;

  public MicroLoanID() {
    this.key = UUID.randomUUID();
  }

  public MicroLoanID(String uuidString) {
    this.key = UUID.fromString(uuidString);
  }

  /**
   * Get the unique UUID key
   */
  public UUID getKey() {
    return this.key;
  }

  public String toString() {
    return this.key.toString();
  }

}
