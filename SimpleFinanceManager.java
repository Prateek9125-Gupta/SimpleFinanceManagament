import java.sql.*;
import java.util.Scanner;

public class SimpleFinanceManager {

    public static Connection connectToDatabase() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "Prateek20/12");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS simple_finance");
            stmt.executeUpdate("USE simple_finance");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255), "
                    + "amount DECIMAL(10, 2), "
                    + "category VARCHAR(100), "
                    + "date DATE)");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            return null;
        }
    }

    public static void addTransaction(Connection conn) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter transaction name: ");
        String name = sc.nextLine();
        System.out.print("Enter amount (positive for income, negative for expense): ");
        double amount = sc.nextDouble();
        sc.nextLine();
        System.out.print("Enter category (e.g., Income, Food, Bills): ");
        String category = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transactions (name, amount, category, date) VALUES (?, ?, ?, ?)");
            stmt.setString(1, name);
            stmt.setDouble(2, amount);
            stmt.setString(3, category);
            stmt.setDate(4, Date.valueOf(date));
            stmt.executeUpdate();
            System.out.println("Transaction added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding transaction: " + e.getMessage());
        }
    }

    public static void showTransactions(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transactions");
            System.out.println("\n--- Transaction Report ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name")
                        + ", Amount: " + rs.getDouble("amount") + ", Category: " + rs.getString("category")
                        + ", Date: " + rs.getDate("date"));
            }
            System.out.println("-------------------------");
        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }

    public static void updateTransaction(Connection conn) {
        showTransactions(conn);
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter transaction ID to update: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new name (leave blank to keep unchanged): ");
        String name = sc.nextLine();
        System.out.print("Enter new amount (leave blank to keep unchanged): ");
        String amountStr = sc.nextLine();
        System.out.print("Enter new category (leave blank to keep unchanged): ");
        String category = sc.nextLine();
        System.out.print("Enter new date (YYYY-MM-DD, leave blank to keep unchanged): ");
        String date = sc.nextLine();

        try {
            if (!name.isEmpty()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET name = ? WHERE id = ?");
                stmt.setString(1, name);
                stmt.setInt(2, id);
                stmt.executeUpdate();
            }
            if (!amountStr.isEmpty()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET amount = ? WHERE id = ?");
                stmt.setDouble(1, Double.parseDouble(amountStr));
                stmt.setInt(2, id);
                stmt.executeUpdate();
            }
            if (!category.isEmpty()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET category = ? WHERE id = ?");
                stmt.setString(1, category);
                stmt.setInt(2, id);
                stmt.executeUpdate();
            }
            if (!date.isEmpty()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET date = ? WHERE id = ?");
                stmt.setDate(1, Date.valueOf(date));
                stmt.setInt(2, id);
                stmt.executeUpdate();
            }
            System.out.println("Transaction updated.");
        } catch (SQLException e) {
            System.out.println("Error updating transaction: " + e.getMessage());
        }
    }

    public static void deleteTransaction(Connection conn) {
        showTransactions(conn);
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter transaction ID to delete: ");
        int id = sc.nextInt();

        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM transactions WHERE id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Transaction deleted.");
        } catch (SQLException e) {
            System.out.println("Error deleting transaction: " + e.getMessage());
        }
    }

    public static void checkBudget(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet incomeRs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE amount > 0");
            double totalIncome = 0;
            if (incomeRs.next()) {
                totalIncome = incomeRs.getDouble(1);
            }

            ResultSet expenseRs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE amount < 0");
            double totalExpenses = 0;
            if (expenseRs.next()) {
                totalExpenses = expenseRs.getDouble(1);
            }

            System.out.println("Total Income: " + totalIncome);
            System.out.println("Total Expenses: " + Math.abs(totalExpenses));
            System.out.println("Net Savings: " + (totalIncome + totalExpenses));
        } catch (SQLException e) {
            System.out.println("Error calculating budget: " + e.getMessage());
        }
    }

    public static void giveFinancialAdvice() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter how much you want to save: ");
        double goalAmount = sc.nextDouble();
        System.out.print("Enter how many years to achieve this goal: ");
        int years = sc.nextInt();
        System.out.print("Enter your current savings: ");
        double currentSavings = sc.nextDouble();

        double yearlySavings = (goalAmount - currentSavings) / years;
        double monthlySavings = yearlySavings / 12;

        System.out.println("To reach your goal of " + goalAmount + " in " + years + " years, you need to save "
                + yearlySavings + " annually, or " + monthlySavings + " per month.");
    }

    public static void main(String[] args) {
        Connection conn = connectToDatabase();
        if (conn == null) {
            System.out.println("Database connection failed. Exiting.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Simple Finance Manager ---");
            System.out.println("::::  1. Add Transaction    ::::");
            System.out.println("::::  2. Update Transaction ::::");
            System.out.println("::::  3. Delete Transaction ::::");
            System.out.println("::::  4. Show Transactions  ::::");
            System.out.println("::::  5. Check Budget       ::::");
            System.out.println("::::  6. Financial Goal Advice ::");
            System.out.println("::::  7. Exit                  ::");

            System.out.print("Choose an option: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    addTransaction(conn);
                    break;
                case 2:
                    updateTransaction(conn);
                    break;
                case 3:
                    deleteTransaction(conn);
                    break;
                case 4:
                    showTransactions(conn);
                    break;
                case 5:
                    checkBudget(conn);
                    break;
                case 6:
                    giveFinancialAdvice();
                    break;
                case 7:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
