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
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.Data;

public class SMS extends RESTServlet {
    Data myData = new Data();

    public void doAddProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String name, _price;
        String[] category;
        try {
            name = readParam(req, "name")[0];
            _price = readParam(req, "price")[0];

            // optional for now
            category = readParam(req, "category", false);
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

        Data.Product inserted = null;
        try {
            inserted = myData.createProduct(name, price);
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            writer.writeObject(inserted.toJson());
        } finally {
            writer.close();
        }
    }

    public void doDeleteProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String _id;
        try {
            _id = readParam(req, "id")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        int id;
        try {
            id = Integer.parseInt(_id);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        boolean result;
        try {
            result = myData.deleteProduct(id);
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

        List<Data.Person> result;
        try {
            result = myData.searchCustomers(isEmpty(name) ? null : name[0],
                                            isEmpty(product) ? null : product[0]);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();

            for(Data.Person p: result) {
                resp.add(p.toJson());
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
