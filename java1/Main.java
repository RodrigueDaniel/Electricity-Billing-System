package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static Connection connection = null;
    public static List<Customer> customerList = new ArrayList<>();
    public static int customerIdCounter = 0; 
    public static int billNumberCounter = 1;
    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        connection = DatabaseConnector.getConnection();
        if (connection == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }
        boolean exit = false;

        while (!exit) {
            System.out.println("\n1. Add Customer");
            System.out.println("2. View Customer List");
            System.out.println("3. Enter Bill Details");
            System.out.println("4. View Customer Details");
            System.out.println("5. View Unpaid Bills");
            System.out.println("6. Calculate Total Amount Due");
            System.out.println("7. Pay Bill");
            System.out.println("8. Update Customer Details");
            System.out.println("9. Update Bill Details");
            System.out.println("10. Delete Bill");
            System.out.println("11. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1:
                    addCustomer();
                    break; 
                case 2:
                    viewCustomerList();
                    break;
                case 3:
                    enterBillDetails();
                    break;
                case 4:
                    viewCustomerDetails();
                    break;
                case 5:
                    viewUnpaidBills();
                    break;
                case 6:
                    calculateTotalAmountDue();
                    break;
                case 7:
                    payBill();
                    break;
                case 8:
                    updateCustomerDetails();
                    break;
                case 9:
                    updateBillDetails();
                    calculateTotalAmountDue();
                    break;
                case 10:
                    deleteBill();
                    break;
                case 11:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 11.");
            }
        }

        sc.close();
    }

    public static void addCustomer() {
        System.out.print("Enter customer name: ");
        String cusName = sc.nextLine();

        System.out.print("Enter customer address: ");
        String cusAddress = sc.nextLine();

        Customer customer = new Customer(cusName, customerIdCounter++, cusAddress);
        customerList.add(customer);
        System.out.println("Customer added successfully.");
    }

    public static void viewCustomerList() {
        System.out.println("\nCustomer List:");
        if (customerList.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            for (Customer customer : customerList) {
                System.out.println("Customer ID: " + customer.getId());
            }
        }
    }

    public static void enterBillDetails() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter house number: ");
        int houseNumber = sc.nextInt();

        System.out.print("Enter unit rate: ");
        double unitRate = sc.nextDouble();

        System.out.print("Enter units consumed: ");
        int unitsConsumed = sc.nextInt();

        Bill bill = new Bill(billNumberCounter++, houseNumber, unitRate, unitsConsumed);
        customer.addBill(bill);
        System.out.println("Bill added successfully.");
    }

    public static void viewCustomerDetails() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
        } else {
            System.out.println("\nCustomer Details:");
            customer.getCustomerDetails();
        }
    }

    public static void viewUnpaidBills() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        List<Bill> unpaidBills = customer.getUnpaidBills();
        if (unpaidBills.isEmpty()) {
            System.out.println("No unpaid bills found for this customer.");
        } else {
            System.out.println("Unpaid Bills:");
            for (Bill bill : unpaidBills) {
                System.out.println("Bill Number: " + bill.getBillNumber());
            }
        }
    }

    public static void calculateTotalAmountDue() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        double totalAmountDue = customer.calculateTotalAmountDue();
        System.out.println("Total Amount Due for Customer " + customer.getId() + ": Rs" + totalAmountDue);
    }

    public static void payBill() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline
    
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }
    
        System.out.print("Enter bill number to pay: ");
        int billNumber = sc.nextInt();
        sc.nextLine(); // Consume newline
    
        Bill bill = findBillByNumber(customer, billNumber);
        if (bill == null) {
            System.out.println("Bill not found.");
            return;
        } else if (bill.isPaid()) {
            System.out.println("Bill is already paid.");
            return;
        }
    
        double totalAmountDue = customer.calculateTotalAmountDue();
        double penalty = 0.0;
    
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isAfter(bill.getDueDate())) {
            penalty = Penalty.calculatePenalty(totalAmountDue, true);
            System.out.println("Penalty added: Rs" + penalty);
        }
    
        double totalAmountPaid = bill.getTotalAmount() + penalty;
        bill.markAsPaid();
        customer.markAsPaid();
    
        System.out.println("Bill paid successfully. Total Amount Paid: Rs" + totalAmountPaid);
    }

    public static void updateCustomerDetails() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter new name: ");
        String newName = sc.nextLine();

        System.out.print("Enter new address: ");
        String newAddress = sc.nextLine();

        customer.updateDetails(newName, newAddress);
        System.out.println("Customer details updated successfully.");
    }

    public static void updateBillDetails() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter bill number to update: ");
        int billNumber = sc.nextInt();
        sc.nextLine(); // Consume newline

        Bill bill = findBillByNumber(customer, billNumber);
        if (bill == null) {
            System.out.println("Bill not found.");
            return;
        }

        System.out.print("Enter new unit rate: ");
        double newUnitRate = sc.nextDouble();

        System.out.print("Enter new units consumed: ");
        int newUnitsConsumed = sc.nextInt();

        bill.updateBillDetails(newUnitRate, newUnitsConsumed);
        System.out.println("Bill details updated successfully.");
    }

    public static void deleteBill() {
        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter bill number to delete: ");
        int billNumber = sc.nextInt();
        sc.nextLine(); // Consume newline

        Bill bill = findBillByNumber(customer, billNumber);
        if (bill == null) {
            System.out.println("Bill not found.");
            return;
        }

        customer.getBillHistory().remove(bill);
        System.out.println("Bill deleted successfully.");
    }

    public static Customer findCustomerById(int customerId) {
        for (Customer customer : customerList) {
            if (customer.getId() == customerId) { // Correct comparison
                return customer;
            }
        }
        return null;
    }

    public static Bill findBillByNumber(Customer customer, int billNumber) {
        for (Bill bill : customer.getBillHistory()) {
            if (bill.getBillNumber() == billNumber) {
                return bill;
            }
        }
        return null;
    }
}

