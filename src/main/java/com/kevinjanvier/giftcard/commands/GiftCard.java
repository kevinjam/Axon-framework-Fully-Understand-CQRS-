package com.kevinjanvier.giftcard.commands;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

@Aggregate
@Profile("command")
public class GiftCard {
    @AggregateIdentifier
    private String id;
    private int balance;

   public GiftCard(){

   }

    @CommandHandler
    public GiftCard(IssueCommand cmd) {
        if(cmd.getAmount()<=0)
            throw new IllegalArgumentException("amount <= 0");

        AggregateLifecycle.apply(new IssuedEvent(cmd.getId(), cmd.getAmount()));
    }

    @CommandHandler
    public void handle(RedeemCommand cmd) {
        if(cmd.getAmount()<=0)
            throw new IllegalArgumentException("amount <= 0");

        if(cmd.getAmount() > balance)
            throw new IllegalArgumentException("amount > balance");

        AggregateLifecycle.apply(new RedeemedEvent(cmd.getId(), cmd.getAmount()));
    }

    @EventSourcingHandler
    public void on(IssuedEvent evt){
        //for a new card, the amount is the balance
        id = evt.getId();
        balance = evt.getAmount();
    }

    @EventSourcingHandler
    public void on(RedeemedEvent evt) {
        //update the balance when a card is redeemed
        balance = balance - evt.getAmount();
    }


}
