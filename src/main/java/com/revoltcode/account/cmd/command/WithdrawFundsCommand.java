package com.revoltcode.account.cmd.command;

import com.revoltcode.cqrs.core.command.BaseCommand;
import lombok.Data;

@Data
public class WithdrawFundsCommand extends BaseCommand {
    private double amount;
}