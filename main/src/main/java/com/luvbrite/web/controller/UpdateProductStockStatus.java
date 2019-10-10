package com.luvbrite.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.GenericConnection;
import com.luvbrite.web.controller.admin.ProductsController;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Product;
import java.net.URL;
import java.util.logging.Level;

@Controller
@RequestMapping(value = "/updateproductStockStatus")
public class UpdateProductStockStatus {

    private static Logger logger = Logger.getLogger(UpdateProductStockStatus.class);

    private static final String postTookanNotification = "http://localhost:8084/inventory/apps/getTookanNotification?json";

    @Autowired
    ProductDAO prdDao;

    @RequestMapping(value = "/updateProds", method = RequestMethod.POST)
    public ResponseEntity<String> updateProductStockStatus(@RequestBody String statusAndMongoID) {
        logger.debug("raw String recieved-->" + statusAndMongoID);
        System.out.println("raw String recieved-->" + statusAndMongoID);
        try {
            statusAndMongoID = URLDecoder.decode(statusAndMongoID, StandardCharsets.UTF_8.toString());
            logger.debug("After Decoding String recieved-->" + statusAndMongoID);

        } catch (UnsupportedEncodingException e) {
            logger.error("Exception while Decoding data recieved from Inventory");
            e.printStackTrace();
        }

        String[] statusAndid = statusAndMongoID.split("~");
        String stockStatus = statusAndid[0];
        Long mongoId = Long.parseLong(statusAndid[1]);

        Product productDb = prdDao.get(mongoId);

        if (productDb != null) {
            productDb.setStockStat(stockStatus);
            prdDao.save(productDb);
            return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
        } else {
            logger.debug("Can not find mongo productid " + mongoId);
            return new ResponseEntity<String>("Can not find mongo productid " + mongoId, HttpStatus.OK);
        }

    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResponseEntity<String> getTookanNotification(@RequestBody String tookanNotification) {

        String tookanNotification1 = "{\"job_id\":50725730,\"created_by\":5,\"order_id\":\"1234567\",\"recurring_id\":\"0\",\"recurring_count\":0,\"partner_order_id\":null,\"team_id\":228977,\"vertical\":0,\"merchant_id\":0,\"geofence\":0,\"tags\":\"\",\"auto_assignment\":0,\"dispatcher_id\":null,\"job_hash\":\"aac028fed245cb6efb10eba01c2f6cba\",\"has_pickup\":0,\"has_delivery\":1,\"is_routed\":0,\"pickup_delivery_relationship\":\"5072573015707059571574323\",\"job_description\":\"Testing through Luvbrite Main\",\"job_pickup_datetime\":\"Invalid date\",\"job_pickup_name\":\"\",\"job_pickup_phone\":\"0\",\"job_delivery_datetime\":\"10/15/2019 09:00 pm\",\"job_pickup_latitude\":\"\",\"job_pickup_longitude\":\"\",\"job_pickup_address\":\"\",\"job_pickup_email\":null,\"job_latitude\":\"17.2801514\",\"job_longitude\":\"-62.6890382\",\"customer_username\":\"TestCustomer\",\"customer_email\":\"TestCustomer\",\"customer_phone\":\"+1 201-555-5555\",\"job_address\":\"frigate bay 2\",\"creation_datetime\":\"2019-10-10T11:12:37.000Z\",\"fleet_id\":419408,\"user_id\":219320,\"fleet_rating\":null,\"customer_comment\":null,\"is_customer_rated\":0,\"customer_id\":14845457,\"arrived_datetime\":\"2019-10-10 16:43:28\",\"started_datetime\":\"2019-10-10 16:43:18\",\"completed_datetime\":\"2019-10-10 16:43:28\",\"acknowledged_datetime\":\"2019-10-10 16:43:4\",\"job_status\":2,\"is_active\":1,\"job_type\":1,\"completed_by_admin\":0,\"open_tracking_link\":0,\"timezone\":\"-330\",\"job_time\":\"2019-10-15 09:00:0\",\"job_date\":\"2019-10-15T00:00:00.000Z\",\"job_time_utc\":\"2019-10-15T15:30:00.000Z\",\"job_date_utc\":\"2019-10-15T00:00:00.000Z\",\"total_distance_travelled\":0,\"form_id\":null,\"customer_rating\":null,\"driver_comment\":null,\"remarks\":null,\"barcode\":null,\"ride_type\":0,\"matched_pickup_delivery_relationship\":null,\"job_vertical\":0,\"days_started\":\"0\",\"tookan_shared_secret\":\"ZraU2QLFxyZZ3YrG\",\"distance_in\":\"KM\",\"access_token\":\"13a76798c468b7571eb7d2a0c66267cc\",\"domain\":null,\"agent_workflow\":0,\"fleet_name\":\"Hemraj Shaqawal\",\"fleet_email\":\"hemraj.shaqawal@logicoy.com\",\"fleet_phone\":\"+91 89559 09361\",\"fleet_latitude\":\"13.0057483\",\"fleet_longitude\":\"77.6507701\",\"transport_type\":2,\"license\":\"\",\"transport_desc\":\"\",\"fleet_image\":\"app/img/user.png\",\"job_details_by_fleet\":419408,\"external_fleet_id\":\"\",\"fleet_vehicle_type\":2,\"fleet_vehicle_color\":\"\",\"fleet_vehicle_description\":\"\",\"task_status\":2,\"promo_used\":\"\",\"custom_fields\":[{\"label\":\"Subtotal\",\"display_name\":\"Subtotal\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"101\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"DeliveryFee\",\"display_name\":\"DeliveryFee\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"2\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"},{\"label\":\"Total\",\"display_name\":\"Total\",\"data_type\":\"Text\",\"app_side\":\"1\",\"required\":1,\"value\":1,\"data\":\"3\",\"input\":\"\",\"before_start\":0,\"template_id\":\"Cannabis_Delivery\"}],\"ref_images\":[\"http://tookanapp.com/wp-content/uploads/2015/11/logo_dark.png\",\"http://tookanapp.com/wp-content/uploads/2015/11/logo_dark.png\"],\"tracking_link\":\"https://app.tookanapp.com/tracking/index.html?jobID=aac028fed245cb6efb10eba01c2f6cba\",\"task_history\":[{\"id\":315554028,\"job_id\":50725730,\"fleet_id\":null,\"fleet_name\":null,\"latitude\":null,\"longitude\":null,\"type\":\"state_changed\",\"description\":\"Created By Ali-219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-10T11:12:37.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='created'>CREATED</span>By Ali-219320\"},{\"id\":315554731,\"job_id\":50725730,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057483\",\"longitude\":\"77.6507701\",\"type\":\"state_changed\",\"description\":\"Assigned Hemraj Shaqawal to task - 219320\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-10T11:13:01.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='assigned'>ASSIGNED</span>Hemraj Shaqawal to task - 219320\"},{\"id\":315554812,\"job_id\":50725730,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057483\",\"longitude\":\"77.6507701\",\"type\":\"state_changed\",\"description\":\"Accepted at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-10T11:13:04.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='accepted'>ACCEPTED</span>at\"},{\"id\":315555221,\"job_id\":50725730,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057483\",\"longitude\":\"77.6507701\",\"type\":\"state_changed\",\"description\":\"Started at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-10T11:13:18.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='started'>STARTED</span>at\"},{\"id\":315555475,\"job_id\":50725730,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057483\",\"longitude\":\"77.6507701\",\"type\":\"signature_image_added\",\"description\":\"https://tookan.s3.amazonaws.com/acknowledgement_images/tQXp1570706007113-tasksignature.png\",\"extra_fields\":\"\",\"creation_datetime\":\"2019-10-10T11:13:27.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='uploaded'>UPLOADED</span>by Hemraj Shaqawal\"},{\"id\":315555491,\"job_id\":50725730,\"fleet_id\":419408,\"fleet_name\":\"Hemraj Shaqawal\",\"latitude\":\"13.0057483\",\"longitude\":\"77.6507701\",\"type\":\"state_changed\",\"description\":\"Successful at\",\"extra_fields\":null,\"creation_datetime\":\"2019-10-10T11:13:28.000Z\",\"creation_date\":\"2019-10-10T00:00:00.000Z\",\"label_description\":\"<span class='successful'>SUCCESSFUL</span>at\"}],\"job_state\":\"Successful\",\"task_state\":\"SUCCESSFUL\",\"completed_datetime_formatted\":\"10 Oct 2019 04:43 pm\",\"started_datetime_formatted\":\"10 Oct 2019 04:43 pm\",\"arrived_datetime_formatted\":\"10 Oct 2019 04:43 pm\",\"acknowledged_datetime_formatted\":\"10 Oct 2019 04:43 pm\",\"job_token\":\"5072573015707059571574323\",\"job_time_formatted\":\"15 Oct 2019 09:00 pm\",\"timestamp_diff\":10000,\"total_time_spent_at_task_till_completion\":\"10 seconds\",\"job_pickup_datetime_formatted\":\"Invalid date\",\"job_delivery_datetime_formatted\":\"15 Oct 2019 09:00 pm\",\"total_distance\":\"0.00 Km\",\"webhook_type\":0,\"format\":\"application/json\",\"template_key\":\"SUCCESSFUL\",\"is_internal\":0,\"full_tracking_link\":\"https://app.tookanapp.com/tracking/index.html?jobID=aac028fed245cb6efb10eba01c2f6cba\"}";

        System.out.println("tookanNotification:--->" + tookanNotification);

        /* POST TO INVENTORY SERVER */
        GenericConnection conn = new GenericConnection();
        try {
            String resp = conn.contactService(tookanNotification1, new URL(postTookanNotification), false);
        } catch (Exception ex) {
            logger.error("Exception occured while sending tookanNotification to  inventory", ex);
        }

        return new ResponseEntity<String>("Recieved Tookan Response", HttpStatus.OK);
    }

}
