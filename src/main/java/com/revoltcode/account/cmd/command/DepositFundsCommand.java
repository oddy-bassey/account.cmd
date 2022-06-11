package com.revoltcode.account.cmd.command;

import com.revoltcode.cqrs.core.command.BaseCommand;
import lombok.Data;

@Data
public class DepositFundsCommand extends BaseCommand {
    private double amount;
}
