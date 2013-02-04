/* 
 * TPCW_Database.java - Contains all of the code involved with database
 *                      accesses, including all of the JDBC calls. These
 *                      functions are called by many of the servlets.
 *
 ************************************************************************
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.
 *
 * Copyright (C) 1999, 2000 by Harold Cain, Timothy Heil, Milo Martin, 
 *                             Eric Weglarz, Todd Bezenek.
 *
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 * Everyone is granted permission to copy, modify and redistribute
 * this code under the following conditions:
 *
 * This code is distributed for non-commercial use only.
 * Please contact the maintainer for restrictions applying to 
 * commercial use of these tools.
 *
 * Permission is granted to anyone to make or distribute copies
 * of this code, either as received or modified, in any
 * medium, provided that all copyright notices, permission and
 * nonwarranty notices are preserved, and that the distributor
 * grants the recipient permission for further redistribution as
 * permitted by this document.
 *
 * Permission is granted to distribute this code in compiled
 * or executable form under the same conditions that apply for
 * source code, provided that either:
 *
 * A. it is accompanied by the corresponding machine-readable
 *    source code,
 * B. it is accompanied by a written offer, with no time limit,
 *    to give anyone a machine-readable copy of the corresponding
 *    source code in return for reimbursement of the cost of
 *    distribution.  This written offer must permit verbatim
 *    duplication by anyone, or
 * C. it is distributed by someone who received only the
 *    executable form, and is accompanied by a copy of the
 *    written offer of source code that they received concurrently.
 *
 * In other words, you are welcome to use, share and improve this codes.
 * You are forbidden to forbid anyone else to use, share and improve what
 * you give them.
 *
 ************************************************************************
 *
 * Changed 2003 by Jan Kiefer.
 *
 ************************************************************************/

import java.io.*;
// import java.util.concurrent.Callable;
import java.net.URL;
import java.sql.*;
import java.lang.Math.*;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;

import tpcw_dto.*;

import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;

// import pt.ist.fenixframework.example.tpcw.TransactionalCommand;
// import pt.ist.fenixframework.example.tpcw.TxManager;
// import pt.ist.fenixframework.example.tpcw.TxSystem;
import pt.ist.fenixframework.example.tpcw.database.DataAccess;
import pt.ist.fenixframework.Atomic;

public class TPCW_Database {
    private static final Void VOID = null;

    private static TransactionManager txManager;

    private static final Object INIT_LOCK = new Object();

    static {
        synchronized (INIT_LOCK) {
            pt.ist.fenixframework.example.tpcw.Init.initializeApp();
            txManager = FenixFramework.getTransactionManager();
        }

        // new Thread(new Runnable() {
        //         public void run() {
        //             System.out.println("=======================================Starting gc thread");
        //             while (true) {
        //                 try {
        //                     Thread.sleep(20000);
        //                 } catch (Exception e) {
        //                     e.printStackTrace();
        //                 }
        //                 System.out.println("GC'ing " + Thread.currentThread());
        //                 System.gc();
        //             }
        //         }
        //     }).start();
    }

    // @Atomic
    public static String[] getName(final int c_id) {
        return txManager.withTransaction(new CallableWithoutException<String[]>() {
                public String[] call() {
                    return DataAccess.getName(c_id);
                }
            });
    }

    // @Atomic
    public static Book getBook(final int i_id) {
        return txManager.withTransaction(new CallableWithoutException<Book>() {
                public Book call() {
                    return DataAccess.getBook(i_id);
                }
            });
    }

    // @Atomic
    public static Customer getCustomer(final String UNAME) {
        return txManager.withTransaction(new CallableWithoutException<Customer>() {
                public Customer call() {
                    return DataAccess.getCustomer(UNAME);
                }
            });
    }

    // @Atomic
    public static Vector doSubjectSearch(final String search_key) {
        return txManager.withTransaction(new CallableWithoutException<Vector>() {
                public Vector call() {
                    return DataAccess.doSubjectSearch(search_key);
                }
            });
    }

    // @Atomic
    public static Vector doTitleSearch(final String search_key) {
        return txManager.withTransaction(new CallableWithoutException<Vector>() {
                public Vector call() {
                    return DataAccess.doTitleSearch(search_key);
                }
            });
    }

    // @Atomic
    public static Vector doAuthorSearch(final String search_key) {
        return txManager.withTransaction(new CallableWithoutException<Vector>() {
                public Vector call() {
                    return DataAccess.doAuthorSearch(search_key);
                }
            });
    }

