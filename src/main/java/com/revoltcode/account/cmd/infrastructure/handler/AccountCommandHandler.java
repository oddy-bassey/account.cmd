package com.revoltcode.account.cmd.infrastructure.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revoltcode.account.cmd.command.*;
import com.revoltcode.account.cmd.domain.aggregate.AccountAggregate;
import com.revoltcode.account.cmd.infrastructure.service.AccountService;
import com.revoltcode.account.cmd.infrastructure.service.CustomerService;
import com.revoltcode.account.common.dto.rest.Customer;
import com.revoltcode.account.common.exception.AccountExistsException;
import com.revoltcode.account.common.exception.FraudulentTransactionException;
import com.revoltcode.account.common.exception.InsufficientFundsException;
import com.revoltcode.account.common.exception.NegativeDepositAmountException;
import com.revoltcode.cqrs.core.infrastructure.handler.EventSourcingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountCommandHandler implements CommandHandler{

    private final EventSourcingHandler<AccountAggregate> eventSourcingHandler;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(OpenAccountCommand command) {
        if(command.getInitialCredit()<0) throw new NegativeDepositAmountException("The initial credit amount cannot be less than 0.00!");

        // ToDO: verify customer exists!
        StringBuffer accountName = new StringBuffer();
        ResponseEntity<Customer> customerResponse = customerService.getCustomer(command.getCustomerId());
        Customer customer = customerResponse.getBody();
        accountName.append(customer.getLastName()).append(" ").append(customer.getFirstName());

        // ToDO: verify if customer has an account of account type!
        ResponseEntity<?> accountResponse = accountService.getAccountByCustomerAndAccountType(command.getCustomerId(), command.getAccountType());

        if(accountResponse.getStatusCode().value() == 200 && Optional.ofNullable(accountResponse.getBody()).isPresent())  throw new AccountExistsException(
                MessageFormat.format("Unable to create account for customer ({0}) with id: {1}, " +
                        "who already has an existing {2} account with Zubank!",
                        accountName, command.getId(), command.getAccountType()));


        var aggregate = new AccountAggregate(command, accountName.toString());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(DepositFundsCommand command) {
        var aggregate = eventSourcingHandler.getById(command.getId());
        aggregate.depositFunds(command.getAmount());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(WithdrawFundsCommand command) {
        var aggregate = eventSourcingHandler.getById(command.getId());
        if(command.getAmount() > aggregate.getBalance()){
            throw new InsufficientFundsException("Withdrawal declined, insufficient funds!");
        }
        aggregate.withdrawFunds(command.getAmount());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(TransferFundsCommand command) {
        var aggregate = eventSourcingHandler.getById(command.getId());

        if(command.getId() == command.getCreditAccountId()) {
            throw new FraudulentTransactionException(
                    MessageFormat.format("Account with id: {0} cannot make a transfer of funds to itself!", command.getId()));
        }
        if(command.getAmount() > aggregate.getBalance()){
            throw new InsufficientFundsException("Transfer declined, insufficient funds!");
        }
        aggregate.transferFunds(command.getAmount(), command.getCreditAccountId());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(CloseAccountCommand command) {
        var aggregate = eventSourcingHandler.getById(command.getId());
        aggregate.closeAccount();
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(RestoreReadDbCommand command) {
        eventSourcingHandler.republishEvents();
    }
}
