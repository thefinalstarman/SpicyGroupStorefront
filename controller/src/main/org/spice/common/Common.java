package org.spice.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.*;

public class Common extends RESTServlet {
    Data myData = new Data();

    public void doListProducts(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] name, id;
        try {
            name = readParam(req, "name", false);
            id = readParam(req, "id", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        List<JsonObject> products = null;
        try {
            Data.Select select = myData.select()
                .addTable(Products.TABLE)
                .addValue(Data.WILDCARD);

            if(!isEmpty(name)) {
                select.addClause(Products.NAME, Data.REGEX, Data.wrap(name[0]));
            } else if(!isEmpty(id)) {
                select.addClause(Products.ID, Data.EQ, Data.wrap(id[0]));
            }

            products = select.execute();
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();
            for(JsonObject p: products)
                resp.add(p);
            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public void doListDiscounts(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] expiresBefore, expiresAfter, expiredNow;
        try {
            expiresBefore = readParam(req, "expiresBefore", false);
            expiresAfter = readParam(req, "expiresAfter", false);
            expiredNow = readParam(req, "expiredNow", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        List<JsonObject> discounts = null;
        try {
            Data.Select select = myData.select()
                .addTable(Discounts.TABLE)
                .addValue(Data.WILDCARD);

            if(expiredNow != null) {
                select.addClause(Discounts.EXP,
                                 Data.LESS,
                                 Data.CURRENT_TIMESTAMP);
            } else if(!isEmpty(expiresBefore)) {
                select.addClause(Discounts.EXP,
                                 Data.LESS,
                                 Data.wrap(expiresBefore[0]));
            }

            if(!isEmpty(expiresAfter)) {
                select.addClause(Discounts.EXP,
                                 Data.GREATER,
                                 Data.wrap(expiresAfter[0]));
            }

            discounts = select.execute();
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();
            for(JsonObject d: discounts)
                resp.add(d);
            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public void doCalculatePrice(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String order;

        try {
            order = readParam(req, "order")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // TODO: load policy from the db
        TreeMap<Integer,Double> policy = new TreeMap<Integer,Double>();

        policy.put(0, 1.);
        policy.put(2, .5);

        double basePrice;
        int memberCount;
        try {
            JsonObject info = myData.select()
                .addTable(Orders.TABLE)
                .addTable(Products.TABLE)
                .addValue(Orders.DISCOUNT)
                .addValue(Products.PRICE)
                .addClause(Orders.PRODUCT, Data.EQ, Products.ID)
                .addClause(Orders.ID, Data.EQ, Data.wrap(order))
                .execute().get(0);

            {
                Logger lgr = Logger.getLogger(this.getClass().getName());
                lgr.log(Level.SEVERE, info.toString());
            }

            int discountId = info.getInt(Orders.DISCOUNT.split("\\.")[1]);
            basePrice = info.getJsonNumber(Products.PRICE.split("\\.")[1]).doubleValue();

            JsonObject count = myData.select()
                .addTable(Orders.TABLE)
                .addValue(Data.count(Orders.ID))
                .addClause(Orders.DISCOUNT, Data.EQ, Data.wrap(discountId))
                .execute().get(0);

            memberCount = ((JsonNumber)count.values().iterator().next()).intValue();
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        double coef = policy.floorEntry(memberCount).getValue();

        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder resp = Json.createObjectBuilder();
            resp.add("result", coef * basePrice);
            writer.writeObject(resp.build());
        } finally {
            writer.close();
        }
    }

    public Common() {
        registerMethod("listProducts", this::doListProducts);
        registerMethod("listDiscounts", this::doListDiscounts);
        registerMethod("calculatePrice", this::doCalculatePrice);
    }
}
