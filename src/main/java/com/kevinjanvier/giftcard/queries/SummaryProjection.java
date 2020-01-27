package com.kevinjanvier.giftcard.queries;


import com.kevinjanvier.giftcard.commands.IssuedEvent;
import com.kevinjanvier.giftcard.commands.RedeemedEvent;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

@Component
@Profile("query")
public class SummaryProjection {


    private final EntityManager entityManager;

    public SummaryProjection(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler
    public void handle(IssuedEvent event){
        entityManager.persist(
                new CardSummary(
                        event.getId(),
                        event.getAmount(),
                        event.getAmount()
                )
        );
    }

    @EventHandler
    public void handle(RedeemedEvent redeemedEvent){
        CardSummary summary = entityManager.find(CardSummary.class, redeemedEvent.getId());
        summary.setRemainingBalance(summary.getRemainingBalance() - redeemedEvent.getAmount());

    }

    //data will be parsisted
    @Autowired
    public void config(EventProcessingConfiguration config){
    }

    @QueryHandler
    public List<CardSummary> handle(DataQuery dataQuery){

        return entityManager.createQuery("SELECT c FROM CardSummary c ORDER BY c.id",
                CardSummary.class).getResultList();
    }

    @QueryHandler
    public Integer handle(SizeQuery query){
return entityManager.createQuery("SELECT COUNT(c) FROM CardSummary c",
        Long.class).getSingleResult().intValue();
    }
}
