/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.sales;

import com.beintoo.api.output.Wrapper;
import com.beintoo.commons.util.ConfigPath;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.xml.internal.rngom.digested.DXMLPrinter;
import java.io.*;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author davide
 */
public class TotalPlayerUserParser implements Serializable {

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        try {
            formattingSalerFile();
        } catch (Exception e) {
        }

    }
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String isoCountryNameFile = "ISO3166_CountryCodes.csv";
    private String from;
    private String to;
    private int totPlayers;
    private int totMaus=0;
    private int totUsers;
    private int activePlayers = 92603358;
    private int activeUsers;
    private int dailyActivePlayers;
    private int dailyActiveUsers;
    private Map<String, Integer> osDailyActive = new HashMap<String, Integer>();
    private List<OsPlayerBean> listaOs = new ArrayList<OsPlayerBean>(1000);
    private List<CountryBeanUserPlayer> lista;

    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        String DATE_EN_GB = "dd-MMM-yyyy HH:mm:ss";
        builder.setDateFormat(DATE_EN_GB);
        return builder.create().toJson(this);
    }

    public TotalPlayerUserParser( Date from, Date to) {
        this.from = sdf.format(from);
        this.to = sdf.format(to);
        lista = new ArrayList<CountryBeanUserPlayer>(310);
    }

    public static void formattingSalerFile() {
        
        TotalPlayerUserParser tpup = null;
        try {
            Date from = sdf.parse("2013-01-31");
            Date to = sdf.parse("2013-02-28");
            try{
                FileInputStream fis = new FileInputStream(ConfigPath.getConfigPath() +
                        "/TotalPlayerUserParser"+sdf.format(new Date()));
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader buf = new BufferedReader(isr);

                Type dataMapType = new TypeToken<TotalPlayerUserParser>() {
                }.getType();
                String line ;
                while ((line = buf.readLine()) != null) {
                    tpup = new GsonBuilder().setDateFormat(Wrapper.DATE_EN_GB).create().fromJson(line, dataMapType);
                }
            }catch(Exception e){
                
            }
            ///////
            if (tpup == null) {
                tpup = new TotalPlayerUserParser(from, to);
                
                tpup.setAppPlayers();
                
                tpup.setCountryUser();
                tpup.setCountryUserAge();
                tpup.setCountryUserGender();
                
                tpup.setCountryUserOs();
                tpup.setCountryImpressions();
                tpup.setCountryBannerImpressions();
                tpup.setCountryActiveUsers();
                tpup.setCountryPlayers();
                tpup.setCountryPlayersMonth();
                
                tpup.setTotalPlayerUser();
                tpup.setDailyActivePlayers();
                //tpup.activePlayers = 87059455 ; // dicembre (79018229); // novembre (63578728)
                tpup.setActiveUsers();
//                tpup.setPercentage();
                tpup.setDerivate();
                
            }
            //////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////
            try{
                FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() +
                        "/TotalPlayerUserParser"+sdf.format(new Date()));
                OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
                out.write(tpup.toJson());
                out.close();
            }catch(Exception e){
            }
            
            tpup.toCSV("SalerDataFormatted_"+to+".csv");
            tpup.toGeneralCSV("AppOs_"+to+".csv");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setTotalPlayerUser() {
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query ;
            query = " SELECT SUM(new_players) , SUM(users) "
                    + " FROM report_app p "
                    + " WHERE day <= '"+to+"' ";
            
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            
            Object[] result = (Object[]) q.getSingleResult();
            totPlayers = ((Number) result[0]).intValue();
            totUsers = ((Number) result[1]).intValue();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setDailyActivePlayers() {
        try {
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "SELECT app_type, MAX(active) FROM ( "
                    + " SELECT day, a.app_type ,SUM(active_players) active "
                    + " FROM report_app p , contest c ,app a "
                    + " WHERE p.contest_id = c.id AND c.app_id = a.id "
                    + " AND day >= '"+from+"' AND day <= '"+from+"' "
                    + " GROUP BY day, app_type ) t "
                    + " GROUP BY app_type ";
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();
            for (int i = 0; i < results.size(); i++) {
                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String os = (String) objectArray[0];
                    int active = ((Number) objectArray[1]).intValue();
                    osDailyActive.put(os, active);
                    dailyActivePlayers += active;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setActiveUsers() {
        try {
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "SELECT COUNT(DISTINCT player_id) "
                    + " FROM player_score p "
                    + " WHERE p.lastupdate  >= '"+from+"' AND p.lastupdate <= '"+to+"' ";
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            activeUsers = ((Number) q.getSingleResult()).intValue();

            query = "SELECT COUNT(DISTINCT player_id) "
                    + " FROM player_score p "
                    + " WHERE lastupdate > now() - interval 1 day ";
            System.out.println("::: " + query);
            q = em.createNativeQuery(query);
            dailyActiveUsers = ((Number) q.getSingleResult()).intValue();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // PER COUNTRY
    private void setCountryActiveUsers() {
        try {
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query = "SELECT u.country , COUNT(*) "
                    + " FROM user_player up, player_score ps , user u "
                    + " WHERE ps.lastupdate > '"+to+"' - interval 10 day "
                    + " AND up.user_id = u.id AND ps.player_id = up.player_id "
                    + " GROUP BY u.country ; ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();
            if(totMaus>0)
                totMaus=0;
            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int c1 = ((Number) objectArray[1]).intValue();
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if(country!=null)
                        totMaus+=c1;
                    if (idx > -1) {
                        lista.get(idx).setDaus(c1);
                    } else {
                        cb.setDaus(c1);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setCountryUser() {

        try {
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query = " SELECT "
                    + " country, count(*) tot FROM user  "
                    +" GROUP BY country ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int c1 = ((Number) objectArray[1]).intValue();

                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setUnits(c1);
                    } else {
                        cb.setUnits(c1);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryUserAge() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query = " SELECT "
                    + " country, count(*) tot,  "
                    + " SUM(IF(age>=10&&age<=65,1,0)) '10-65', "
                    + " SUM(IF(age>=20&&age<=40,1,0)) '20-40',  "
                    + " SUM(IF(age>=15&&age<20,1,0)) '15-19', "
                    + " SUM(IF(age>=20&&age<25,1,0)) '20-24', "
                    + " SUM(IF(age>=25&&age<30,1,0)) '25-29', "
                    + " SUM(IF(age>=30&&age<35,1,0)) '30-34', "
                    + " SUM(IF(age>=35&&age<40,1,0)) '35-39' "
                    + " FROM ( "
                    + "     SELECT country, 2012 - YEAR(birthdate) age FROM user  "
                    + "     WHERE birthdate IS NOT null  "
                    + " ) t "
                    + " GROUP BY COUNTRY ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int c1 = ((Number) objectArray[1]).intValue();
                    int c2 = ((Number) objectArray[2]).intValue();
                    int c3 = ((Number) objectArray[3]).intValue();
                    int c4 = ((Number) objectArray[4]).intValue();
                    int c5 = ((Number) objectArray[5]).intValue();
                    int c6 = ((Number) objectArray[6]).intValue();
                    int c7 = ((Number) objectArray[7]).intValue();
                    int c8 = ((Number) objectArray[8]).intValue();

                    Map<String, Integer> age = new HashMap<String, Integer>();
                    age.put("tot", c1);
                    age.put("10-65", c2);
                    age.put("20-40", c3);
                    age.put("15-19", c4);
                    age.put("20-24", c5);
                    age.put("25-29", c6);
                    age.put("30-34", c7);
                    age.put("35-39", c8);

                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setAge(age);
                    } else {
                        cb.setAge(age);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryUserGender() {
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = " SELECT country, COUNT(*) gg, "
                    + " SUM(IF(gender=1,1,0)) 'male', "
                    + " SUM(IF(gender=2,1,0)) 'female' "
                    + " FROM user "
                    + " WHERE gender IS NOT  null "
                    + " GROUP BY country ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int tot = ((Number) objectArray[1]).intValue();
                    int male = ((Number) objectArray[2]).intValue();
                    int female = ((Number) objectArray[3]).intValue();

                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setMale(male);
                        lista.get(idx).setFemale(female);
                    } else {
                        cb.setMale(male);
                        cb.setFemale(female);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryUserOs() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "   SELECT ra.country, a.app_type, SUM(IFNULL(new_players,0)) "
                    + " FROM report_app_bycountry ra, app a "
                    + " WHERE ra.app_id = a.id "
                    + " AND ra.day >= '"+from+"' AND ra.day <= '"+to+"' "
                    + " GROUP BY ra.country, a.app_type ; ";

            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    String os = (String) objectArray[1];
                    int c2 = ((Number) objectArray[2]).intValue();
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).getOs().put(os, c2);
                    } else {
                        cb.getOs().put(os, c2);
                        lista.add(cb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryImpressions() {
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = " SELECT C , SUM(imps) , AVG(imps), MAX(imps) FROM  ( "
                    + " SELECT "
                    + " rv.day, "
                    + "     (SELECT group_concat(distinct p.country) FROM place p, vgood_poi vp  "
                    + "             WHERE  rv.vgood_id = vp.vgood_id AND vp.place_id = p.id "
                    + "             AND vp.status = 1 AND p.status = 1 ) C, "
                    + "     IFNULL( SUM(assigned_vgoods)+SUM(player_assigned_vgoods),0) imps "
                    //+ "     ,IFNULL( SUM(converted_vgoods)+SUM(player_converted_vgoods),0)  clicks "
                    + " FROM report_vgood rv  "
                    + " WHERE  rv.day >= '"+from+"' AND rv.day <= '"+to+"' "
                    + " GROUP BY rv.day, C ) T "
                    + " GROUP BY C ";
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int impressions = ((Number) objectArray[1]).intValue();
                    int avgImps = ((Number) objectArray[2]).intValue(); 
                    int maxImps = ((Number) objectArray[3]).intValue(); 
                    //int clicks = ((Number) objectArray[4]).intValue();
                    
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setImpressions(impressions);
                      //  lista.get(idx).setClicks(clicks);
                        lista.get(idx).setAvgImpressions(avgImps);
                        lista.get(idx).setMaxImpressions(maxImps);
                    } else {
                        cb.setImpressions(impressions);
                      //  cb.setClicks(clicks);
                        cb.setAvgImpressions(avgImps);
                        cb.setMaxImpressions(maxImps);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryBannerImpressions() {
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "  SELECT country, "
                    + " SUM(IFNULL(imps,0)+IFNULL(ntds,0)) , "
                    + " AVG(IFNULL(imps,0)+IFNULL(ntds,0)) , "
                    + " MAX(IFNULL(imps,0)+IFNULL(ntds,0)) "
                    + " FROM report_banner_bycountry "
                    + " WHERE provider = 'BEINTOO' "
                    + " AND day >= '"+from+"' AND day <= '"+to+"'"
                    + " GROUP BY country ";
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int impressions = ((Number) objectArray[1]).intValue();
                    int avgImpressions = ((Number) objectArray[2]).intValue();
                    int maxImpressions = ((Number) objectArray[3]).intValue();
                    
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setBannerDisplay(impressions);
                        lista.get(idx).setAvgBannerDisplay(avgImpressions);
                        lista.get(idx).setMaxBannerDisplay(maxImpressions);
                    } else {
                        cb.setBannerDisplay(impressions);
                        cb.setAvgBannerDisplay(avgImpressions);
                        cb.setMaxBannerDisplay(maxImpressions);
                        lista.add(cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setCountryPlayers(){
        
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "  SELECT country, app_id, IFNULL(SUM(new_players),0) pp" +
                    "  FROM report_app_bycountry  " +
                    "  WHERE  day >= '2013-01-10' AND day <= '"+to+"' " +
                    "  GROUP BY country, app_id  ";

            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int app_id = ((Number) objectArray[1]).intValue();
                    int pp = ((Number) objectArray[2]).intValue();
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).getApps().put(app_id, pp);
                    } else {
                        cb.getApps().put(app_id, pp);
                        lista.add(cb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    private void setCountryPlayersMonth(){
        
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "  SELECT country, IFNULL(SUM(new_players),0) pp" +
                    "  FROM report_app_bycountry  " +
                    "  WHERE  day >= '"+from+"' AND day <= '"+to+"' " +
                    "  GROUP BY country ";

            Query q = em.createNativeQuery(query);
            List results = q.getResultList();

            for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    String country = (String) objectArray[0];
                    int pp = ((Number) objectArray[1]).intValue();
                    CountryBeanUserPlayer cb = new CountryBeanUserPlayer(country);
                    int idx = lista.indexOf(cb);
                    if (idx > -1) {
                        lista.get(idx).setLastMonthPlayers(pp);
                    } else {
                        cb.setLastMonthPlayers(pp);
                        lista.add(cb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    // 
    private void setAppPlayers() {
        try {
            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            String query ;
            query = "SELECT id, name, app_type , IFNULL(MAX(active),0) , IFNULL(MAX(sub),0) FROM ( "
                    + " SELECT day, a.id, a.name , a.app_type , SUM(sessions) active, SUM(submits) sub "
                    + " FROM report_app p , contest c ,app a "
                    + " WHERE p.contest_id = c.id AND c.app_id = a.id "
                    + " AND day >= '"+to+"' - interval 15 day "
                    + " GROUP BY day, a.id ) t "
                    + " GROUP BY id ";
            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);
            List results = q.getResultList();
            for (int i = 0; i < results.size(); i++) {
                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    int app_id = ((Number) objectArray[0]).intValue();
                    String name = (String) objectArray[1];
                    String type = (String) objectArray[2];
                    int daps = ((Number) objectArray[3]).intValue();
                    int submits = ((Number) objectArray[4]).intValue();

                    OsPlayerBean opb = new OsPlayerBean(app_id);
                    int idx = listaOs.indexOf(opb);
                    if (idx > -1) {
                        listaOs.get(idx).setDaps(daps);
                        listaOs.get(idx).setDailySubmits(submits);
                    } else {
                        opb.setName(name);
                        opb.setDaps(daps);
                        opb.setApp_type(type);
                        opb.setDailySubmits(submits);
                        listaOs.add(opb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //////////////////

            query = " SELECT up.app_id , COUNT(*) "
                    + " FROM user_player up, player_score ps , user u "
                    + " WHERE ps.lastupdate > now() - interval 1 day "
                    + " AND up.user_id = u.id AND ps.player_id = up.player_id "
                    + " GROUP BY up.app_id ; ";

            System.out.println("::: " + query);
            q = em.createNativeQuery(query);
            results = q.getResultList();
            for (int i = 0; i < results.size(); i++) {
                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    int app_id = ((Number) objectArray[0]).intValue();
                    int daus = ((Number) objectArray[1]).intValue();

                    OsPlayerBean opb = new OsPlayerBean(app_id);
                    int idx = listaOs.indexOf(opb);
                    if (idx > -1) {
                        listaOs.get(idx).setDaus(daus);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //////////////////

            query = "SELECT a.id, a.name, a.app_type,  IFNULL(SUM(new_players),0) pp, IFNULL(SUM(users),0) uu"
                    + " FROM report_app p , contest c ,app a "
                    + " WHERE p.contest_id = c.id AND c.app_id = a.id "
                    + " GROUP BY  a.id ";
            System.out.println("::: " + query);
            q = em.createNativeQuery(query);
            results = q.getResultList();
            for (int i = 0; i < results.size(); i++) {
                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    int app_id = ((Number) objectArray[0]).intValue();
                    String name = (String) objectArray[1];
                    String type = (String) objectArray[2];
                    int pp = ((Number) objectArray[3]).intValue();
                    int uu = ((Number) objectArray[4]).intValue();

                    OsPlayerBean opb = new OsPlayerBean(app_id);
                    int idx = listaOs.indexOf(opb);
                    if (idx > -1) {
                        listaOs.get(idx).setPlayers(pp);
                        listaOs.get(idx).setUsers(uu);
                    } else {
                        opb.setName(name);
                        opb.setApp_type(type);
                        opb.setPlayers(pp);
                        opb.setUsers(uu);
                        listaOs.add(opb);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            ////////////////// from 10 genuary

            query = "SELECT c.app_id, IFNULL(SUM(new_players),0) pp"
                    + " FROM report_app p , contest c "
                    + " WHERE p.contest_id = c.id  "
                    + " AND day >= '2013-01-10' AND day <= '"+to+"'"
                    + " GROUP BY c.app_id ";
            System.out.println("::: " + query);
            q = em.createNativeQuery(query);
            results = q.getResultList();
            for (int i = 0; i < results.size(); i++) {
                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                try {
                    int app_id = ((Number) objectArray[0]).intValue();
                    int pp = ((Number) objectArray[1]).intValue();
                    
                    OsPlayerBean opb = new OsPlayerBean(app_id);
                    int idx = listaOs.indexOf(opb);
                    if (idx > -1) {
                        listaOs.get(idx).setPlayersForSplit(pp);
                    } 

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void toCSV(String salerDataFormattedcsv) {
        try {

            FileInputStream fis = new FileInputStream(ConfigPath.getConfigPath() + "/ContinentRegionCountryISO.csv");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buf = new BufferedReader(isr);

            Map<String, String> isoMap = new HashMap<String, String>(252);
            Map<String, String> region = new HashMap<String, String>(300);
            Map<String, String> continent = new HashMap<String, String>(300);

            String line = "";

            while ((line = buf.readLine()) != null) {
                String[] s = line.split(";");
                region.put(s[3], s[1]);
                continent.put(s[3], s[0]);
            }

            fis = new FileInputStream(ConfigPath.getConfigPath() + "/" + isoCountryNameFile);
            isr = new InputStreamReader(fis);
            buf = new BufferedReader(isr);
            while ((line = buf.readLine()) != null) {
                String[] s = line.split(";");
                isoMap.put(s[0], s[1]);
            }


            /////////// OUTPIUT
            FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() + "/" + salerDataFormattedcsv);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write(
                    "USERS,"+this.totUsers+"\n"
                    + "MA users,"+this.activeUsers+"\n"
                    + "DA users,"+this.dailyActiveUsers+"\n"
                    + "PLAYERS,"+ this.totPlayers + "\n" 
                    + "MA players,"+this.activePlayers + "\n" 
                    + "DA playes,"+this.dailyActivePlayers + "\n"
                    + ",,,,,,,,delivery,,,"
                    + "split OS,,,,,"
                    + "demographic\n");
            out.write("continent,region,iso,name,"
                    + "users,"
                    + "players,month players,maus,daus_10days,"
                    + "tot imps,avg imps,max imps,tot getAd,avg getAd, max getAd,"
                    + "ANDROID,CROSS_OS,IOS,OTHER,WEB,"
                    + "male,female,10-65,20-40,15-19,20-24,25-29,30-34,35-39\n");

            Collections.sort(this.lista);

            int limit = 0;
            for (CountryBeanUserPlayer cb : this.lista) {
                String row = "";
                try {
                    row += continent.get(cb.getIso()) + ",";
                    row += region.get(cb.getIso()) + ",";
                } catch (Exception e) {
                    row += ",,";
                }

                try {
                    row += cb.getIso() + ",";
                    row += isoMap.get(cb.getIso()).replace(",", "-") + ",";
                } catch (Exception e) {
                    row += ",,";
                }

                try {
                    row += cb.getUnits() + ",";
                } catch (Exception e) {
                    row += ",";
                }

                try {
                    row += cb.getPlayers() + "," + cb.getLastMonthPlayers() + "," + cb.getMaus() + ","+  cb.getDaus() + ",";
                } catch (Exception e) {
                    row += ",,,";
                }

                // impressions & clicks
                try {
                    row += cb.getImpressions() + "," + cb.getAvgImpressions() + ","+cb.getMaxImpressions() + ",";
                } catch (Exception e) {
                    row += ",,,";
                }
//                try {
//                    row += cb.getClicks() + ",";
//                } catch (Exception e) {
//                    row += ",";
//                }
                try {
                    row += cb.getBannerDisplay() + "," + cb.getAvgBannerDisplay() + "," + cb.getMaxBannerDisplay() + ",";
                } catch (Exception e) {
                    row += ",,,";
                }
                // gender
                String[] os = {"ANDROID", "CROSS_OS", "IOS", "OTHER", "WEB"};
                for (String s : os) {
                    if (!cb.getOs().containsKey(s) || cb.getOs().get(s) == null) {
                        cb.getOs().put(s, 0);
                    }
                }
                int totOS = cb.getOs().get("ANDROID")
                        + cb.getOs().get("CROSS_OS")
                        + cb.getOs().get("IOS")
                        + cb.getOs().get("OTHER")
                        + cb.getOs().get("WEB");
                try {

                    row += ((float) cb.getOs().get("ANDROID")) / totOS + ",";
                    row += ((float) cb.getOs().get("CROSS_OS")) / totOS + ",";
                    row += ((float) cb.getOs().get("IOS")) / totOS + ",";
                    row += ((float) cb.getOs().get("OTHER")) / totOS + ",";
                    row += ((float) cb.getOs().get("WEB")) / totOS + ",";

                } catch (Exception e) {
                    row += ",";
                }

                // gender
                int totGender = cb.getMale() + cb.getFemale();
                try {
                    row += ((float) cb.getMale()) / totGender + ",";
                    row += ((float) cb.getFemale()) / totGender + ",";
                } catch (Exception e) {
                    row += ",";
                }

                // age
                int totAged = cb.getAge().get("10-65");
                try {
                    row += ((float) cb.getAge().get("10-65")) / totAged + ",";
                    row += ((float) cb.getAge().get("20-40")) / totAged + ",";
                    row += ((float) cb.getAge().get("15-19")) / totAged + ",";
                    row += ((float) cb.getAge().get("20-24")) / totAged + ",";
                    row += ((float) cb.getAge().get("25-29")) / totAged + ",";
                    row += ((float) cb.getAge().get("30-34")) / totAged + ",";
                    row += ((float) cb.getAge().get("35-39")) / totAged + ",";

                } catch (Exception e) {
                    row += ",";
                }
                row += "\n";
                out.write(row);

                limit++;
                if (limit == 100) {
                    break;
                }

            }

            out.close();

        } catch (Exception e) {
        }

    }

    private void toGeneralCSV(String salerDataFormattedcsv) {
        try {
            /////////// OUTPIUT
            FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() + "/" + salerDataFormattedcsv);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write("os,activePlayers\n");
            for (String os : osDailyActive.keySet()) {
                out.write(os + "," + osDailyActive.get(os) + "\n");
            }

            out.write("\n id,name,os,players,daps,daily submits,users,daus\n");

            Collections.sort(this.listaOs);

            int limit = 0;
            for (OsPlayerBean cb : this.listaOs) {
                String row = "";

                try {

                    row += cb.getId() + ",";
                    row += cb.getName() + ",";
                    row += cb.getApp_type() + ",";
                    row += cb.getPlayers() + ",";
                    row += cb.getDaps() + ",";
                    row += cb.getDailySubmits() + ",";
                    row += cb.getUsers() + ",";
                    row += cb.getDaus() + ",";

                } catch (Exception e) {
                }

                row += "\n";
                out.write(row);
                limit++;
                if (limit == 100) {
                    break;
                }

            }

            out.close();

        } catch (Exception e) {
        }

    }

    private void setPercentage() {
        CountryBeanUserPlayer cbNull = new CountryBeanUserPlayer(null);
        int idx = lista.indexOf(cbNull);
        int noCountry = 0;
        if (idx > -1) {
            noCountry = lista.get(idx).getUnits();
            lista.remove(idx);
        }
        for (CountryBeanUserPlayer cb : lista) {
            cb.setPercentage(cb.getUnits() / (float) totUsers);
            int plus = (int) (cb.getPercentage() * noCountry);
            cb.setUsers(cb.getUnits() + plus);
        }

    }
    
    /////////////////

    private void setDerivate() {
        // settings players and maus
        System.out.println(":::::::: "+totMaus);
        System.out.println(":::::::: "+activePlayers);
        System.out.println(":::::::: "+totPlayers);
        System.out.println(":::::::: "+totUsers);
        
        for (CountryBeanUserPlayer cb : lista) {
            try{
                cb.setMaus( (int)(  cb.getDaus() /((float)  totMaus) * activePlayers));
                        
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                int player =0 ;
                for(Integer app_id : cb.getApps().keySet()){
                
                    OsPlayerBean o = new OsPlayerBean(app_id);
                    int idx = listaOs.indexOf(o);
                    if(idx>-1){
                        player += ( ( cb.getApps().get(app_id)) / (float) listaOs.get(idx).getPlayersForSplit() ) * listaOs.get(idx).getPlayers() ;
                    }
                    
                }
                System.out.println("- "+cb.getIso()+" : "+player);
                cb.setPlayers(player);
                
            }catch(Exception e){
                cb.setPlayers(0);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////
    ////////////// GETTER AND SETTER ////////////////////////////////
    public int getActivePlayers() {
        return activePlayers;
    }

    public void setActivePlayers(int activePlayers) {
        this.activePlayers = activePlayers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getDailyActivePlayers() {
        return dailyActivePlayers;
    }

    public void setDailyActivePlayers(int dailyActivePlayers) {
        this.dailyActivePlayers = dailyActivePlayers;
    }

    public int getDailyActiveUsers() {
        return dailyActiveUsers;
    }

    public void setDailyActiveUsers(int dailyActiveUsers) {
        this.dailyActiveUsers = dailyActiveUsers;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<CountryBeanUserPlayer> getLista() {
        return lista;
    }

    public void setLista(List<CountryBeanUserPlayer> lista) {
        this.lista = lista;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getTotPlayers() {
        return totPlayers;
    }

    public void setTotPlayers(int totPlayers) {
        this.totPlayers = totPlayers;
    }

    public int getTotUsers() {
        return totUsers;
    }

    public void setTotUsers(int totUsers) {
        this.totUsers = totUsers;
    }

}
