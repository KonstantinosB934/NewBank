package newbank.server;

import java.util.UUID;

/**
 * Microloan implementation.
 * Instance attributes:
 * 	- microLoanID: 	The unique identifier for the microloan.
 * 	- amount:		The amount of the loan
 * 	- owner:		The owner of the loan
 * 	- receiver:		The receiver of the loan
 * 	- isAvailable:	Flag to show if the loan is available to be taken. If a receiver
 * 					has been assigned, a loan should no longer be available
 */
public class MicroLoan {

	MicroLoanID microLoanID;
	double amount;
	Customer owner;
	Customer receiver;
	boolean isAvailable;

	public MicroLoan(Customer owner, double amount) {
		this.amount = amount;
		this.microLoanID = new MicroLoanID();
		this.owner = owner;
		this.isAvailable = true;
		this.receiver = null;
	}

	/**
	 * Assign the loan to a receiver customer
	 * @param receiver The customer that receives the loan
	 */
	public void assignToReceiver(Customer receiver) {
		this.receiver = receiver;
		this.isAvailable = false;
	}

	/** Getter for the ID attribute */
	public UUID getID() { return this.microLoanID.getKey(); }

	/** Getter for the amount attribute */
	public double getAmount() { return this.amount; }

	/** Getter for the owner attribute */
	public Customer getOwner() { return this.owner; }

	/** Getter for the owner attribute */
	public Customer getReceiver() { return this.receiver; }

	/** Getter for the isAvailable attribute */
	public boolean getAvailability() { return this.isAvailable; }

	/** Setter for the isAvailable attribute */
	public void setAmount(double amount) { this.amount = amount; }

}
