/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package misc;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author davide
 */
public class GoogleAnalyticsRequest {


//    private static final String CLIENT_USERNAME = "davide.niga@gmail.com";
//    private static final String CLIENT_PASS = "deejaydavide";
    private static final String CLIENT_USERNAME = "dcazzaniga@beintoo.com";
    private static final String CLIENT_PASS = "davide22";
//    private static final String CLIENT_USERNAME = "info@beintoo.com";
//    private static final String CLIENT_PASS = "pressword01";
    private static String TABLE_ID = "ga:40805375";
    private DataQuery dataQuery;
    private AnalyticsService analyticsService;

    public GoogleAnalyticsRequest(){
        try{
            System.out.println(this.CLIENT_USERNAME);
        //testTableId();
            
        this.dataQuery = new DataQuery(new URL("https://www.google.com/analytics/feeds/data"));
        this.analyticsService = new AnalyticsService("gaExportAPI_acctSample_v1.0");
        analyticsService.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void testTableId(){
        // Service Object to work with the Google Analytics Data Export API.
        AnalyticsService analyticsService = new AnalyticsService("gaExportAPI_acctSample_v1.0");

        try{
        // ClientLogin Authorization.
        analyticsService.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);
        // Construct query from a string.
        URL queryUrl = new URL("https://www.google.com/analytics/feeds/accounts/default?max-results=50");
        // Make request to the API, using AccountFeed class as the second parameter.
        AccountFeed accountFeed = analyticsService.getFeed(queryUrl, AccountFeed.class);
        // Output the data to the screen.
        System.out.println("-------- Account Feed Results --------");
        for (AccountEntry entry : accountFeed.getEntries()) {
            System.out.println(
                    "\nAccount Name  = " + entry.getProperty("ga:accountName") +
                    "\nProfile Name  = " + entry.getTitle().getPlainText() +
                    "\nProfile Id    = " + entry.getProperty("ga:profileId") +
                    "\nProfile Id    = " + entry.getProperty("ga:tableId") +
                    "\nTable Id      = " + entry.getTableId().getValue());
        }
        }catch( Exception e){
            e.printStackTrace();
        }
    }

    public List<String[]> stringRequest(List<String> dimensions, List<String> metrics, String segment, String sort, String filter,Date from ,Date to) {

        List result = new ArrayList<String>();

        Date today = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(from);
        String end = sdf.format(to);

        dataQuery.setStartDate(start);
        dataQuery.setEndDate(end);

        String dimensionStr  = "";
        for(String s: dimensions){
            dimensionStr += "ga:"+s+",";
        }

        String metricStr  = "";
        for(String s: metrics){
            metricStr += "ga:"+s+",";
        }

        dataQuery.setDimensions(dimensionStr.substring(0, dimensionStr.length()-1));
        dataQuery.setMetrics(metricStr.substring(0, metricStr.length()-1));
        if(sort!=null)
            dataQuery.setSort(sort);
        if(filter!=null){
            System.out.println(filter);
            dataQuery.setFilters(filter);
        }
        //dataQuery.setMaxResults(50);
        dataQuery.setIds(TABLE_ID);

        if(segment!=null)
            dataQuery.setSegment(segment);

        try{

        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::: " );
        System.out.println("::: Google data for: "+start+" to "+end );

        DataFeed dataFeed = analyticsService.getFeed(dataQuery.getUrl(), DataFeed.class);

        // intestazione
        int i = 0;
        String[] line = new String[dimensions.size()+metrics.size()+2];
        for(String d: dimensions){
                System.out.print(String.format("%1$30s",d)+"\t" );
                line[i] = d;
                i++;
        }
        for(String m: metrics){
                System.out.print(String.format("%1$15s", m)+"\t");
                line[i] = m;
                i++;
        }
        result.add(line);
        System.out.println("");
        // risultati

        for (DataEntry entry : dataFeed.getEntries()) {
            int j = 0;
            String[] data = new String[dimensions.size()+metrics.size()+2];
            for(String d: dimensions){
                String cell = entry.stringValueOf("ga:"+d);
                System.out.print(String.format("%1$30s", cell.substring(0, Math.min(cell.length(), 29)))+"\t" );
                data[j] = cell;
                j++;
            }
            for(String m: metrics){
                String cell = entry.stringValueOf("ga:"+m);
                System.out.print(String.format("%1$15s",cell)+"\t");
                data[j] = cell;
                j++;
            }
            result.add(data);
            System.out.println("");
        }


        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public List<String[]> request(List<String> dimensions, List<String> metrics, String segment, String sort, String filter,Date from ,Date to) {

        List result = new ArrayList<String>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(from);
        String end = sdf.format(to);

        dataQuery.setStartDate(start);
        dataQuery.setEndDate(end);

        String dimensionStr  = "";
        for(String s: dimensions){
            dimensionStr += "ga:"+s+",";
        }

        String metricStr  = "";
        for(String s: metrics){
            metricStr += "ga:"+s+",";
        }

        dataQuery.setDimensions(dimensionStr.substring(0, dimensionStr.length()-1));
        dataQuery.setMetrics(metricStr.substring(0, metricStr.length()-1));
        if(sort!=null)
            dataQuery.setSort(sort);
        if(filter!=null)
            dataQuery.setFilters(filter);
        //dataQuery.setMaxResults(50);
        dataQuery.setIds(TABLE_ID);

        if(segment!=null)
            dataQuery.setSegment(segment);

        try{

        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::: " );
        System.out.println("::: Google data for: "+start+" to "+end );

        DataFeed dataFeed = analyticsService.getFeed(dataQuery.getUrl(), DataFeed.class);

        // intestazione
        int i = 0;
        String[] line = new String[dimensions.size()+metrics.size()+2];
        for(String d: dimensions){
                System.out.print(String.format("%1$30s",d)+"\t" );
                
        }
        for(String m: metrics){
                System.out.print(String.format("%1$15s", m)+"\t");
                
        }
        System.out.println("");
        // risultati

        for (DataEntry entry : dataFeed.getEntries()) {
            int j = 0;
            String[] data = new String[dimensions.size()+metrics.size()+2];
            for(String d: dimensions){
                String cell = entry.stringValueOf("ga:"+d);
                System.out.print(String.format("%1$30s", cell.substring(0, Math.min(cell.length(), 29)))+"\t" );
                data[j] = cell;
                j++;
            }
            for(String m: metrics){
                String cell = entry.stringValueOf("ga:"+m);
                System.out.print(String.format("%1$15s",cell)+"\t");
                data[j] = cell;
                j++;
            }
            result.add(data);
            System.out.println("");
        }


        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String getTABLE_ID() {
        return TABLE_ID;
    }

    public static void setTABLE_ID(String TABLE_ID) {
        GoogleAnalyticsRequest.TABLE_ID = TABLE_ID;
    }

    

}
