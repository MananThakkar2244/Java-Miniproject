package miniproject.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DataAccessException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public class AccountDAO implements AccountRepository {

    private static final String URL = "jdbc:mysql://localhost:3306/collecttrack";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL JDBC driver not found", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public void insertAccount(Customer customer, LoanAccount account) throws DuplicateAccountException {
        String insertCustomer = "INSERT IGNORE INTO customer (unique_id, name, address, phone, email) VALUES (?, ?, ?, ?, ?)";
        String checkAccount = "SELECT account_number FROM loan_account WHERE account_number = ?";
        String insertAccount = "INSERT INTO loan_account (account_number, unique_id, open_date, loan_type, "
                + "loan_amount, emi_amount, due_day, emis_missed, dpd, dpd_bucket, paid_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(checkAccount)) {
                    ps.setString(1, account.getAccountNumber());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            throw new DuplicateAccountException(
                                    "Account already exists: " + account.getAccountNumber());
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertCustomer)) {
                    ps.setString(1, customer.getUniqueId());
                    ps.setString(2, customer.getName());
                    ps.setString(3, customer.getAddress());
                    ps.setString(4, customer.getPhone());
                    ps.setString(5, customer.getEmail());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(insertAccount)) {
                    ps.setString(1, account.getAccountNumber());
                    ps.setString(2, account.getCustomerId());
                    ps.setDate(3, java.sql.Date.valueOf(account.getOpenDate()));
                    ps.setString(4, account.getLoanTypeCode());
                    ps.setInt(5, (int) account.getLoanAmount());
                    ps.setInt(6, (int) account.getEmiAmount());
                    ps.setInt(7, account.getDueDay());
                    ps.setInt(8, account.getEmisMissed());
                    ps.setInt(9, account.getDpd());
                    ps.setString(10, account.getBucket());
                    ps.setString(11, account.getPaidStatus());
                    ps.executeUpdate();
                }

                con.commit();
            } catch (DuplicateAccountException e) {
                throw e;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (DuplicateAccountException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert account: " + account.getAccountNumber(), e);
        }
    }

    @Override
    public void updateRecord(Customer customer, LoanAccount account) throws AccountNotFoundException {
        String updateCustomer = "UPDATE customer SET name = ?, address = ?, phone = ?, email = ? WHERE unique_id = ?";
        String updateAccount = "UPDATE loan_account SET loan_type = ?, open_date = ?, loan_amount = ?, emi_amount = ?, "
                + "due_day = ?, emis_missed = ?, dpd = ?, dpd_bucket = ?, paid_status = ? WHERE account_number = ?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(updateCustomer)) {
                    ps.setString(1, customer.getName());
                    ps.setString(2, customer.getAddress());
                    ps.setString(3, customer.getPhone());
                    ps.setString(4, customer.getEmail());
                    ps.setString(5, customer.getUniqueId());
                    ps.executeUpdate();
                }

                int rows;
                try (PreparedStatement ps = con.prepareStatement(updateAccount)) {
                    ps.setString(1, account.getLoanTypeCode());
                    ps.setDate(2, java.sql.Date.valueOf(account.getOpenDate()));
                    ps.setInt(3, (int) account.getLoanAmount());
                    ps.setInt(4, (int) account.getEmiAmount());
                    ps.setInt(5, account.getDueDay());
                    ps.setInt(6, account.getEmisMissed());
                    ps.setInt(7, account.getDpd());
                    ps.setString(8, account.getBucket());
                    ps.setString(9, account.getPaidStatus());
                    ps.setString(10, account.getAccountNumber());
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    con.rollback();
                    throw new AccountNotFoundException("Account not found: " + account.getAccountNumber());
                }

                con.commit();
            } catch (AccountNotFoundException e) {
                throw e;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update account: " + account.getAccountNumber(), e);
        }
    }

    @Override
    public AccountRecord findByAccountNumber(String accountNumber) throws AccountNotFoundException {
        String sql = "SELECT la.*, c.name, c.address, c.phone, c.email "
                + "FROM loan_account la JOIN customer c ON la.unique_id = c.unique_id "
                + "WHERE la.account_number = ?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AccountNotFoundException("Account not found: " + accountNumber);
                }
                return buildAccountRecord(rs);
            }
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find account: " + accountNumber, e);
        }
    }

    @Override
    public Customer findCustomer(String uniqueId) throws AccountNotFoundException {
        String sql = "SELECT * FROM customer WHERE unique_id = ?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AccountNotFoundException("Customer not found: " + uniqueId);
                }
                return new Customer(
                        rs.getString("unique_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email"));
            }
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find customer: " + uniqueId, e);
        }
    }

    @Override
    public List<AccountRecord> findAll() {
        String sql = "SELECT la.*, c.name, c.address, c.phone, c.email "
                + "FROM loan_account la JOIN customer c ON la.unique_id = c.unique_id";
        List<AccountRecord> records = new ArrayList<>();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(buildAccountRecord(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list accounts", e);
        }
        return records;
    }

    @Override
    public List<AccountRecord> findByCustomerId(String uniqueId) throws AccountNotFoundException {
        String sql = "SELECT la.*, c.name, c.address, c.phone, c.email "
                + "FROM loan_account la JOIN customer c ON la.unique_id = c.unique_id "
                + "WHERE la.unique_id = ?";
        List<AccountRecord> records = new ArrayList<>();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(buildAccountRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find accounts for customer: " + uniqueId, e);
        }

        if (records.isEmpty()) {
            throw new AccountNotFoundException("No accounts found for customer: " + uniqueId);
        }

        return records;
    }

    private AccountRecord buildAccountRecord(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
                rs.getString("unique_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email"));
        LoanAccount account = LoanAccount.fromCode(
                rs.getString("loan_type"),
                rs.getString("account_number"),
                rs.getString("unique_id"),
                rs.getDate("open_date").toLocalDate(),
                rs.getInt("loan_amount"),
                rs.getInt("emi_amount"),
                rs.getInt("due_day"),
                rs.getInt("emis_missed"));
        account.calculateDpd(LocalDate.now());
        return new AccountRecord(customer, account);
    }
}
