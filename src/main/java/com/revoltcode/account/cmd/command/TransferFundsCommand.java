package com.revoltcode.account.cmd.command;

import com.revoltcode.cqrs.core.command.BaseCommand;
import lombok.Data;

@Data
public class TransferFundsCommand extends BaseCommand {
    private String creditAccountId;
    private double amount;
}
