/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import com.google.gdata.client.*;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author davide
 */
public class GoogleCalendarUpdater {
    
    
    public static void main(String[] args) {
        
        try{
        CalendarService myService = new CalendarService("DAVIDE");
        myService.setUserCredentials("dcazzaniga@beintoo.com", "davide22");        
        URL postUrl =
        new URL("https://www.google.com/calendar/feeds/dcazzaniga@beintoo.com/private/full");
        
        
        CalendarEventEntry myEntry = new CalendarEventEntry();
        myEntry.setTitle(new PlainTextConstruct("prova"));
        myEntry.setContent(new PlainTextConstruct("cron"));
        String recurData = "DTSTART;TZID=US-Eastern:20130201T090000\r\n"
                + "EXDATE;TZID=Europe/London:20130201T092000\r\n"
                + "RRULE:FREQ=DAILY;BYHOUR=8;BYMINUTE=5;\r\n"
                + "UNTILL;TZID=Europe/London:20130209T090000"
                ;
        
        System.out.println(""+recurData);
        
        
        Recurrence recur = new Recurrence();
        recur.setValue(recurData);
        myEntry.setRecurrence(recur);
        myService.insert(postUrl, myEntry);
        }catch(Exception e){
            e.printStackTrace();
        }

//        putQuarzJobInCalendar();
        
    }
    
    public static void putQuarzJobInCalendar() throws AuthenticationException, MalformedURLException{
        
        String file = "/Users/davide/comandi/quartz-job.xml";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        CalendarService myService = new CalendarService("DAVIDE");
        myService.setUserCredentials("dcazzaniga@beintoo.com", "davide22");        
        URL postUrl =
        new URL("https://www.google.com/calendar/feeds/dcazzaniga@beintoo.com/private/full");
        
        
        
        
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();


            //parse using builder to get DOM representation of the XML file
            Document document = (Document) db.parse(file);

            Element element = document.getDocumentElement();

            NodeList crons = element.getElementsByTagName("cron");
            if(crons != null && crons.getLength() > 0) {
                    for(int i = 0 ; i < crons.getLength();i++) {

                        String name = ((Element) (((Element) crons.item(i)).getElementsByTagName("name").item(0))).getFirstChild().getNodeValue();
                        String cron =  ((Element) (((Element) crons.item(i)).getElementsByTagName("cron-expression").item(0))).getFirstChild().getNodeValue();
                        System.out.println(name+": "+cron);
                        saveEvent(myService, postUrl, name, cron);

                    }
            }
                        

        }catch(Exception e){
            e.printStackTrace();
            
        }
        
        
        
    }
    
    public static void saveEvent(CalendarService cs, URL postUrl, String title, String cron ) throws IOException, ServiceException{
        
        CalendarEventEntry myEntry = new CalendarEventEntry();
        myEntry.setTitle(new PlainTextConstruct("quarz-task"));
        
        myEntry.setContent(new PlainTextConstruct(title));
        String FREQ = parseCronExpresssion(cron);
        System.out.print(" ---- " +FREQ);
        String recurData = "DTSTART;VALUE=DATE:201310201\r\n"
        + "DTEND;VALUE=DATE:20130228;UNTIL\r\n"
        + "RRULE:+"+FREQ+"\r\n";
        
        Recurrence recur = new Recurrence();
        recur.setValue(recurData);
        myEntry.setRecurrence(recur);
        cs.insert(postUrl, myEntry);
        
    }
    
    public static String parseCronExpresssion(String crontabExpression){
                   
            String[] crontabElements = crontabExpression.split(" ");
            String FREQ = "";
            for (int field = 1; field < crontabElements.length; ++field) {
//                0 1  2 3              4 5           6    
//                s m  h Day of month   m Day of week year
//                0 45 4 ?              * MON-FRI     *  
//                RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20070904\r\n
                
                 
                  if(crontabElements[3].equals("*"))
                      FREQ = "FREQ=DAILY;" ;
                  
                  if(!crontabElements[5].equals("?")){
                      if(crontabElements[5].equals("MON-FRI")){
                          FREQ = "FREQ=WEEKLY;BYDAY=Mo-Fr;";
                      }   
                  }
                  
                  if(crontabElements[1].equals("*") && crontabElements[2].equals("*")){
                      FREQ += "BYHOUR="+crontabElements[2]+";BYMINUTE="+crontabElements[1]+";";
                  }
                
                
            }

            return FREQ;
    }
    
    
}
