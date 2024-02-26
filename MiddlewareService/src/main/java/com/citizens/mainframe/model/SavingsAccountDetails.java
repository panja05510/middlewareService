package com.citizens.mainframe.model;

public class SavingsAccountDetails {
	private String accountNumber;
	private String withdrawalType;
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getWithdrawalType() {
		return withdrawalType;
	}
	public void setWithdrawalType(String withdrawalType) {
		this.withdrawalType = withdrawalType;
	}
	@Override
	public String toString() {
		return "SavingsAccountDetails [accountNumber=" + accountNumber + ", withdrawalType=" + withdrawalType + "]";
	}
	
	

}
