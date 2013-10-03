package misc;

import com.beintoo.commons.bean.UserLevelBean;
import com.beintoo.commons.enums.GoogleAnalyticsTrackerEnum;
import com.beintoo.commons.enums.UserLevelEnum;
import com.beintoo.commons.util.GeoCountries;
import com.beintoo.entities.User;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import locator.WorldMap;



import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

       // System.out.println("--------"+ConfigPath.getConfigPath() + "/beintoo.properties");
        
//        IsoDialCsv();
        
//        getStringFromHtmlPage("http://it.wikipedia.org/wiki/Serie_A_2012-2013");
        
//        FileOutputStream fos = new FileOutputStream(ConfigPath.getAdminPath() + "/test.html");
//        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
//        out.write(graphResults());
//        out.close();
        
        
        //reafFile();
        
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        
//        List<RankingBean<App>> list = (List<RankingBean<App>>) RankingUtil
//				.getTop(App.class, em, RankingMethodEnum.WEBSITE_APP_RANK,
//						RankingDirectionEnum.DESC, 50);
//        System.out.println("------------------ "); 
//       for(RankingBean<App> rb : list){
//           
//           System.out.println(
//           AppHelper.getDownloadUrl(rb.getItem()));
//           
//       }
        

    }

 
    private static void vep(){
        
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

        try {

            
                
                String query = "   SELECT id, ext_id from sandbox.vgood where status = 100 ";

//                System.out.println("::: " + from + " > " + to);
                Query q = em.createNativeQuery(query);
                List results = new ArrayList<Object>();
                results = q.getResultList();

                for (int i = 0; i < results.size(); i++) {

                    Object obj = results.get(i);
                    Object[] objectArray = (Object[]) obj;

                    long id = ((Long) objectArray[0]).longValue();
                    String s = (String) objectArray[1];
                    
                    System.out.println("INSERT INTO sandbox.vgood_external_provider (vgood_id, provider) VALUES ("+id+",'"+s+"'); ");
                    
                }


        } catch (Exception e) {
        }
    }
    
    private static void googleTest() throws ParseException {
        ////////////////////////////////////////////////////////////////////////////////////////////////

        Date from;
        Date to;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        from = sdf.parse("2013-02-23");
        to = sdf.parse("2013-02-25");

        List<String[]> result ;
        com.beintoo.commons.database.GoogleAnalyticsRequest gar = new com.beintoo.commons.database.GoogleAnalyticsRequest();
        List<String> dimensions = new ArrayList<String>();
        List<String> metrics = new ArrayList<String>();

        String sort = null;
        String filter = null;



        //filter = "ga:eventLabel=~185\\+,ga:eventLabel=~183\\+;ga:eventAction==InAppClick,ga:eventAction==InAppPlayerEmailRequest";
        //dimensions.add("date");
        dimensions.add("eventAction");
        dimensions.add("eventLabel");
        metrics.add("uniqueEvents");

        result = gar.request(dimensions, metrics, GoogleAnalyticsTrackerEnum.MOBILE, sort, filter, from, to);


        if (result.size() > 0) {
            System.out.println("GARequest result.size() = " + result.size());
        }

        for (String[] ss : result) {
            for(String s : ss){
                System.out.print(s+" ");
                
            }
            System.out.println("");
        }



    }

    private static void IsoDialCsv() {

        try {
            
            Map<String,String[]> ITU = new HashMap<String, String[]>();
            Map<String,String> iso = new HashMap<String, String>();
            Map<String,String> isoNumeric = new HashMap<String, String>();
            
            Map<String, String> isoName = new HashMap<String, String>();
            
            FileOutputStream fos = new FileOutputStream("/Users/dcazzaniga/Dropbox/BEINTOO/IsoDialing.csv",true);
            
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;


            try {

                fis = new FileInputStream("/Users/dcazzaniga/Documents/ITU.2.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                String line = "";
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split(";");
                    ITU.put(s[0], s);
                }
                
                fis = new FileInputStream("/Users/dcazzaniga/NetBeansProjects/git/xone/vodafone.country.iso.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split("\t");
                    iso.put(s[4].replaceAll("\"", ""), line);
                    isoName.put(s[4].replaceAll("\"", ""), s[0].replaceAll("\"", ""));
                }
                
                fis = new FileInputStream("/Users/dcazzaniga/NetBeansProjects/git/xone/countries.iso.numeric.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split(";");
                    isoNumeric.put(s[0], s[2]);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                
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
            
            
            for(String s: iso.keySet()){
                
                if(ITU.containsKey(s)){
                    
                    StringBuilder out = new StringBuilder();
                    
                    
                    out.append("\""+s+"\"");
                    out.append("\t");
                    out.append(iso.get(s));
                    out.append("\t");
                    
                    if(ITU.get(s)[4].contains("(")){
                        
                        String sub = ITU.get(s)[4];
                        sub = sub.substring(sub.indexOf("(")+1);
                        sub = sub.substring(0, sub.indexOf(")"));
                        out.append("\"").append(sub).append("\"");
                        
                    }else{
                        out.append("\" \"");
                    }
                    out.append("\t");
                    
                    if(isoNumeric.containsKey(isoName.get(s))){
                        System.out.println("--");
                        out.append("\"").append(isoNumeric.get(isoName.get(s))).append("\"");
                    }else{
                        out.append("\" \"");
                    }
                    
                    
                    out.append("\n");
                    osw.write(out.toString());
                    
                }else{
                    System.out.println(s);
                }
                
            }
            osw.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private static void updateUserLevel(User u) {
        for (UserLevelBean ulb : UserLevelEnum.getValues()) {
            System.out.println("-------------- " + ulb.getName());
            if (u.getBescore().doubleValue() <= ulb.getMax()) {
                u.setLevel(ulb.getLevel());
                break;
            }
            u.setLevel(5);
        }

    }

    private static BufferedReader getFileReader(String filenname) {
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;

            fis = new FileInputStream(filenname);
            isr = new InputStreamReader(fis);
            buf = new BufferedReader(isr);
            return buf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void carrieFromIp() {
        try {
            BufferedReader buf = getFileReader("/Users/davide/NetBeansProjects/LocalTester/mx_.txt");
            FileOutputStream fos = new FileOutputStream("/Users/davide/NetBeansProjects/LocalTester/mx_ip_carrie.txt");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            String line = "";
            HashMap<String, Integer> carries = new HashMap<String, Integer>();
            while ((line = buf.readLine()) != null) {

                String c = GeoCountries.ipAddr2ISP(line);
                if (carries.containsKey(c)) {
                    carries.put(c, carries.get(c) + 1);
                } else {
                    carries.put(c, 1);
                }
            }
            for (String c : carries.keySet()) {
                osw.write(String.format("%1$50s", c) + ";" + String.format("%1$15s", carries.get(c)) + "\n");
            }
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void netspeedFromIp() throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream("/Users/davide/NetBeansProjects/LocalTester/netspeed_mx_ip.txt");
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        try {
            BufferedReader buf = getFileReader("/Users/davide/NetBeansProjects/LocalTester/mx_ip.txt");

            String line = "";
            HashMap<String, Integer> nets = new HashMap<String, Integer>();


            String license_key = "fwA7kRKYGZ40";
            String url_str = "http://geoip.maxmind.com/e?l=" + license_key + "&i=";
            int i = 0;
            while ((line = buf.readLine()) != null) {

                try {
                    i++;

                    if (i > 10000) {
                        String c = getNetspeedFromIP(url_str, line);;
                        if (nets.containsKey(c)) {
                            nets.put(c, nets.get(c) + 1);
                        } else {
                            nets.put(c, 1);
                        }
                    }
                    if (i == 10001) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
            for (String c : nets.keySet()) {
                osw.write(String.format("%1$50s", c) + ";" + String.format("%1$15s", nets.get(c)) + "\n");
            }
            osw.close();
        } catch (Exception e) {
            osw.close();
            e.printStackTrace();
        }
    }

    public static String getNetspeedFromIP(String url_str, String ip_address) throws MalformedURLException, IOException {
        URL url = new URL(url_str + ip_address);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inLine;
        String netspeed = "";
        while ((inLine = in.readLine()) != null) {
            // Alternatively use a CSV parser here.
            Pattern p = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");
            Matcher m = p.matcher(inLine);

            ArrayList fields = new ArrayList();
            String f;
            while (m.find()) {
                f = m.group(1);
                if (f != null) {
                    fields.add(f);
                } else {
                    fields.add(m.group(2));
                }
            }

//            String countrycode = fields.get(0);
//            String countryname = fields.get(1);
//            String regioncode = fields.get(2);
//            String regionname = fields.get(3);
//            String city = fields.get(4);
//            String lat = fields.get(5);
//            String lon = fields.get(6);
//            String metrocode = fields.get(7);
//            String areacode = fields.get(8);
//            String timezone = fields.get(9);
//            String continent = fields.get(10);
//            String postalcode = fields.get(11);
//            String isp = fields.get(12);
//            String org = fields.get(13);
//            String domain = fields.get(14);
//            String asnum = fields.get(15);
            netspeed = (String) fields.get(16);
//            String usertype = fields.get(17);
//            String accuracyradius = fields.get(18);
//            String countryconf = fields.get(19);
//            String cityconf = fields.get(20);
//            String regionconf = fields.get(21);
//            String postalconf = fields.get(22);
//            String error = fields.get(23);

        }
        return netspeed;

    }

    private static void quarzJobXmlReader() {

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
            if (crons != null && crons.getLength() > 0) {
                for (int i = 0; i < crons.getLength(); i++) {

                    System.out.print(String.format("%1$40s", ((Element) (((Element) crons.item(i)).getElementsByTagName("name").item(0))).getFirstChild().getNodeValue()));
                    System.out.print(": " + ((Element) (((Element) crons.item(i)).getElementsByTagName("cron-expression").item(0))).getFirstChild().getNodeValue());
                    System.out.println("");

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static StringBuilder getStringFromHtmlPage(String page) throws MalformedURLException, URISyntaxException, IOException {

        
        
        StringBuilder out = new StringBuilder();
        URL url;

        try {
            // get URL content
            url = new URL(page);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;

            //save to this filename
                
            //use FileWriter to write file
            boolean print = false;
            while ((inputLine = br.readLine()) != null) {
                if (inputLine.contains("id=\"Classifica_in_divenire")) {
                    print = true;
                }
                if (inputLine.contains("</div>")) {
                    print = false;
                }
                if (print) {
                    if(!inputLine.contains("table") && !inputLine.contains("h3") && !inputLine.contains("div") && !inputLine.contains("tr") ){
                        out.append(inputLine);
                    }
                }
            }


            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;

    }
    
    public static String graphResults() {

        StringBuilder graph = new StringBuilder();
        try {

            graph.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            graph.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
            graph.append("<head>\n");
            graph.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n");
            graph.append("<script type=\"text/javascript\" src=\"http://www.google.com/jsapi\"></script>\n");
            graph.append("<script type=\"text/javascript\">\n");
            graph.append("google.load('visualization', '1', {packages: ['motionchart']});");
            graph.append("</script>\n");
            graph.append("<script type=\"text/javascript\">\n");
            graph.append("function drawVisualization() {\n");
            graph.append("var data = new google.visualization.DataTable();\n");
            graph.append(" data.addColumn('string', 'team');\n");
            graph.append(" data.addColumn('number', 'day');\n");
            graph.append(" data.addColumn('number', 'points');\n");
            
            String html = getStringFromHtmlPage("http://it.wikipedia.org/wiki/Serie_A_2012-2013").toString();
            
            String[] trs = html.split("<th>");
            
            
            for (int i =80 ; i<trs.length ; i++) {
                String[] th = trs[i].split("<td>");
                
                String team = th[0].replace("</th>", "");
                
                for(int j=1; j<28; j++){
                    System.out.println(team+","+j+","+th[j].replace("</td>", ""));
                    graph.append("data.addRow(['").append(team).append("',").append(j).append(",").append(th[j].replace("</td>", "")).append("]);\n");
                }
            }

            graph.append(" \n var motionchart = new google.visualization.MotionChart(document.getElementById('visualization'));\n");
            graph.append(" motionchart.draw(data, {'width': 800, 'height': 400}); \n }\n ");
                        
            graph.append("google.setOnLoadCallback(drawVisualization);\n");
            graph.append("</script>\n");
            graph.append("</head>\n");
            graph.append("<body >\n");
            graph.append("   <div id=\"visualization\" style=\"width: 800px; height: 400px;\"></div>\n");
            graph.append("</body>\n");
            graph.append("</html>\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph.toString();
    }
}
