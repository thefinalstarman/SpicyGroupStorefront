package org.spice.sms;

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

public class SMS extends RESTServlet {
    Data myData = new Data();

    public void doAddProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String name, _price;
        try {
            name = readParam(req, "name")[0];
            _price = readParam(req, "price")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        double price;
        try {
            price = Double.parseDouble(_price);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonObject inserted = null;
        try {
            int id = myData.insert(Products.TABLE)
                .add(Products.NAME, name)
                .add(Products.PRICE, price)
                .execute();

            inserted = myData.select()
                .addTable(Products.TABLE)
                .addValue(Data.WILDCARD)
                .addClause(Products.ID, Data.EQ, Data.wrap(id))
                .execute().get(0);
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            writer.writeObject(inserted);
        } finally {
            writer.close();
        }
    }

    public void doDeleteProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String id;
        try {
            id = readParam(req, "id")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        boolean result;
        try {
            result = myData.delete(Products.TABLE)
                .addClause(Products.ID,Data.EQ,id)
                .execute() > 0;
        } catch(SQLException e) {
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

    public void doCustomerSearch(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] name, category, product;
        try {
            name = readParam(req, "name", false);
            category = readParam(req, "category", false);
            product = readParam(req, "product", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // TODO: find the list of matching people

        List<JsonObject> result = null;
        try {
            Data.Select select = myData.select();
            select.addTable(Persons.TABLE);
            select.addValue(Persons.ID);
            select.addValue(Persons.NAME);
            select.addValue(Persons.CREDIT);
            select.addValue(Persons.ADDRESS);
            if(!isEmpty(name)) select.addClause(Persons.NAME,Data.REGEX,Data.wrap(name[0]));
            if(!isEmpty(product)) {
                select.addTable(Products.TABLE);
                select.addTable(Orders.TABLE);
                select.addClause(Orders.PERSON,Data.EQ,Persons.ID);
                select.addClause(Orders.PRODUCT,Data.EQ,Products.ID);
                select.addClause(Products.NAME,Data.REGEX,Data.wrap(product[0]));
            }
            result = select.execute();
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();

            for(JsonObject o: result) {
                resp.add(o);
            }

            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public SMS() {
        registerMethod("addProduct", this::doAddProduct);
        registerMethod("deleteProduct", this::doDeleteProduct);
        registerMethod("customerSearch", this::doCustomerSearch);
    }
}
