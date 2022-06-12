package com.revoltcode.account.cmd.command;

import com.revoltcode.account.common.dto.AccountType;
import com.revoltcode.cqrs.core.command.BaseCommand;
import lombok.Data;

@Data
public class OpenAccountCommand extends BaseCommand {

    private String customerId;
    private AccountType accountType;
    private double initialCredit;
}
