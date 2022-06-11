package com.revoltcode.account.cmd.domain.aggregate;

import com.revoltcode.account.cmd.command.OpenAccountCommand;
import com.revoltcode.account.common.event.AccountClosedEvent;
import com.revoltcode.account.common.event.AccountOpenedEvent;
import com.revoltcode.account.common.event.FundsDepositedEvent;
import com.revoltcode.account.common.event.FundsWithdrawnEvent;
import com.revoltcode.cqrs.core.domain.aggregate.AggregateRoot;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AccountAggregate extends AggregateRoot {

    private Boolean active;
    private double balance;

    public Boolean getActive() {
        return active;
    }

    public double getBalance(){
        return this.balance;
    }

    public AccountAggregate(OpenAccountCommand command){
        raiseEvent(AccountOpenedEvent.builder()
                .id(command.getId())
                .accountHolder(command.getAccountHolder())
                .accountType(command.getAccountType())
                .openingBalance(command.getOpeningBalance())
                .build());
    }

    public void apply(AccountOpenedEvent event){
        this.id = event.getId();
        this.active = true;
        this.balance = event.getOpeningBalance();
    }

    public void depositFunds(double amount){
        if(!this.active) throw new IllegalStateException("Funds cannot be deposited into a closed account!");
        if(amount <= 0) throw new IllegalStateException("The deposit amount must be greter than 0!");

        raiseEvent(FundsDepositedEvent.builder()
                .id(this.id)
                .amount(amount)
                .build());
    }

    public void apply(FundsDepositedEvent event){
        this.id = event.getId();
        this.balance += event.getAmount();
    }

    public void withdrawFunds(double amount){
        if(!this.active) throw new IllegalStateException("Funds cannot be withdrawn from a closed account!");

        raiseEvent(FundsWithdrawnEvent.builder()
                .id(this.id)
                .amount(amount)
                .build());
    }

    public void apply(FundsWithdrawnEvent event){
        this.id = event.getId();
        this.balance -= event.getAmount();
    }

    public void closeAccount(){
        if(!this.active) throw new IllegalStateException("The bank account has already been closed!");

        raiseEvent(AccountClosedEvent.builder()
                .id(this.id)
                .build());
    }

    public void apply(AccountClosedEvent event){
        this.id = event.getId();
        this.active = false;
    }
}
