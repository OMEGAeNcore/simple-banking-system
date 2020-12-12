package banking;
import org.sqlite.SQLite;
import org.sqlite.SQLiteDataSource;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Main {
    /*
        Global variables used across the program
        dict: Stores the card number and PIN number for the respective cards. Can be used later for several operations.
        iiNum: Card identification number based on our Credit Card rules.

        Member Functions used for different operations

    */
    protected static HashMap<String, String> dict = new HashMap<>();
    protected static String[][] balanceSheet;
    private static final int iiNum = 400000;

    private static boolean verifyLuhn(String cardNum){
        // To verify if the card number generated is satisfying the LUHN Algorithm or not.

        int len = cardNum.length() - 1;
        ArrayList<Integer> odd = new ArrayList<>();
        ArrayList<Integer> even = new ArrayList<>();
        int check, chsum = 0;

        // Drop last digit
        for(int i = 0; i < len; i++){
            if(i % 2 == 0)
                odd.add(Character.getNumericValue(cardNum.charAt(i)));
            else
                even.add(Character.getNumericValue(cardNum.charAt(i)));
        }
        check = Character.getNumericValue(cardNum.charAt(len));

        // Multiplying odd digits by 2
        for(int i = 0; i < odd.size(); i++){
            odd.set(i, odd.get(i) * 2);
        }

        // Subtract 9 to numbers over 9
        for(int  i = 0; i < odd.size(); i++){
            if(odd.get(i) > 9){
                odd.set(i, odd.get(i) - 9);
            }
        }

        // Add all numbers
        for (Integer integer : odd) {
            chsum += integer;
        }
        for (Integer integer : even) {
            chsum += integer;
        }

        // Check for LUHN: If mod of sum of all numbers gives 0 or not
        return (10 - chsum % 10) == check;
    }

    private static String createCardNumber(){
        // Returns the newly created card

        Random random = new Random();
        StringBuilder temp = new StringBuilder();
        int[] rnd = new int[16];
        String iiNumCopy = Integer.toString(iiNum);
        /*
        * Continue as long as the generated card number is present in the dictionary and
        * is not in Luhn.
        *
        * Break out of the loop, if dictionary doesn't have the number and it satisfies LUHN.
        * */
        // Iterate as long as card number is not unique and does not satisfies LUHN Algorithm
        do{
            temp = new StringBuilder();
            for(int i = 0; i < 6; i++){
                rnd[i] = Character.getNumericValue(iiNumCopy.charAt(i));
            }
            for(int i = 6; i < 16; i++){
                rnd[i] = random.nextInt(9);
            }
            for(int i = 0; i < 16; i++){
                temp.append(Integer.toString(rnd[i]));
            }
        }while(!(!dict.containsKey((temp.toString())) && verifyLuhn(temp.toString())));
        return temp.toString();
    }
    private static String createPINNumber(){
        // Returns the newly created PIN

        Random random = new Random();
        int one, two, three, four;
        one = random.nextInt(9);
        two = random.nextInt(9);
        three = random.nextInt(9);
        four = random.nextInt(9);
        return Integer.toString(one)+ two + three + four;
    }
    private static void displayCardDetails(String newCard){
        // Displays the newly created credit card data

        System.out.println("Your card number:\n" + newCard);
        System.out.println("Your card PIN:\n" + dict.get(newCard));
    }
    private static boolean checkCorrectCard(String cardNum, String pinNum){
        // Return if card number and pin correct or not

        // If card number not in table or pin number does not match the existing pin, then return false, otherwise true
        return dict.get(cardNum) != null && dict.get(cardNum).equals(pinNum);
    }
    private static void displayBalance(int dispBalance){
        // Displaying the balance of the newly created account

        System.out.println("Balance: " + dispBalance);
    }
    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        /*
        * Objectives
            1. Create a db from command line arguments
            2. If card table does not exist, Create table in the db from step 1 with a table card that has columns id, number, int, balance.
            3. load data from card table
            4. insert accounts to the table when they are created
        * */
        int ch1, ch2;

        // TODO: Create a datasource object from the command line arguments: READY!
        String dbFile = args[1];
        String url = "jdbc:sqlite:" + dbFile;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        // TODO: Create a Connection Object
        try (Connection connection = dataSource.getConnection()){
            try(Statement statement = connection.createStatement()){

                // TODO: Create the card database if it doesn't already exists: READY!
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY, " +
                        "number TEXT, " +
                        "pin TEXT, " +
                        "balance INTEGER DEFAULT 0)"
                        );

                // TODO: Get data from the database: READY!
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM card")){
                    while(resultSet.next()){
                        dict.put(resultSet.getString("number"), resultSet.getString("pin"));
                    }
                }

                do{
                /*
                *   First Menu section. To choose what has to be done
                */
                    System.out.println();
                    System.out.println("1. Create an account");
                    System.out.println("2. Log into account");
                    System.out.println("0. Exit");
                    ch1 = Integer.parseInt(br.readLine());
                    System.out.println();
                    switch(ch1){
                        case 0: // Dummy exit
                            break;
                        case 1: // Create an account

                            // TODO: Create card: READY!
                            String newCard, newPin;
                            newCard = createCardNumber();
                            newPin = createPINNumber();

                            System.out.println("Your card has been created");

                            // Keeping the newly created card number and PIN in memory <HashTable>
                            dict.put(newCard, newPin);

                            // TODO: Use the data from database and perform respective operations: READY!
                            String insertSQL = "INSERT INTO card(number, pin, balance) VALUES (?, ?, ?)";
                            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                            preparedStatement.setString(1, newCard);
                            preparedStatement.setString(2, newPin);
                            preparedStatement.setInt(3, 0);
                            preparedStatement.executeUpdate();

                            // TODO: Display card details: READY!
                            displayCardDetails(newCard);
                            break;
                        case 2: // Log into account

                            // TODO: Enter card number and pin number: READY!
                            String cardNum, pinNum;
                            System.out.println("Enter your card number:");
                            cardNum = br.readLine();
                            System.out.println("Enter your PIN:");
                            pinNum = br.readLine();

                            // TODO: Check if pin or card number correct or not: READY!
                            if(checkCorrectCard(cardNum, pinNum)){
                                System.out.println("You have successfully logged in!");

                                // TODO: If successfully logged in, procceed: READY!
                                do{
                                    /*
                                    *   Second Menu after logging in.
                                    */
                                    System.out.println("1. Balance");
                                    System.out.println("2. Add income");
                                    System.out.println("3. Do transfer");
                                    System.out.println("4. Close account");
                                    System.out.println("5. Log out");
                                    System.out.println("0. Exit");
                                    ch2 = Integer.parseInt(br.readLine());
                                    switch(ch2){
                                        case 0: // Dummy Exit
                                            ch1 = 0;
                                            break;
                                        case 1: // Balance
                                        {
                                            String checkBalanceSQL = "SELECT * FROM card " +
                                                    "WHERE number = ? AND pin = ?";
                                            PreparedStatement checkBalance = connection.prepareStatement(checkBalanceSQL);
                                            checkBalance.setString(1, cardNum);
                                            checkBalance.setString(2, pinNum);
                                            ResultSet checkBalVal = checkBalance.executeQuery();
                                            checkBalVal.next();
                                            int balanceVar = checkBalVal.getInt("balance");

                                            // TODO: Display balance. Display 0 if new account: READY!
                                            displayBalance(balanceVar);
                                            checkBalVal.close();
                                        }
                                            break;
                                        case 2: // Add income
                                        {
                                            String checkBalanceSQL = "SELECT * FROM card " +
                                                    "WHERE number = ? AND pin = ?";
                                            PreparedStatement checkBalance = connection.prepareStatement(checkBalanceSQL);
                                            checkBalance.setString(1, cardNum);
                                            checkBalance.setString(2, pinNum);
                                            ResultSet checkBalVal = checkBalance.executeQuery();
                                            checkBalVal.next();
                                            int balanceVar = checkBalVal.getInt("balance");
                                            System.out.println("Enter income: ");
                                            int newIncome = Integer.parseInt(br.readLine());
                                            balanceVar += newIncome;
                                            // TODO: Add income to the currently logged in account
                                            String addIncome = "UPDATE card " +
                                                    "SET balance = ? " +
                                                    "WHERE number = ? AND pin = ?";
                                            PreparedStatement addIncomeStmt = connection.prepareStatement(addIncome);
                                            addIncomeStmt.setInt(1, balanceVar);
                                            addIncomeStmt.setString(2, cardNum);
                                            addIncomeStmt.setString(3, pinNum);
                                            addIncomeStmt.executeUpdate();

                                            System.out.println("Income was added!");
                                            checkBalVal.close();
                                        }
                                            break;
                                        case 3: // Do transfer
                                        {
                                            // TODO: Turn off auto-commit to avoid unnecessary transaction failure: READY!
                                            connection.setAutoCommit(false);

                                            System.out.println("Transfer");
                                            System.out.println("Enter card number: ");
                                            String cardNum2 = br.readLine();

                                            /*
                                             * Check If the user tries to transfer money to the same account,
                                             *  output the following message: “You can't transfer money to the same account!”
                                             * */
                                            if(cardNum.equals(cardNum2)){
                                                System.out.println("You can't transfer money to the same account!");
                                                break;
                                            }

                                            /*
                                             * Check If the receiver's card number doesn’t pass the Luhn algorithm,
                                             * you should output: “Probably you made mistake in the card number. Please try again!”
                                             * */
                                            if(!verifyLuhn(cardNum2)){
                                                System.out.println("Probably you made mistake in the card number. Please try again!");
                                                break;
                                            }

                                            /*
                                             * Check If the receiver's card number doesn’t exist,
                                             * you should output: “Such a card does not exist.”
                                             * */
                                            String receiverVerifySQL = "SELECT COUNT(*) FROM card " +
                                                                        "WHERE number = ?";
                                            PreparedStatement receiverVerifyStmt = connection.prepareStatement(receiverVerifySQL);
                                            receiverVerifyStmt.setString(1, cardNum2);
                                            ResultSet receiverVerifySet = receiverVerifyStmt.executeQuery();
                                            receiverVerifySet.next();
                                            int verifyReceiver = receiverVerifySet.getInt(1);
                                            if(verifyReceiver != 1) {
                                                System.out.println("Such a card does not exist.");
                                                break;
                                            }


                                            String checkBalanceSQL = "SELECT * FROM card " +
                                                    "WHERE number = ? AND pin = ?";
                                            PreparedStatement checkBalance = connection.prepareStatement(checkBalanceSQL);
                                            checkBalance.setString(1, cardNum);
                                            checkBalance.setString(2, pinNum);
                                            ResultSet checkBalVal = checkBalance.executeQuery();
                                            checkBalVal.next();
                                            int balanceVar = checkBalVal.getInt("balance");

                                            /*
                                             *  Check If the user tries to transfer more money than he/she has,
                                             *  output: "Not enough money!"
                                             * */
                                            System.out.println("Enter how much money you want to transfer:");
                                            int transferAmount = Integer.parseInt(br.readLine());
                                            if(transferAmount > balanceVar){
                                                System.out.println("Not enough money!");
                                                break;
                                            }

                                            /*
                                             * Check If there is no error,
                                             * ask the user how much money they want to transfer and make the transaction.
                                             * */
                                            // TODO: Do a transfer of amount between two card holders
                                            String user1Update = "UPDATE card " +
                                                                    "SET balance = ? " +
                                                                    "WHERE number = ?";
                                            // Reduce amount from sender account
                                            PreparedStatement sender = connection.prepareStatement(user1Update);
                                            sender.setInt(1, (balanceVar - transferAmount));
                                            sender.setString(2, cardNum);
                                            sender.executeUpdate();

                                            // Get receiver balance for update
                                            String receiverBalanceSQL = "SELECT * FROM card " +
                                                    "WHERE number = ?";
                                            PreparedStatement receiverBalance = connection.prepareStatement(receiverBalanceSQL);
                                            receiverBalance.setString(1, cardNum2);
                                            ResultSet receiverBalnc = receiverBalance.executeQuery();
                                            receiverBalnc.next();
                                            int user2Balance = receiverBalnc.getInt("balance");

                                            String user2Update = "UPDATE card " +
                                                                    "SET balance = ? " +
                                                                    "WHERE number = ?";
                                            // Add the amount to the receiver account
                                            PreparedStatement receiver = connection.prepareStatement(user2Update);
                                            receiver.setInt(1, (user2Balance + transferAmount));
                                            receiver.setString(2, cardNum2);
                                            receiver.executeUpdate();

                                            connection.commit();

                                            // TODO: Turn on auto-commit for other transactions: READY!
                                            connection.setAutoCommit(true);
                                        }
                                            break;
                                        case 4: // Close Account

                                            // TODO: Close the account of the currently logged in user and remove their data from database
                                            String deleteSQL = "DELETE FROM card " +
                                                                "WHERE number = ? AND pin = ?";
                                            PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL);
                                            deleteStmt.setString(1, cardNum);
                                            deleteStmt.setString(2, pinNum);
                                            deleteStmt.executeUpdate();

                                            break;
                                        case 5: // Log out

                                            // TODO: Log out of system: READY!
                                            System.out.println("You have successfully logged out!");
                                            ch2 = 0;
                                            break;
                                        default:System.out.println("Try again. Wrong choice!");
                                    }
                                }while(ch2!=0);     // Actual exit condition
                            }
                            else{
                                System.out.println("Wrong card number or PIN!");
                            }
                            break;
                        default: System.out.println("Try again. Wrong input");
                    }
                }while(ch1 != 0);   // Actual exit condition
                System.out.println("Bye!");
                connection.close();
            } catch (SQLException sqlException){
                System.out.println("SQL Exception occured:\n" + sqlException.getMessage());
            }
        } catch (SQLException sqlException){
            System.out.println("SQL Exception occured:\n" + sqlException.getMessage());
        }
    }
}