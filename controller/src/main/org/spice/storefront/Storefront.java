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
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.Data;

public class Storefront extends RESTServlet {
    Data myDataSt = new Data();

    public void doSubmitOrder(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);
        
        // Will item parameters be here as well?
        String cname, caddress;
        String ccard, discountID;
        String itemId;
        try {
            cname = readParam(req, "cname")[0];
            caddress = readParam(req, "caddress")[0];
            ccard = readParam(req, "ccard")[0];
            discountID = readParam(req, "discountId")[0];
            itemId = readParam(req,"itemId")[0];

        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        Data.Person dataInsert = null;
        try{
            dataInsert = myDataSt.createPerson(cname,caddress,ccard);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);
        try {
            writer.writeObject(dataInsert.toJson());
        } finally {
            writer.close();
        }

        int disId = Integer.parseInt(discountID);
        int itId = Integer.parseInt(itemId);
        Data.Orders dataOrder = null;
        try{
            dataOrder = myDataSt.createOrder(itId,dataInsert.id,disId);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

        JsonWriter Ordwriter = setupJson(response);
        try{
            Ordwriter.writeObject(dataOrder.toJson());
        } finally {
            Ordwriter.close();
        }

    }

    public void generateDiscountCode(HttpServletRequest req, HttpServletResponse response) {

    }

}