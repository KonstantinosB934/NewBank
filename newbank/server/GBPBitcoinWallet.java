package newbank.server;

import java.util.Random;

/***
 * Bitcoin wallet for handling GBP transactions that also determines exchange rates
 */
public class GBPBitcoinWallet extends BitcoinWallet {

  /**
   * Method which implements an abstract method that determines the current exchange rate
   * of the subclass currency (GBP in this subclass) to BTC.
   *
   * @return The amount of BTC per GBP
   */
  @Override
  public double getExchangeRate() {
    double base = 0.000069;
    int factor = new Random().nextInt(21) + 1;
    return  base * factor;
  }
}
