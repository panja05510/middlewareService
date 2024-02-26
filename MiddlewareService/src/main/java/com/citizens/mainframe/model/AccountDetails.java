package com.citizens.mainframe.model;

public class AccountDetails {
	
	private String amount;
	private String  balanceType ;
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getBalanceType() {
		return balanceType;
	}
	public void setBalanceType(String balanceType) {
		this.balanceType = balanceType;
	}
	@Override
	public String toString() {
		return "AccountDetails [amount=" + amount + ", balanceType=" + balanceType + "]";
	}
	

}
