package com.revoltcode.account.cmd.infrastructure.handler;

import com.revoltcode.account.cmd.command.*;

public interface CommandHandler {

    public void handle(OpenAccountCommand command);
    public void handle(DepositFundsCommand command);
    public void handle(WithdrawFundsCommand command);
    public void handle(CloseAccountCommand command);
    public void handle(RestoreReadDbCommand command);
}
