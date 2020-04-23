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
import javax.json.JsonValue;
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

    private double getDiscountedPrice(String order) throws SQLException {
        // TODO: load policy from the db
        TreeMap<Integer,Double> policy = new TreeMap<Integer,Double>();

        policy.put(0, 1.);
        policy.put(2, .5);

        double basePrice;
        int memberCount;
        JsonObject info = myData.select()
            .addTable(Orders.TABLE)
            .addTable(Products.TABLE)
            .addValue(Orders.DISCOUNT, "A")
            .addValue(Products.PRICE, "B")
            .addClause(Orders.PRODUCT, Data.EQ, Products.ID)
            .addClause(Orders.ID, Data.EQ, Data.wrap(order))
            .execute().get(0);

        int discountId = info.getInt("A");
        basePrice = info.getJsonNumber("B").doubleValue();

        JsonObject count = myData.select()
            .addTable(Orders.TABLE)
            .addValue(Data.count(Orders.ID))
            .addClause(Orders.DISCOUNT, Data.EQ, Data.wrap(discountId))
            .execute().get(0);

        memberCount = ((JsonNumber)count.values().iterator().next()).intValue();

        double coef = policy.floorEntry(memberCount).getValue();
        return Math.ceil(100 * coef * basePrice) / 100.;
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

        double result;
        try {
            result = getDiscountedPrice(order);
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder resp = Json.createObjectBuilder();
            resp.add("result", result);
            writer.writeObject(resp.build());
        } finally {
            writer.close();
        }
    }

    public void doListOrders(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] discountId, withDiscount;
        try {
            discountId = readParam(req, "discountId", false);
            withDiscount = readParam(req, "withDiscount", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        List<JsonObject> orders = null;
        try {
            Data.Select select = myData.select()
                .addTable(Orders.TABLE)
                .addTable(Products.TABLE)
                .addTable(Persons.TABLE)
                .addTable(Discounts.TABLE)
                .addTable(Credits.TABLE)
                .addValue(Orders.ID,"orderId")
                .addValue(Discounts.ID)
                .addValue(Products.ID)
                .addValue(Products.PRICE)
                .addValue(Products.NAME)
                .addValue(Persons.ID)
                .addValue(Persons.NAME, "customer")
                .addValue(Persons.ADDRESS, "address")
                .addValue(Credits.ID, "creditId")
                .addValue(Credits.NAME, "cardName")
                .addValue(Credits.NUMBER, "cardNumber")
                .addValue(Credits.MONTH, "cardExpMonth")
                .addValue(Credits.YEAR, "cardExpYear")
                .addValue(Credits.CVV, "cardCVV")
                .addValue(Credits.ZIP, "cardZIP")
                .addClause(Orders.PERSON, Data.EQ, Persons.ID)
                .addClause(Orders.PRODUCT, Data.EQ, Products.ID)
                .addClause(Orders.DISCOUNT, Data.EQ, Discounts.ID);

            if(!isEmpty(discountId)) {
                select.addClause(Discounts.ID, Data.EQ, Data.wrap(discountId[0]));
            }

            orders = select.execute();
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if(withDiscount != null) {
            ArrayList<JsonObject> next = new ArrayList<JsonObject>();
            for(JsonObject o: orders) {
                JsonObjectBuilder out = Json.createObjectBuilder();
                for(Map.Entry<String,JsonValue> entries: o.entrySet()) {
                    out.add(entries.getKey(), entries.getValue());
                }

                try {
                    String orderId = Integer.toString(o.getInt("orderId"));
                    out.add("priceAdj", getDiscountedPrice(orderId));
                } catch(SQLException e) {
                    trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return;
                }

                next.add(out.build());
            }

            orders = next;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();
            for(JsonObject o: orders)
                resp.add(o);
            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public Common() {
        registerMethod("listProducts", this::doListProducts);
        registerMethod("listDiscounts", this::doListDiscounts);
        registerMethod("listOrders", this::doListOrders);
        registerMethod("calculatePrice", this::doCalculatePrice);
    }
}
