package newbank.server;

/***
 * Represents an abstract Bitcoin wallet
 */
public abstract class BitcoinWallet {

  public double getBitcoins() {
    return bitcoins;
  }

  private double bitcoins;

  public BitcoinWallet() {
    this.bitcoins = 0.0;
  }

  public abstract double getExchangeRate();

  public boolean changeBitcoin(double bitcoinChange) {
    if (this.bitcoins + bitcoinChange < 0.0) {
      return false;
    }
    this.bitcoins += bitcoinChange;
    return true;
  }

  public double getBtcEquivalent(double baseCurrency) {
    return baseCurrency * this.getExchangeRate();
  }

  @Override
  public String toString() {
    return String.format("{bitcoins: %s }", this.bitcoins);
  }
}
