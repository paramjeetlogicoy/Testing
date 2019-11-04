/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luvbrite.apis;

/**
 *
 * @author dell
 */
public class APIs {

    /**
     * *LOCAL ENVIRONMENT**
     */
	/*******Netbeans************/
//    public  final String POST_PRODLIST_URL = "http://localhost:8085/inventory/apps/acceptproductlist?json";
//    public  final String NEW_ORDER_URL = "http://localhost:8085/inventory/apps/a-ordermeta?json";
//    public  final String UPDATE_ORDER_URL = "http://localhost:8085/inventory/apps/a-c-ordermeta?json";
//    public  final String POST_TOOKAN_NOTICATION = "http://localhost:8085/inventory/apps/getTookanNotification?json";

    /***Eclipse****/
    public  final String POST_PRODLIST_URL = "http://localhost:8081/inventory/apps/acceptproductlist?json";
    public  final String NEW_ORDER_URL = "http://localhost:8081/inventory/apps/a-ordermeta?json";
    public  final String UPDATE_ORDER_URL = "http://localhost:8081/inventory/apps/a-c-ordermeta?json";
    public  final String POST_TOOKAN_NOTICATION = "http://localhost:8081/inventory/apps/getTookanNotification?json";

    
    /**
     * *PRODUCTION APIS**
//     */
//    public  final String POST_PRODLIST_URL = "https://www.luvbrite.com/inventory/apps/acceptproductlist?json";
//    public  final String NEW_ORDER_URL = "https://www.luvbrite.com/inventory/apps/a-ordermeta?json";
//    public  final String UPDATE_ORDER_URL = "https://www.luvbrite.com/inventory/apps/a-c-ordermeta?json";
//    public  final String POST_TOOKAN_NOTICATION = "https://www.luvbrite.com/inventory/apps/getTookanNotification?json";

    
     /**
     * *TEST ENVIRONMENT**
     */
//    public  final String POST_PRODLIST_URL = "http://localhost:8080/inventory/apps/acceptproductlist?json";
//    public  final String NEW_ORDER_URL = "http://localhost:8080/inventory/apps/a-ordermeta?json";
//    public  final String UPDATE_ORDER_URL = "http://localhost:8080/inventory/apps/a-c-ordermeta?json";
//    public  final String POST_TOOKAN_NOTICATION = "http://localhost:8080/inventory/apps/getTookanNotification?json";

}
