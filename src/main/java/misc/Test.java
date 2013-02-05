
package misc;

import com.beintoo.commons.bean.CountryBean;
import com.beintoo.commons.bean.FacebookBean;
import com.beintoo.commons.bean.ReportBannerBean;
import com.beintoo.commons.bean.UserLevelBean;
import com.beintoo.commons.database.AdminReportEngine;
import com.beintoo.commons.database.DatabaseConnector;
import com.beintoo.commons.enums.GoogleAnalyticsTrackerEnum;
import com.beintoo.commons.enums.UserLevelEnum;
import com.beintoo.commons.helper.UserHelper;
import com.beintoo.commons.helper.VgoodHelper;
import com.beintoo.commons.setting.CommonSetting;
import com.beintoo.commons.util.FbUserManager;
import com.beintoo.entities.MediaPlanner;
import com.beintoo.entities.User;
import com.beintoo.entities.UserCredit;
import com.beintoo.entities.Vgood;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import locator.CountryMap;
import locator.WorldMap;
import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author davide
 */
public class Test {

    public static void main(String[] args) throws Exception {
        
        quarzJobXmlReader();
        
//        Date from = new Date();
//        from = new Date(new Date().getTime() - 1 * 86400000);
//        Date to = new Date(new Date().getTime() - 1 * 86400000);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        
//        from = sdf.parse("2013-01-22");
//        to = sdf.parse("2013-01-23");
//       
//        ReportBannerBean a = new ReportBannerBean(to, 1, null, null, null);
//        ReportBannerBean b = new ReportBannerBean(to, 2, null, null, null);
//        
//        System.out.println("--------------------------------"+a.equals(b));
//        
//        
//        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
//        
//        AdminReportEngine are = new AdminReportEngine(null);
//        
//        List<ReportBannerBean> results =  are.reportBanner(from, to, em);
//        
//        List<ReportBannerBean> out = are.getOutputFromReportBanner( results , 
//                null, false, 
//                "NEODATA", false, 
//                null, true, 
//                true);
//        System.out.println("::::: "+out.size());
//        for(ReportBannerBean rbb :out){
//            System.out.println(""+rbb.toString());
//            
//        }
        
        
//        
//        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
//        MediaPlanner mp = em.find(MediaPlanner.class, 3 );
//        
//        System.out.println(" Vgood[1111111], MediaPlanner["+mp.getId()+"] : ["+mp.getStartDate()+","+mp.getEndDate()+"]\n");
        
//        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
//        User person = em.find(User.class, 18242);
//        
//        List<UserCredit> ucs = UserHelper.getUserCredit(em, person, null, 10);
//        
//        for (UserCredit uc : ucs) {
//            
//            System.out.println(""+uc.toString());
            
//        }
        
        
//        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
//        
//        em.getTransaction().begin();
//        User u = em.find(User.class, 18242);
//        
//        updateUserLevel(u);
//        em.getTransaction().commit();
//        em.close();
//        
//        googleTest();

//        String from = null;
//        String to = null;
//
//        List<CountryBean> list = userCountryStats(from, to, "IT" );
//        System.out.println("::::::::::::::::::::::::::::::::::::::::");
//        float tot = 0.0f;
//        long totU = 0;
//        System.out.print(String.format("%1$-30s","city")+
//                                   String.format("%1$-8s","users")+
//                                   String.format("%1$10s%n","%"));
//        for(CountryBean cb: list ){
//            
//            try{
//                System.out.print(String.format("%1$-30s",cb.getIso().replaceAll(" ", "_"))+
//                                   String.format("%1$-8s",cb.getUnits())+
//                                   String.format("%10.2f%n",cb.getPercentage()).replace(",", "."));
//                totU += cb.getUnits();
//                tot += cb.getPercentage();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            
//        }
//        System.out.println("::::::: "+totU);
//        System.out.println("::::::: "+tot);

    }

