package miniproject.dao;

import java.util.List;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public interface AccountRepository {

    void insertAccount(Customer customer, LoanAccount account) throws DuplicateAccountException;

    LoanAccount findByAccountNumber(String accountNumber) throws AccountNotFoundException;

    Customer findCustomer(String uniqueId) throws AccountNotFoundException;

    List<LoanAccount> findAll();
}