class Customer {
    public String cus_name;
    public int cus_id;
    public String cus_address;
    public List<Bill> bills;
    public boolean hasPaid;

    public Customer(String cus_name, int cus_id, String cus_address) {
        this.cus_name = cus_name;
        this.cus_id = cus_id;
        this.cus_address = cus_address;
        this.bills = new ArrayList<>();
        this.hasPaid = false;
    }

    public void getCustomerDetails() {
        System.out.println("Customer Name: " + cus_name);
        System.out.println("Customer ID: " + cus_id);
        System.out.println("Customer Address: " + cus_address);
        System.out.println("Payment Status: " + (hasPaid ? "Paid" : "Not Paid"));
    }

    public int getId() {
        return cus_id;
    }

    public void addBill(Bill bill) {
        bills.add(bill);
    }

    public List<Bill> getBillHistory() {
        return bills;
    }

    public List<Bill> getUnpaidBills() {
        List<Bill> unpaidBills = new ArrayList<>();
        for (Bill bill : bills) {
            if (!bill.isPaid()) {
                unpaidBills.add(bill);
            }
        }
        return unpaidBills;
    }

    public double calculateTotalAmountDue() {
        double totalAmount = 0;
        for (Bill bill : bills) {
            if (!bill.isPaid()) {
                totalAmount += bill.generateBill();
            }
        }
        return totalAmount;
    }

    public void markAsPaid() {
        hasPaid = true;
    }

    public boolean hasPaid() {
        return hasPaid;
    }

    public void updateDetails(String newName, String newAddress) {
        this.cus_name = newName;
        this.cus_address = newAddress;
    }
}

class Bill {
    public static final double FIXED_CHARGE = 20.0;
    public int billNumber;
    public int houseNumber;
    public double unitRate;
    public int unitsConsumed;
    public double totalAmount;
    public LocalDate dueDate;
    public boolean isPaid;

    public Bill(int billNumber, int houseNumber, double unitRate, int unitsConsumed) {
        this.billNumber = billNumber;
        this.houseNumber = houseNumber;
        this.unitRate = unitRate;
        this.unitsConsumed = unitsConsumed;
        this.totalAmount = unitRate * unitsConsumed;
        this.dueDate = LocalDate.now().plusDays(30); // Due date is set to 30 days from the current date
        this.isPaid = false;
    }

    public int getBillNumber() {
        return billNumber;
    }

    public double generateBill() {
        return (unitsConsumed * unitRate) + FIXED_CHARGE;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public double getUnitRate() {
        return unitRate;
    }

    public int getUnitsConsumed() {
        return unitsConsumed;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void markAsPaid() {
        isPaid = true;
    }

    public void updateBillDetails(double newUnitRate, int newUnitsConsumed) {
        unitRate = newUnitRate;
        unitsConsumed = newUnitsConsumed;
        totalAmount = unitRate * unitsConsumed;
    }
}

class Penalty {
    public static final double LATE_PAYMENT_FEE = 10.0;
    public static final double FINE_PER_UNIT = 5.0;

    public static double calculatePenalty(double totalAmountDue, boolean isLatePayment) {
        double penalty = 0;
        if (isLatePayment) {
            penalty += LATE_PAYMENT_FEE;
        }
        if (totalAmountDue > 0) {
            penalty += totalAmountDue * FINE_PER_UNIT;
        }
        return penalty;
    }
}

class DatabaseConnector {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/electricity";
    public static final String USER = "root"; // Modify with your database username
    public static final String PASSWORD = ""; // Modify with your database password

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}