    private static void googleTest() throws ParseException {
        ////////////////////////////////////////////////////////////////////////////////////////////////

        Date from = new Date();
        from = new Date(new Date().getTime() - 1 * 86400000);
        Date to = new Date(new Date().getTime() - 1 * 86400000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        from = sdf.parse("2013-01-23");
        to = sdf.parse("2013-01-23");
        
        List<String[]> result = new ArrayList<String[]>();
        com.beintoo.commons.database.GoogleAnalyticsRequest gar = new com.beintoo.commons.database.GoogleAnalyticsRequest();
        List<String> dimensions = new ArrayList<String>();
        List<String> metrics = new ArrayList<String>();

        //dimensions.add("date");
        //dimensions.add("eventAction");
        //dimensions.add("eventLabel");
        //metrics.add("uniqueEvents");
        
        String sort = null;
        String filter = null;
        
        

        filter = "ga:eventLabel=~185\\+,ga:eventLabel=~183\\+;ga:eventAction==InAppClick,ga:eventAction==InAppPlayerEmailRequest";
        dimensions.add("date");
        dimensions.add("eventAction");
        dimensions.add("eventLabel");
        metrics.add("uniqueEvents");
        result = gar.request(dimensions, metrics, GoogleAnalyticsTrackerEnum.MOBILE, sort, filter, from, to);
            
        
        if (result.size() > 0) {
                System.out.println("ReportVgood : GARequest result.size() = " + result.size());
            }

            boolean first = true;
            for (String[] ss : result) {
                if (first) {
                    first = false;
                    continue;
                }
                String action = ss[1];
                int clicks = Integer.parseInt(ss[3]);
                String[] campaign = ss[2].split("\\+-\\+");
                // int customer_id = Integer.parseInt(campaign[0]);
                int vgood_id = Integer.parseInt(campaign[1]);
               
                    System.out.println("ReportVgood - " + vgood_id);
                    
                        if (action.equals("InAppClick")) {
                            System.out.println("   ReportVgood - " + vgood_id + " -  convertedVgoods > cus_1 ");
                            
                            System.out.println("   ReportVgood - InAppClick " + vgood_id + " - convertedVgoods =" + clicks);
                            
                        }
                        if (action.equals("InAppPlayerEmailRequest")) {
                            System.out.println("   ReportVgood - InAppPlayerEmailRequest " + vgood_id + " - playerConvertedVgoos =" + clicks);
                        }
                    
            }
            


    }

    private static void reafFile() {

        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;

            FileOutputStream fos = new FileOutputStream("C:/Users/davide/Desktop/beintoo_test.sql");
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            try {

                fis = new FileInputStream("C:/Users/davide/Desktop/beintoo_prod.201108051648.sql");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                String line = "";
                int i = 0;


                while ((line = buf.readLine()) != null) {

                    if (i > 2500 && i < 2618) {


                        osw.write(line + "\n");

                        System.out.println("" + line);

                    }
                    i++;

                }
                osw.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    fos.close();
                }
                if (buf != null) {
                    buf.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static List<CountryBean> userCountryStats(String from, String to, String country) {

        List<CountryBean> geos = new ArrayList<CountryBean>();

        try {

            String query = "";

            query = "   SELECT u.latitude lat , u.longitude lon, 1 c "
                    + " FROM  user u  "
                    + " WHERE u.latitude IS NOT NULL   "
                    + " AND u.country = '" + country + "' ";

            if (from != null) {
                query += " AND u.creationdate >= '" + from + "'";
            }
            if (to != null) {
                query += " AND u.creationdate < '" + to + "'  ";
            }

            //query   += " GROUP BY lat, lon ";

            System.out.println("" + query);
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            CountryMap cm = new CountryMap(country, 50000l);
            long total = 0L;
            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;

                try {

                    float lat = ((Number) objectArray[0]).floatValue() + 90;
                    float lon = ((Number) objectArray[1]).floatValue() + 180;

                    long unit = ((Number) objectArray[2]).longValue();

                    total += unit;
                    String city = "--";
                    city = cm.getCountry(lat, lon);

                    CountryBean cb = new CountryBean(city);
                    int j = geos.indexOf(cb);
                    if (j > -1) {
                        geos.get(j).addUnits(unit);
                    } else {
                        cb.setUnits(unit);
                        geos.add(cb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            System.out.println(" ::::::: TOTAL " + total);
            if (total > 0) {
                for (CountryBean cb : geos) {
                    cb.setPercentage(total);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(geos);
        return geos;
    }

    private static void correctorISOCOUNTRY_2() {


        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

        try {

            String query = " ";
            int notFound = 0;
            int counter = 0;
            int step = 50000;
            WorldMap worldMap = WorldMap.getInstance();

            while (true) {
                File destinationFile = new File("writeCOUNTRY_UK.sql");
                FileOutputStream fos = new FileOutputStream(destinationFile);
                OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

                int from = counter * step;
                int to = from + step;

                query = "   SELECT u.id , u.email, u.latitude, u.longitude"
                        + " , substring_index(email, '.', -1) suffix "
                        //                        + " , p.latitude, p.longitude "
                        + " FROM user u "
                        //                        + " LEFT OUTER JOIN player p"
                        //                        + " ON u.id = p.user_id AND p.latitude is not null "
                        + " WHERE country is null "
                        + " AND u.latitude is null"
                        //                        + " AND u.id >  1199243 "
                        //                        + " AND u.id <=  1207940 "
                        + " GROUP BY u.id "
                        + " HAVING suffix = 'UK'";

//                System.out.println("::: " + from + " > " + to);
                Query q = em.createNativeQuery(query);
                List results = new ArrayList<Object>();
                results = q.getResultList();

                for (int i = 0; i < results.size(); i++) {

                    Object obj = results.get(i);
                    Object[] objectArray = (Object[]) obj;

                    boolean ok = false;

                    long id = ((Long) objectArray[0]).longValue();
                    String email = (String) objectArray[1];
                    String suffix = (String) objectArray[4];

//                    float lat;
//                    float lon;

                    if (email.contains("qq.com")) {
                        out.write("UPDATE beintoo_v1.user SET address=coalesce(address,country) , country= 'CN' WHERE id=" + id + ";\n");
                        ok = true;
                    }

                    if (!ok) {

                        try {
                            String s = worldMap.iso2countryName(suffix.toUpperCase());
                            if (s != null) {
                                out.write("UPDATE beintoo_v1.user SET address=coalesce(address,country) , country= '" + suffix.toUpperCase() + "' WHERE id=" + id + ";\n");
                                ok = true;
                            } else {
                                System.out.println(suffix.toUpperCase());
                            }
                        } catch (Exception e) {
                            System.out.println("NO ISO SUFFIX");
                        }



                    }




//                    if (!ok) {
//                        try {
//                            lat = ((BigDecimal) objectArray[2]).floatValue() + 90;
//                            lon = ((BigDecimal) objectArray[3]).floatValue() + 180;
//                            String country = worldMap.getCountry(lat, lon);
//                            if (country != null && !country.equals("NotFound")) {
//                                out.write("UPDATE beintoo_v1.user SET address=coalesce(address,country) , country= '" + country + "' WHERE id=" + id + ";\n");
//                                ok = true;
//                            }
//                        } catch (Exception e) {
//                           
//                        }
//                    }

//                    if (!ok) {
//                        try {
//                            lat = ((BigDecimal) objectArray[4]).floatValue() + 90;
//                            lon = ((BigDecimal) objectArray[5]).floatValue() + 180;
//                            String country = worldMap.getCountry(lat, lon);
//                            if (country != null && !country.equals("NotFound")) {
//                                out.write("UPDATE beintoo_v1.user SET address=coalesce(address,country) , country= '" + country + "' WHERE id=" + id + ";\n");
//                                ok = true;
//                            }
//                        } catch (Exception e) {
//                           
//                        }
//                    }


                    if (!ok) {
                        notFound++;
                    }

                }

                out.close();
                if (to > 1) {
                    break;
                }
                if (to > 1200000) {
                    break;
                }
                System.out.println(".");
                counter++;
            }


            System.out.println("-----------------  " + counter);
            System.out.println("-----------------  " + notFound);


        } catch (Exception e) {
        }

    }
    
    private static void updateUserLevel(User u){
            for(UserLevelBean ulb: UserLevelEnum.getValues()){
                System.out.println("-------------- "+ulb.getName());
                if(u.getBescore().doubleValue() <= ulb.getMax()){
                    u.setLevel(ulb.getLevel());
                    break;
                }
                u.setLevel(5);
            }
            
        }
    
    private static void quarzJobXmlReader(){
        
        //String file = "/opt/software_prod/quartz/conf/quartz-job.xml";
        String file = "/Users/davide/comandi/quartz-job.xml";
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
                        
                        //parse using builder to get DOM representation of the XML file
			Document document = (Document) db.parse(file);

                        Element element = document.getDocumentElement();
                        
                        NodeList crons = element.getElementsByTagName("cron");
                        if(crons != null && crons.getLength() > 0) {
                                for(int i = 0 ; i < crons.getLength();i++) {
                                    
                                    System.out.println(""+ ((Element) (((Element) crons.item(i)).getElementsByTagName("name").item(0))).getFirstChild().getNodeValue());
                                    System.out.println(""+ ((Element) (((Element) crons.item(i)).getElementsByTagName("cron-expression").item(0))).getFirstChild().getNodeValue());
                                    System.out.println("");
                                    
                                }
                        }
                        

        }catch(Exception e){
        }
	
        
    }
    
}