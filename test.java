import java.util.HashMap;
import java.util.Map;
 
public class BankingSystem {
    private Map<String, Double> accounts;
 
    public BankingSystem() {
        accounts = new HashMap<>();
    }
 
    public void createAccount(String accountNumber, double initialBalance) {
        accounts.put(accountNumber, initialBalance);
        System.out.println("Account created successfully for account number: " + accountNumber);
    }
 
    public void deposit(String accountNumber, double amount) {
        if (accounts.containsKey(accountNumber)) {
            double currentBalance = accounts.get(accountNumber);
            accounts.put(accountNumber, currentBalance + amount);
            System.out.println(amount + " deposited successfully to account number: " + accountNumber);
        } else {
            System.out.println("Error: Account not found for account number: " + accountNumber);
        }
    }
 
    public void withdraw(String accountNumber, double amount) {
        if (accounts.containsKey(accountNumber)) {
            double currentBalance = accounts.get(accountNumber);
            if (currentBalance >= amount) {
                accounts.put(accountNumber, currentBalance - amount);
                System.out.println(amount + " withdrawn successfully from account number: " + accountNumber);
            } else {
                System.out.println("Error: Insufficient balance in account number: " + accountNumber);
            }
        } else {
            System.out.println("Error: Account not found for account number: " + accountNumber);
        }
    }
 
    public void transfer(String fromAccount, String toAccount, double amount) {
        if (accounts.containsKey(fromAccount) && accounts.containsKey(toAccount)) {
            double fromBalance = accounts.get(fromAccount);
            double toBalance = accounts.get(toAccount);
            if (fromBalance >= amount) {
                accounts.put(fromAccount, fromBalance - amount);
                accounts.put(toAccount, toBalance + amount);
                System.out.println(amount + " transferred successfully from account number " + fromAccount +
                        " to account number " + toAccount);
            } else {
                System.out.println("Error: Insufficient balance in account number: " + fromAccount);
            }
        } else {
            System.out.println("Error: One or both of the accounts not found");
        }
    }
 
    public static void main(String[] args) {
        BankingSystem bankingSystem = new BankingSystem();
        bankingSystem.createAccount("1234567890", 1000.0);
        bankingSystem.createAccount("0987654321", 500.0);
        bankingSystem.deposit("1234567890", 200.0);
        bankingSystem.withdraw("1234567890", 100.0);
        bankingSystem.transfer("1234567890", "0987654321", 300.0);
        // Attempting invalid operations
        bankingSystem.deposit("9999999999", 100.0);
        bankingSystem.withdraw("1234567890", 1500.0);
        bankingSystem.transfer("1234567890", "9999999999", 2000.0);
    }
}
