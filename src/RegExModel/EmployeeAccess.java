package RegExModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.SQLException;

/**
 * Created by Walter Schaertl on 3/23/2019.
 */

/**
 * Class that contains ease of use methods for the employees that use the system.
 * Functions here are called by the server.
 */
public class EmployeeAccess implements AutoCloseable{
    private String username, type;
    private Connection connection;
    private H2Access h2;

    /**
     * How an employee logs in
     * @param username the employee's username
     * @param password the employee's password
     * @param type the type of employee (accounting or package)
     * @throws SQLException Indicates a failure to log in, either wrong username or password
     */
    public EmployeeAccess(String username, String password, String type) throws SQLException{
        this.username = username;
        this.type = type;
        this.h2 = new H2Access();
        this.connection = this.h2.createConnection(username, password);
    }

    /**
     * An easy way to get the ID of a logged in employee
     * @return int: The employee's ID. returns -1 on failure.
     */
    public int getId() {
        String query = "SELECT general_fk FROM user where username='" + username + "'";
        ResultSet r = h2.createAndExecuteQuery(connection, query);
        try {
            if (r.next())
                return r.getInt(1);
        }catch (SQLException e) {}
        return -1;
    }

    /**
     * Function required to be implemented by AutoCloseable.
     * Lets CustomerAccess be used in a 'try with resources' block
     */
    @Override
    public void close(){
        h2.closeConnection(this.connection);
    }

    /**
     * Ease of use function that lets logged in package employees update a packages location.
     * Makes an entry in the transaction table.
     *
     * @param locationId The location the package employee is at
     * @param acctNumber The account number assocaited with the package
     * @param pkgSerial The package serial
     */
    public void updatePackageLocation(String locationId, int acctNumber, String pkgSerial){
        Date date = new Date(System.currentTimeMillis());
        Time time = new Time(System.currentTimeMillis());
        int employeeId = getId();
        String query = String.format("INSERT INTO transaction(DATE, TIME, EMPLOYEE_ID_FK, LOCATION_ID_FK, " +
                        "ACCOUNT_NUMBER_FK, PACKAGE_SERIAL_FK) VALUES (\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\');",
                        date, time, employeeId, locationId, acctNumber, pkgSerial);
        h2.createAndExecute(connection, query);
    }

    /**
     * An easy way for accounting employees to see all the customers in the system to
     * better aid them with billing or questions.
     * @return a ResultSet of customers, to be processed by the client
     */
    public ResultSet getCustomers(){
        String query = "SELECT * from customer";
        return h2.createAndExecuteQuery(connection, query);
    }

    /**
     * An easy way for accounting employees to see all the customers in the system,
     * and allows for selection and trimming down of results.
     * @return a ResultSet of customers, to be processed by the client
     */
    public ResultSet getCustomersWhere(String conditional){
        String query = "SELECT * from customer WHERE " +  conditional;
        return h2.createAndExecuteQuery(connection, query);
}

    /**
     * An easy way for accounting employees to see customer billing information
     * @param acctNumber int: The id of the account to view the billing information
     * @return a ResultSet of customer billing information, to be processed by the client
     */
    public ResultSet viewCustomerBilling(int acctNumber){
        String query = "SELECT * from billing WHERE account_number_fk=" + acctNumber;
        return h2.createAndExecuteQuery(connection, query);
    }

    public void modifyCustomerBilling(int ID, double balance, String payModel, int acctNum) {
        int employeeID = this.getId();
        String query = "UPDATE billing SET balance_to_date=" + balance + " , pay_model=" + payModel +
                ", employeeID=" + employeeID + "WHERE ID=" + ID + " AND account_number_fk=" + acctNum;
        h2.createAndExecute(connection, query);
    }

    /**
     * An easy way for accounting employee's to track a package
     * @param accntNum: The account id associate with the package
     * @param serial: The package's serial
     * @return A ResultSet of transactions showing the locations a package has been
     */
    public ResultSet viewPackageHistory(int accntNum, String serial){
        String query = "SELECT * from transaction WHERE account_number_fk=" + accntNum +
                " AND package_serial_fk='" + serial +"'";
        return h2.createAndExecuteQuery(connection, query);
    }

    /**
     * An easy way to view detailed information about a package.
     * @param accntNum: The account id associate with the package
     * @param serial: The package's serial
     * @return A ResultSet of the package.
     */
    public ResultSet viewPackageData(int accntNum, String serial){
        String query = "SELECT * from package WHERE account_number_fk=" + accntNum +
                " AND serial='" + serial +"'";
        return h2.createAndExecuteQuery(connection, query);
    }


    /**
     * The way to create an entry to rates table.
     * @param negotiatedID the negotiated id
     * @param groundRate the ground rate of the package
     * @param airRate the air rate of the package
     * @param rushRate the rush rate of the package
     * @param DRB dim_rating_break
     */
    public void CreateRate(String negotiatedID, String groundRate, String airRate, String rushRate, String DRB) {
        int employeeID = this.getId();
        int negotiated = Integer.parseInt(negotiatedID);
        double ground = Double.parseDouble(groundRate);
        double air = Double.parseDouble(airRate);
        double rush = Double.parseDouble(rushRate);
        int drb = Integer.parseInt(DRB);
        String query = String.format("INSERT INTO rates(NEGOTIATED_RATE_ID, GROUND_RATE, AIR_RATE, RUSH_RATE, " +
                "DIM_RATING_BREAK, EMPLOYEEID) VALUES (\"%d\",\"%f\",\"%f\",\"%f\",\"%d\",\"%d\");",
                negotiated, ground, air, rush, drb, employeeID);
        h2.createAndExecute(connection, query);
    }

    /**
     * The way to view the rates of the package by negotiated id
     * @param negotiatedID the negotiated id for the package
     * @return A ResultSet of the package.
     */
    public ResultSet viewRates(String negotiatedID) {
        int negotiated = Integer.parseInt(negotiatedID);
        String query = "SELECT * FROM rates WHERE negotiated_rate_id=\"" + negotiated + "\"";
        return h2.createAndExecuteQuery(connection, query);
    }

    /**
     * The way to modify the info of the entry from rates table
     * @param negotiatedID the negotiated id
     * @param groundRate the new ground rate to be set up
     * @param airRate the new air rate to be set up
     * @param rushRate the new rush rate to be set up
     * @param DRB the new dim_rating_break to be set up
     */
    public void modifyRates(String negotiatedID, String groundRate, String airRate, String rushRate, String DRB) {
        int employeeID = this.getId();
        int negotiated = Integer.parseInt(negotiatedID);
        double ground = Double.parseDouble(groundRate);
        double air = Double.parseDouble(airRate);
        double rush = Double.parseDouble(rushRate);
        int drb = Integer.parseInt(DRB);
        String query = String.format("UPDATE rates SET GROUND_RATE = \"" + ground + "\", AIR_RATE = \"" +
                air + "\", RUSH_rATE = \"" + rush + "\", DIM_RATING_BREAK = \"" + drb + "\", EMPLOYEEID =\""
                + employeeID + "WHERE NEGOTIATED_RATE_ID = \"" + negotiated + "\";");

        h2.createAndExecute(connection, query);
    }



}
