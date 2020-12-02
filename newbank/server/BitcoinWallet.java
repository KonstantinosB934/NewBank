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

  /**
   * Abstract method that determines the exchange rate of bitcoin and a currency specified
   * in subclasses.
   *
   * @return The exchange rate 1 subclass currency equals [returnValue] bitcoins
   */
  public abstract double getExchangeRate();

  public boolean changeBitcoin(double bitcoinChange) {
    if (this.bitcoins + bitcoinChange < 0.0) {
      return false;
    }
    this.bitcoins += bitcoinChange;
    return true;
  }

  /**
   * Calculates the BTC equivalent of a subclass currency.
   *
   * @param baseCurrency The amount of the subclass currency
   * @return The equivalent BTC amount
   */
  public double getBtcEquivalent(double baseCurrency) {
    return baseCurrency * this.getExchangeRate();
  }

  /**
   * Calculates the subclass currency equivalent of a certain amount of bitcoins.
   * @param btcAmount The amount of bitcoins
   * @return The equivalent subclass currency amount
   */
  public double getEquivalentToBtc(double btcAmount) {
    return btcAmount / this.getExchangeRate();
  }

  @Override
  public String toString() {
    return String.format("{bitcoins: %s }", this.bitcoins);
  }
}
