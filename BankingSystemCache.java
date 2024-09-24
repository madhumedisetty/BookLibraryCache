package Banking_Cache;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*; // For concurrent collections and thread management
import java.util.concurrent.atomic.AtomicInteger; // For atomic counters

public class BankingSystemCache {
    
    // Cache for storing bank accounts
    private final Map<Integer, BankAccount> cache = new ConcurrentHashMap<>();
    // Simulated database for storing bank accounts
    private final Map<Integer, BankAccount> dataBase = new HashMap<>();
    // Maximum size of the cache
    private final int cache_Size = 3;
    // Counters for cache hits and misses
    private final AtomicInteger cache_hits = new AtomicInteger(0);
    private final AtomicInteger cache_Misses = new AtomicInteger(0);
    
    // Inner class representing a bank account
    private static class BankAccount {
        private int id; // Account ID
        private int acc_no; // Account number
        private String name; // Account holder name
        private double balance; // Account balance
        
        // Constructor for BankAccount
        BankAccount(int id, int acc_no, String name, double balance) {
            this.id = id;
            this.acc_no = acc_no;
            this.name = name;
            this.balance = balance;
        }

        // Method to get account holder's name
        public String getName() {
            return name;
        }

        // Method to get account balance
        public double getBalance() {
            return balance;
        }

        // Synchronized method to withdraw money
        public synchronized void withDraw(int amt) {
            if (balance >= amt) {
                System.out.println("Withdrawn amount: " + amt);
                balance -= amt;
                System.out.println("Remaining balance: " + balance + "\n");
            } else {
                System.out.println("Insufficient Funds" + "\n");
            }
        }

        // Synchronized method to deposit money
        public synchronized void Deposit(int amt) {
            System.out.println("Deposited amount: " + amt);
            balance += amt;
            System.out.println("After deposit, the balance is: " + balance + "\n");
        }

        // Override toString for account representation
        @Override
        public String toString() {
            return "Id: " + id + ", Account Number: " + acc_no + ", Account Holder: " + name + ", Balance: " + balance + "\n";
        }
    }

    // Method to print cache statistics
    public void getCacheStatistics() {
        System.out.println("Cache Hits: " + cache_hits.get());
        System.out.println("Cache Misses: " + cache_Misses.get());
        System.out.println("Cache Size: " + cache.size());
        System.out.println("Cache Capacity: " + cache_Size);
        System.out.println("Cache Efficiency: " + ((double) cache_hits.get() / (cache_hits.get() + cache_Misses.get()) * 100) + "%");
        System.out.println("Cache Hit Ratio: " + ((double) cache_hits.get() / (cache_hits.get() + cache_Misses.get()) * 100) + "%");
        System.out.println("Cache Miss Ratio: " + ((double) cache_Misses.get() / (cache_hits.get() + cache_Misses.get()) * 100) + "%");
    }

    // Method to add an account to the cache
    private void addToCache(int id, BankAccount account) {
        while (cache.size() >= cache_Size) {
            removeCacheElement(); // Remove an element if cache is full
        }
        cache.put(id, account); // Add account to cache
    }

    // Method to remove an element from the cache (FIFO)
    private void removeCacheElement() {
        int id = cache.keySet().iterator().next(); // Get an arbitrary key
        cache.remove(id); // Remove the account from cache
    }

    // Method to get an account by ID
    public BankAccount getAccount(int id) {
        BankAccount account = cache.get(id); // Check cache for account
        if (account != null) {
            cache_hits.incrementAndGet(); // Increment cache hit counter
            System.out.println("Cache Hit: Account Found: " + id);
            return account; // Return cached account
        } else {
            cache_Misses.incrementAndGet(); // Increment cache miss counter
            System.out.println("Cache Miss: Account Not Found: " + id);
            BankAccount accdb = dataBase.get(id); // Retrieve from database
            if (accdb != null)
                addToCache(id, accdb); // Add to cache if found
            return accdb; // Return the account (or null if not found)
        }
    }

    // Constructor to initialize the database with accounts
    public BankingSystemCache() {
        dataBase.put(1, new BankAccount(1, 1, "Vishnu", 24000));
        dataBase.put(2, new BankAccount(2, 2, "Udaya", 23450));
        dataBase.put(3, new BankAccount(3, 3, "Lalitha", 23540));
        dataBase.put(4, new BankAccount(4, 4, "Madhuri", 42350));
        dataBase.put(5, new BankAccount(5, 5, "Anushka", 54320));
    }

    // Main method to simulate bank operations
    public static void main(String[] args) {
        BankingSystemCache bank = new BankingSystemCache(); // Create bank system instance
        ExecutorService executor = Executors.newFixedThreadPool(5); // Thread pool for concurrent operations
        System.out.println("Starting Bank Operations: ");
        int[] arr = {1, 2, 3, 4, 5}; // Array of account IDs
        
        for (int i = 0; i <= 10; i++) {
            System.out.println("=============== Loop " + i + " ===============\n");
            int id = arr[new Random().nextInt(arr.length)]; // Randomly select an account ID
            int random = new Random().nextInt(10); // Random operation choice
            
            executor.execute(() -> {
                try {
                    if (random % 2 == 0) { // Even: Deposit
                        BankAccount acc = bank.getAccount(id);
                        if (acc != null) {
                            acc.Deposit(1000); // Deposit money
                            System.out.println(acc);
                        } else {
                            System.out.println("Account not found\n");
                        }
                    } else { // Odd: Withdraw
                        BankAccount acc = bank.getAccount(id);
                        if (acc != null) {
                            acc.withDraw(300); // Withdraw money
                            System.out.println(acc);
                        } else {
                            System.out.println("Account not found\n");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Handle exceptions
                } finally {
                    try {
                        executor.awaitTermination(1, TimeUnit.SECONDS); // Wait for thread termination
                    } catch (InterruptedException e) {
                        System.out.println("There was an interruption");
                        e.printStackTrace();
                    }
                }
            });
        }
        
        try {
            Thread.sleep(5000); // Allow time for operations to complete
        } catch (Exception e) {
            System.out.println("Error during sleep");
            e.printStackTrace();
        }
        
        bank.getCacheStatistics(); // Print cache statistics
        executor.shutdown(); // Shutdown the executor
    }
}
