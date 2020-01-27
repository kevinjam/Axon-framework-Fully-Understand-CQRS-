package com.kevinjanvier.giftcard.gui;

import com.kevinjanvier.giftcard.commands.IssueCommand;
import com.kevinjanvier.giftcard.commands.RedeemCommand;
import com.kevinjanvier.giftcard.commands.IssueCommand;
import com.kevinjanvier.giftcard.commands.RedeemCommand;
import com.kevinjanvier.giftcard.queries.CardSummary;
import com.kevinjanvier.giftcard.queries.CardSummary;
import com.kevinjanvier.giftcard.queries.DataQuery;
import com.kevinjanvier.giftcard.queries.SizeQuery;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Profile;

import java.util.stream.Stream;

@SpringUI
@Profile("client")
public class GiftCardGUI extends UI {


    private CommandGateway commandGateway;

    private final DataProvider<CardSummary, Void> dataProvider;


    public GiftCardGUI(CommandGateway commandGateway,
                       QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.dataProvider = dataProvider(queryGateway);
    }

    private DataProvider<CardSummary, Void> dataProvider(QueryGateway queryGateway) {
        return new AbstractBackEndDataProvider<CardSummary, Void>() {
            @Override
            protected Stream<CardSummary> fetchFromBackEnd(Query<CardSummary, Void> query) {
                return queryGateway.query(new DataQuery(query.getOffset(), query.getLimit()),
                        ResponseTypes.multipleInstancesOf(CardSummary.class)).join().stream();
            }

            @Override
            protected int sizeInBackEnd(Query<CardSummary, Void> query) {
                return queryGateway.query(new SizeQuery(),
                        ResponseTypes.instanceOf(Integer.class)).join();
            }
        };
    }


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout commands = new HorizontalLayout();
        commands.setSizeFull();
        commands.addComponents(issuePanel(), redeemPanel());

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponents(commands, summaryGrid()); //adding summary Grid



        setContent(layout);

        //Error Handler
        setErrorHandler(new DefaultErrorHandler(){
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = event.getThrowable();
                while (cause.getCause() != null) cause = cause.getCause();
                Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE)
                        .addCloseListener(x->dataProvider.refreshAll());
            }
        });
    }

    private Panel issuePanel() {
        //Create the text fields and submit button
        TextField id = new TextField("id");
        TextField amount = new TextField("amount");
        Button submit = new Button("Submit");

        //Add listener to the button
        submit.addClickListener(event -> {
            IssueCommand cmd = new IssueCommand(
                    id.getValue(),
                    Integer.parseInt(amount.getValue())
            );

            //Sent the command to Axon
            commandGateway.sendAndWait(cmd);

            //Display a success notification
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(x->dataProvider.refreshAll());
        });

        //Create a form and add the textfields and button
        FormLayout form = new FormLayout();
        form.setMargin(true);
        form.addComponents(id, amount, submit);

        //Add the form to the panel
        Panel panel = new Panel("Issue");
        panel.setContent(form);

        return panel;
    }


    private Panel redeemPanel(){
        System.out.println("Redeem ++++++++++++");
        TextField id = new TextField("id");
        TextField amount = new TextField("amount");
        Button submit = new Button("Submit");


        submit.addClickListener(event -> {
            RedeemCommand cmd = new RedeemCommand(
                    id.getId(),
                    Integer.parseInt(amount.getValue())
            );

            System.out.println("Redeem UI ID " + cmd.getId());
            //send the comand
            commandGateway.sendAndWait(cmd);

            //command sign is successful
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
            .addCloseListener(x->dataProvider.refreshAll());

        });
        FormLayout form = new FormLayout();
        form.setMargin(true);
        form.addComponents(id, amount, submit);
        Panel panel = new Panel("Redeem Pannel");
        panel.setContent(form);
        return panel;
    }


    public Grid<CardSummary> summaryGrid(){
        Grid<CardSummary> grid = new Grid<>();

        //grid Size
        grid.setSizeFull();

        //add column to grid
        grid.addColumn(CardSummary::getId).setCaption("id");
        grid.addColumn(CardSummary::getInitialBalance).setCaption("Initial Balance");
        grid.addColumn(CardSummary::getRemainingBalance).setCaption("Remaining Balance");

        grid.setDataProvider(dataProvider);
        return grid;
    }
}
