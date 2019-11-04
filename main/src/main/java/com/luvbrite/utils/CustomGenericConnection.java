/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luvbrite.utils;

/**
 *
 * @author dell
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CustomGenericConnection {

    public CustomGenericConnection() {
    }

    public String contactService(String inputString, URL url, boolean SysOut, Map<String, String> headers)
            throws Exception {
        String outputStr = null;
        OutputStream outputStream = null;

        try {

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("Client established connection with " + url.toString());
            if (SysOut) {
                System.out.println(inputString);
            }

            // Setup HTTP POST parameters
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");

            if (headers != null && (!headers.isEmpty())) {
                for (String key : headers.keySet()) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }

            outputStream = connection.getOutputStream();
            outputStream.write(inputString.getBytes());
            outputStream.flush();
            outputStream.close();

            outputStr = readURLConnection(connection);
            if (SysOut) {
                System.out.println(outputStr);
            }

        } catch (Exception e) {
            System.out.println("Error sending data to server");
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        }
        return outputStr;
    }

    public String contactService(String inputString, URL url, boolean SysOut) throws Exception {

        Map<String, String> headers = new HashMap<String, String>();
        if (url.toString().indexOf("/xml") > -1) {
            headers.put("Content-Type", "text/xml; charset=utf-8");
        } else if (url.toString().indexOf("json") > -1) {
          //  headers.put("Content-Type", "application/json; charset=UTF-8");
             headers.put("Content-Type", "text/plain");
        }

        return contactService(inputString, url, SysOut, headers);
    }

    /**
     * This method read all of the data from a URL connection to a String
     */
    private String readURLConnection(HttpURLConnection uc) throws Exception {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String response = "";
            while ((response = reader.readLine()) != null) {
                buffer.append(response);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Could not read from URL: " + e.toString());
            e.printStackTrace();
            return "Error getting response";
        } finally {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }
        return buffer.toString();
    }
}
