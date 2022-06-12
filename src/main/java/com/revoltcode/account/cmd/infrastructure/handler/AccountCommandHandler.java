package com.revoltcode.account.cmd.infrastructure.handler;

import com.revoltcode.account.cmd.command.*;
import com.revoltcode.account.cmd.domain.aggregate.AccountAggregate;
import com.revoltcode.account.common.exception.InsufficientFundsException;
import com.revoltcode.account.common.exception.NegativeDepositAmountException;
import com.revoltcode.cqrs.core.infrastructure.handler.EventSourcingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountCommandHandler implements CommandHandler{

    @Autowired
    private final EventSourcingHandler<AccountAggregate> eventSourcingHandler;

    @Override
    public void handle(OpenAccountCommand command) {
        if(command.getInitialCredit()<0) throw new NegativeDepositAmountException("The initial credit amount cannot be less than 0.00!");
        // ToDO: verify customer exists!
        var aggregate = new AccountAggregate(command);
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
