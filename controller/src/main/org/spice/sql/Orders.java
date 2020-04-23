package org.spice.sql;

/*
  orderId    | int  | NO   | PRI | NULL    | auto_increment |
  | ItemId     | int  | NO   | MUL | NULL    |                |
  | discountId | int  | YES  | MUL | NULL    |                |
  | personId   | int  | NO   | MUL | NULL   
 */

public class Orders {
    public static final String TABLE = "Orders";
    public static final String ID = "Orders.orderId";
    public static final String DISCOUNT = "Orders.discountId";
    public static final String PERSON = "Orders.personId";
    public static final String PRODUCT = "Orders.itemId";
}
