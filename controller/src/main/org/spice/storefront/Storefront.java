
package org.spice.storefront;

import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.*;

public class Storefront extends RESTServlet {
    Data myData = new Data();

    public void doSubmitOrder(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        // Will item parameters be here as well?
        String name, address;

        String credit, discountID;
        String itemId;

        try {
            name = readParam(req, "name")[0];
            address = readParam(req, "address")[0];
            credit = readParam(req, "credit")[0];
            discountID = readParam(req, "discountId")[0];
            itemId = readParam(req,"itemId")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonObjectBuilder result = Json.createObjectBuilder();

        int personId;
        JsonObject person = null;
        try{
            personId = myData.insert(Persons.TABLE)
                .add("name", name)
                .add("billingAddress", address)
                .add("credit", credit)
                .execute();

            person = myData.select()
                .addTable(Persons.TABLE)
                .addValue(Data.WILDCARD)
                .addClause(Persons.ID, Data.EQ, Data.wrap(personId))
                .execute().get(0);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        result.add("person", person);

        int disId, itId;
        try {
            disId = Integer.parseInt(discountID);
            itId = Integer.parseInt(itemId);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonObject order = null;
        try{
            int id = myData.insert(Orders.TABLE)
                .add("itemId", itId)
                .add("discountId", disId)
                .add("personId", personId)
                .execute();

            order = myData.select()
                .addTable(Orders.TABLE)
                .addValue(Data.WILDCARD)
                .addClause(Orders.ID, Data.EQ, Data.wrap(id))
                .execute().get(0);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        result.add("order", order);

        JsonWriter writer = setupJson(response);
        try{
            writer.writeObject(result.build());
        } finally {
            writer.close();
        }
    }

    public void doGenerateDiscount(HttpServletRequest req, HttpServletResponse response) {
        JsonObject discount = null;
        try{
            int id = myData.insert(Discounts.TABLE).execute();
            discount = myData.select()
                .addTable(Discounts.TABLE)
                .addValue(Data.WILDCARD)
                .addClause(Discounts.ID, Data.EQ, Data.wrap(id))
                .execute().get(0);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);
        try{
            writer.writeObject(discount);
        } finally {
            writer.close();
        }
    }

    public Storefront() {
        registerMethod("submitOrder", this::doSubmitOrder);
        registerMethod("generateDiscount", this::doGenerateDiscount);
    }

}