    // @Atomic
    public static Vector getNewProducts(final String subject) {
        return txManager.withTransaction(new CallableWithoutException<Vector>() {
                public Vector call() {
                    return DataAccess.getNewProducts(subject);
                }
            });
    }

    // @Atomic
    public static Vector getBestSellers(final String subject) {
        return txManager.withTransaction(new CallableWithoutException<Vector>() {
                public Vector call() {
                    return DataAccess.getBestSellers(subject);
                }
            });
    }

    // @Atomic
    public static void getRelated(final int i_id, final Vector i_id_vec, final Vector i_thumbnail_vec) {
        txManager.withTransaction(new CallableWithoutException<Void>() {
                public Void call() {
                    DataAccess.getRelated(i_id, i_id_vec, i_thumbnail_vec);
                    return VOID;
                }
            });
    }

    // @Atomic
    public static void adminUpdate(final int i_id, final double cost, final String image, final String thumbnail) {
        txManager.withTransaction(new CallableWithoutException<Void>() {
                public Void call() {
                    DataAccess.adminUpdate(i_id, cost, image, thumbnail);
                    return VOID;
                }
            });
    }

    // @Atomic
    public static String GetUserName(final int C_ID) {
        return txManager.withTransaction(new CallableWithoutException<String>() {
                public String call() {
                    return DataAccess.GetUserName(C_ID);
                }
            });
    }

    // @Atomic
    public static String GetPassword(final String C_UNAME) {
        return txManager.withTransaction(new CallableWithoutException<String>() {
                public String call() {
                    return DataAccess.GetPassword(C_UNAME);
                }
            });
    }

    // @Atomic
    public static Order GetMostRecentOrder(final String c_uname, final Vector order_lines) {
        return txManager.withTransaction(new CallableWithoutException<Order>() {
                public Order call() {
                    return DataAccess.GetMostRecentOrder(c_uname, order_lines);
                }
            });
    }

    // ********************** Shopping Cart code below ************************* 

    // Called from: TPCW_shopping_cart_interaction 
    // @Atomic
    public static int createEmptyCart() {
        return txManager.withTransaction(new CallableWithoutException<Integer>() {
                public Integer call() {
                    return DataAccess.createEmptyCart();
                }
            });
    }

    // @Atomic
    public static Cart doCart(final int SHOPPING_ID, final Integer I_ID, final Vector ids, final Vector quantities) {
        return txManager.withTransaction(new CallableWithoutException<Cart>() {
                public Cart call() {
                    return DataAccess.doCart(SHOPPING_ID, I_ID, ids, quantities);
                }
            });
    }

    //This function finds the shopping cart item associated with SHOPPING_ID
    //and I_ID. If the item does not already exist, we create one with QTY=1,
    //otherwise we increment the quantity.
    
    // @Atomic
    public static Cart getCart(final int SHOPPING_ID, final double c_discount) {
        return txManager.withTransaction(new CallableWithoutException<Cart>() {
                public Cart call() {
                    return DataAccess.getCartDTO(SHOPPING_ID, c_discount);
                }
            });
    }


    // ************** Customer / Order code below *************************

    // @Atomic
    public static void refreshSession(final int C_ID) {
        txManager.withTransaction(new CallableWithoutException<Void>() {
                public Void call() {
                    DataAccess.refreshSession(C_ID);
                    return VOID;
                }
            });
    }

    // @Atomic
    public static Customer createNewCustomer(final Customer cust) {
        return txManager.withTransaction(new CallableWithoutException<Customer>() {
                public Customer call() {
                    return DataAccess.createNewCustomer(cust);
                }
            });
    }

    //BUY CONFIRM 

    // @Atomic
    public static BuyConfirmResult doBuyConfirm(final int shopping_id, final int customer_id, final String cc_type,
                                                final long cc_number, final String cc_name, final Date cc_expiry,
                                                final String shipping) {
        return txManager.withTransaction(new CallableWithoutException<BuyConfirmResult>() {
                public BuyConfirmResult call() {
                    return DataAccess.doBuyConfirm(shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping);
                }
            });
    }

    // @Atomic
    public static BuyConfirmResult doBuyConfirm(final int shopping_id, final int customer_id, final String cc_type,
                                                final long cc_number, final String cc_name, final Date cc_expiry,
                                                final String shipping, final String street_1, final String street_2,
                                                final String city, final String state, final String zip, final String country) {
        return txManager.withTransaction(new CallableWithoutException<BuyConfirmResult>() {
                public BuyConfirmResult call() {
                    return DataAccess.doBuyConfirm(shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping,
                                                   street_1, street_2, city, state, zip, country);
                }
            });
    }
    
}

