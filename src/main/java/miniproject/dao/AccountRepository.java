package miniproject.dao;

import java.util.List;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public interface AccountRepository {

    void insertAccount(Customer customer, LoanAccount account) throws DuplicateAccountException;

    void updateRecord(Customer customer, LoanAccount account) throws AccountNotFoundException;

    AccountRecord findByAccountNumber(String accountNumber) throws AccountNotFoundException;

    Customer findCustomer(String uniqueId) throws AccountNotFoundException;

    List<AccountRecord> findAll();

    List<AccountRecord> findByCustomerId(String uniqueId) throws AccountNotFoundException;
}
