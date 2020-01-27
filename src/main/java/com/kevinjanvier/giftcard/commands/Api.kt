package com.kevinjanvier.giftcard.commands

import org.axonframework.modelling.command.TargetAggregateIdentifier


data class IssueCommand(@TargetAggregateIdentifier val id: String,
                        val amount: Int)
data class IssuedEvent(val id: String,
                       val amount: Int)
data class RedeemCommand(
        @TargetAggregateIdentifier
                         val id: String,
                         val amount: Int)

data class RedeemedEvent(val id: String,
                         val amount: Int)