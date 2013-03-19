/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbUpdater;

import com.beintoo.commons.enums.BannerProviderEnum;
import com.beintoo.commons.enums.LogContextEnum;
import com.beintoo.commons.enums.LogLevelEnum;
import com.beintoo.commons.enums.VgoodLogProcessorEnum;
import com.beintoo.commons.helper.AppHelper;
import com.beintoo.commons.helper.ContestHelper;

import com.beintoo.commons.util.Logger;
import com.beintoo.entities.App;
import com.beintoo.entities.Contest;
import com.sun.org.apache.bcel.internal.generic.IFEQ;
import com.sun.tools.javac.code.Lint;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author davide
 */
public class NewTablesBycountry {
    // 
// report_banner_bycountry

    private static String BANNER_FILENAME = "/Users/davide/comandi/banner_parser.log";
    private static String METHOD_FILENAME = "/Users/davide/comandi/report_method.log";
// report_app_bycountry and report_app    
    private static String ACTIVE_FILENAME = "/Users/davide/comandi/ps_parser_active_output.log";
    private static String NEW_FILENAME = "/Users/davide/comandi/ps_parser_new_output.log";
    private static String UNIQUE_FILENAME = "/Users/davide/comandi/ps_parser_output.log";
    private static String data = "2013-03-18";

    public static void main(String[] args) throws FileNotFoundException {

        System.out.println("::::: " + data);

        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();
        try{
//            System.out.println("WRITING NEW PLAYERS BY COUNTRY");
//            writeNewPlayersByCountry(em, data);
//            System.out.println("WRITING ACTIVE PLAYERS");
//            writeActivePlayers(em, data);
//            System.out.println("WRITING UNIQUE PLAYERS");
//            writeUniquePlayers(em, data);
//            System.out.println("WRITING NEW PLAYERS");
//            writeNewPLayers(em, data);
            
            writeReportMethodByCountry(em, data);
            
//            writeBannerDisplayByCountry(em, data);
            em.close();
            
            
        }catch(Exception e){
            em.close();
            e.printStackTrace();
        }
    }

    public static boolean writeNewPlayersByCountry(EntityManager em, String data) throws FileNotFoundException {

        BufferedReader reader = new BufferedReader(new FileReader(NEW_FILENAME + "." + data));
        String line = null;
        // NUMBER APP_ID COUNTRY
        EntityTransaction entityTransaction = em.getTransaction();
        try {

            int i = 0;

            entityTransaction.begin();
            while ((line = reader.readLine()) != null) {
                i++;
                if (i % 100 == 0) {
                    System.out.println("" + i);
                    entityTransaction.commit();
                    entityTransaction.begin();
                }

                String[] ar = line.trim().split(" ");
                try {
                    Query query = em.createNativeQuery(""
                            + "INSERT IGNORE INTO report_app_bycountry(day,app_id,country,new_players) "
                            + "VALUES ('" + data + "',?,?,?) "
                            + "; ");
                    query.setParameter(1, ar[1]);
                    try {
                        query.setParameter(2, ar[2]);
                    } catch (Exception e) {
                        query.setParameter(2, null);
                    }
                    query.setParameter(3, ar[0]);
                    query.executeUpdate();

                } catch (Exception e) {
                    System.out.println("ERROR LINE: " + line);
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
            entityTransaction.commit();
            
        } catch (IOException e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            System.out.println("ERROR LINE: " + line);
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }

        return true;
    }

    public static boolean writeNewPLayers(EntityManager em, String data) throws FileNotFoundException {

        HashMap<Integer, Integer> newPlayers = loadNewPlayers(data);
        EntityTransaction entityTransaction = em.getTransaction();

        try {
            int i = 0;
            entityTransaction.begin();


            for (Integer app_id : newPlayers.keySet()) {

                i++;
                if (i % 20 == 0) {
                    System.out.println("" + i);
                    entityTransaction.commit();
                    entityTransaction.begin();
                }

                App app = em.find(App.class, app_id);
                if (app == null) {
                    System.out.println("AppReporter, new players, null app for id: " + app_id);
                    continue;
                }

                Contest contest;
                try {
                    contest = (Contest) em.createQuery("SELECT c FROM Contest c WHERE c.codeID = :codeID and c.app = :app").setParameter("codeID", "default").setParameter("app", app).getSingleResult();
                } catch (Exception e) {
                    System.out.println("NO DEFAULT CONTEST FOR" + app_id);
                    continue;
                }
                int contest_id = contest.getId();

                Query query = em.createNativeQuery(""
                        + "INSERT INTO report_app(day,contest_id,new_players) "
                        + "VALUES ('" + data + "',?,?) "
                        + "ON DUPLICATE KEY UPDATE new_players = ? ; ");
                query.setParameter(1, contest_id);
                query.setParameter(2, newPlayers.get(app_id));
                query.setParameter(3, newPlayers.get(app_id));
                query.executeUpdate();
            }
            entityTransaction.commit();
            
        } catch (Exception e) {
             if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            System.out.println("Unable to calculate new player: " + e.toString());
        }
        return true;

    }

    public static boolean writeActivePlayers(EntityManager em, String data) throws FileNotFoundException {
        HashMap<Integer, Integer> active = loadActivePlayers(data);
        EntityTransaction entityTransaction = em.getTransaction();
        try {
            int i = 0;
            entityTransaction.begin();

            for (Integer contest_id : active.keySet()) {

                i++;
                if (i % 20 == 0) {
                    System.out.println("" + i);
                    entityTransaction.commit();
                    entityTransaction.begin();
                }


                if (em.find(Contest.class, contest_id) != null) {

                    Query query = em.createNativeQuery(""
                            + "INSERT INTO report_app(day,contest_id,active_players) "
                            + "VALUES ('" + data + "',?,?) "
                            + "ON DUPLICATE KEY UPDATE active_players = ? ; ");
                    query.setParameter(1, contest_id);
                    query.setParameter(2, active.get(contest_id));
                    query.setParameter(3, active.get(contest_id));
                    query.executeUpdate();


                } else {
                    System.out.println("Active Player Parser Not A Contest : " + contest_id);
                }

            }
            entityTransaction.commit();
            
        } catch (Exception e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            System.out.println("ERROR writeActivePlayers"+e.toString());
            
        }

        return true;
    }

    public static boolean writeUniquePlayers(EntityManager em, String data) throws FileNotFoundException {
        
        HashMap<Integer, Integer> unique = loadUniquePlayers(data);
        EntityTransaction entityTransaction = em.getTransaction();
        try {

            int i = 0;
            entityTransaction.begin();

            for (Integer app_id : unique.keySet()) {

                i++;
                if (i % 20 == 0) {
                    System.out.println("" + i);
                    entityTransaction.commit();
                    entityTransaction.begin();
                }

                App app = em.find(App.class, app_id);
                if (app == null) {
                    System.out.println("AppReporter, unique players, null app for id: " + app_id);
                    continue;
                }

                Contest contest;
                try {
                    contest = (Contest) em.createQuery("SELECT c FROM Contest c WHERE c.codeID = :codeID and c.app = :app").setParameter("codeID", "default").setParameter("app", app).getSingleResult();
                } catch (Exception e) {
                    System.out.println("NO DEFAULT CONTEST FOR" + app_id);
                    continue;
                }
                int contest_id = contest.getId();

                Query query = em.createNativeQuery(""
                        + "INSERT INTO report_app(day,contest_id,unique_players) "
                        + "VALUES ('" + data + "',?,?) "
                        + "ON DUPLICATE KEY UPDATE unique_players = ? ; ");
                query.setParameter(1, contest_id);
                query.setParameter(2, unique.get(app_id));
                query.setParameter(3, unique.get(app_id));
                query.executeUpdate();

            }
            entityTransaction.commit();
            
        } catch (Exception e) {
             if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            System.out.println("Unable to calculate unique player: " + e.toString());
        }

        return true;
    }

    public static void writeBannerDisplayByCountry(EntityManager em, String data) throws FileNotFoundException {

        try {
            List<BannerLogBean> lista = loadBannerStats();
            
            EntityManager emReadOnly = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
            int i = 0;
            EntityTransaction entityTransaction = em.getTransaction();

            try {

                entityTransaction.begin();
                for (BannerLogBean blb : lista) {
                    i++;
                    if (i % 50 == 0) {
                        System.out.println(" - "+i);
                        entityTransaction.commit();
                        entityTransaction.begin();
                    }
                    String country = blb.getCountry();
                    if(blb.getCountry()==null || blb.getCountry().equals("null") || blb.getCountry().equals("")){
                        country = null;
                    }
                    
                    
                    if (blb.getVlpe().equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY)) {
                        
                        float gain = 0f;
                        if(blb.getCount()!=null && blb.getCount()>0 && !blb.getProvider().equals("BEINTOO")){
                            gain = AppHelper.getBannerDisplayEcpm(emReadOnly, 
                                                                    BannerProviderEnum.valueOf(blb.getProvider().trim()), 
                                                                    emReadOnly.find(App.class, blb.getApp_id()), 
                                                                    country);
                            gain = gain * blb.getCount() / 1000;
                        }
                        
                        try {
                            Query query = em.createNativeQuery(""
                                    + "INSERT INTO report_banner_bycountry(day,app_id,provider,country,imps,cus1) "
                                    + "VALUES ('" + data + "',?,?,?,?,?) "
                                    + "ON DUPLICATE KEY UPDATE "
                                    + " imps = coalesce(imps,0) + ? , "
                                    + " cus1 = coalesce(cus1,0) + ? ");
                            query.setParameter(1, blb.getApp_id());
                            query.setParameter(2, blb.getProvider().trim());
                            query.setParameter(3, country);
                            query.setParameter(4, blb.getCount());
                            query.setParameter(5, gain);
                            query.setParameter(6, blb.getCount());
                            query.setParameter(7, gain);
                            query.executeUpdate();
                            
                            // aggiungo gain anche al provider BEINTOO 
                            if(gain>0){
                                query = em.createNativeQuery(""
                                    + "INSERT INTO report_banner_bycountry(day,app_id,provider,country,cus1) "
                                    + "VALUES ('" + data + "',?,?,?,?) "
                                    + "ON DUPLICATE KEY UPDATE "
                                    + " cus1 = coalesce(cus1,0) + ? ");
                                query.setParameter(1, blb.getApp_id());
                                query.setParameter(2, "BEINTOO");
                                query.setParameter(3, country);
                                query.setParameter(4, gain);
                                query.setParameter(5, gain);
                                query.executeUpdate();
                            }
                            if(gain>0){
//                                System.out.println(blb.getApp_id()+" : "+blb.getProvider()+" : "+blb.getCountry()+" : "+blb.getCount()+ " : "+gain );
                                Logger.log("ADDING "+gain+"$ FOR:app "+blb.getApp_id()+""
                                        + ", provider "+blb.getProvider()+""
                                        + ", country "+blb.getCountry()+""
                                        + ", imps "+blb.getCount(), LogContextEnum.CRON, LogLevelEnum.INFO);
                            }
                            
                        } catch (Exception e) {
                            System.out.println(":::::::::::: ");
                        }
                    }

                    // scrivo le eventuali ntd in entrambi i casi
                    try {
                        if(blb.getNtd_provider()!=null){
                            for (String provider : blb.getNtd_provider()) {
                                if(!provider.equals("")){
                                    try {
                                        Query query = em.createNativeQuery(""
                                                + "INSERT INTO report_banner_bycountry(day,app_id,provider,country,ntds) "
                                                + "VALUES ('" + data + "',?,?,?,?) "
                                                + "ON DUPLICATE KEY UPDATE ntds = coalesce(ntds,0) + ? ");
                                        query.setParameter(1, blb.getApp_id());
                                        query.setParameter(2, provider.trim());
                                        query.setParameter(3, country);
                                        query.setParameter(4, blb.getCount());
                                        query.setParameter(5, blb.getCount());
                                        query.executeUpdate();

                                    } catch (Exception e) {
                                       e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                entityTransaction.commit();
            } catch (Exception e) {
                if (entityTransaction.isActive()) {
                    entityTransaction.rollback();
                }
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("report_banner_bycountry execution is over");
    }

    public static void writeReportMethodByCountry(EntityManager em, String data) throws FileNotFoundException {

        try {
            //executeReportMethodParserScript(data);
            List<MethodLogBean> lista = loadMethodStats();

            int i = 0;
            EntityTransaction entityTransaction = em.getTransaction();

            try {

                entityTransaction.begin();
                for (MethodLogBean blb : lista) {
                    i++;
                    if (i % 50 == 0) {
                        System.out.println(" - "+i);
                        entityTransaction.commit();
                        entityTransaction.begin();
                    }
                    
                    String country = blb.getCountry();
                    if(blb.getCountry()==null || blb.getCountry().equals("null") || blb.getCountry().equals("")){
                        country = null;
                    }
                    
                    try {
                        Query query = em.createNativeQuery(""
                                + "INSERT INTO report_method_bycountry(app_id,day,provider,country,p_imps,u_imps,ntds) "
                                + "VALUES (?,'" + data + "',?,?,?,?,?) "
                                + "ON DUPLICATE KEY UPDATE "
                                + " p_imps = coalesce(p_imps,0) + ? , "
                                + " u_imps = coalesce(u_imps,0) + ? ,"
                                + " ntds = coalesce(ntds,0) + ? ;");
                        query.setParameter(1, blb.getApp_id());
                        query.setParameter(2, blb.getMethod().trim());
                        query.setParameter(3, country);
                        query.setParameter(4, blb.getP_imps());
                        query.setParameter(5, blb.getU_imps());
                        query.setParameter(6, blb.getNtds());
                        query.setParameter(7, blb.getP_imps());
                        query.setParameter(8, blb.getU_imps());
                        query.setParameter(9, blb.getNtds());
                        query.executeUpdate();

                    } catch (Exception e) {
                        Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                    }
                    
                }
                entityTransaction.commit();
            } catch (Exception e) {
                if (entityTransaction.isActive()) {
                    entityTransaction.rollback();
                }
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        } catch (Exception e) {
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        }
    }

    public static List<MethodLogBean> loadMethodStats() throws FileNotFoundException {

        List<MethodLogBean> output = new ArrayList<MethodLogBean>();
        BufferedReader reader = new BufferedReader(new FileReader(METHOD_FILENAME + "." + data));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] ar = line.trim().split(" ");
                try {
                    
                    MethodLogBean blb = new MethodLogBean(ar);
                    
                    int idx = output.indexOf(blb);
                    if(idx>-1){
                        output.get(idx).add(blb);
                    }else{
                        output.add(blb);
                    }
                    
                } catch (Exception e) {
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
        } catch (IOException e) {
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }
        return output;
    }
    
    public static List<BannerLogBean> loadBannerStats() throws FileNotFoundException {

        List<BannerLogBean> output = new ArrayList<BannerLogBean>();
        BufferedReader reader = new BufferedReader(new FileReader(BANNER_FILENAME + "." + data));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] ar = line.trim().split("\t");
                try {
                    BannerLogBean blb = new BannerLogBean(ar);

                    int idx = output.indexOf(blb);
                    if (idx > -1) {
                        output.get(idx).setCount(output.get(idx).getCount() + blb.getCount());
                    } else {
                        output.add(blb);
                    }

                    BannerLogBean beintoo = new BannerLogBean();
                    beintoo.setApp_id(blb.getApp_id());
                    beintoo.setCountry(blb.getCountry());
                    beintoo.setVlpe(blb.getVlpe());
                    beintoo.setProvider("BEINTOO");
                    if (blb.getVlpe().equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY_NTD)) {
                        String[] p = new String[1];
                        p[0] = "BEINTOO";
                        beintoo.setNtd_provider(p);
                    }
                    beintoo.setCount(blb.getCount());

                    int jdx = output.indexOf(beintoo);
                    if (jdx > -1) {
                        output.get(jdx).setCount(output.get(jdx).getCount() + blb.getCount());
                    } else {
                        output.add(beintoo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return output;
    }

    public static HashMap<Integer, Integer> loadNewPlayers(String data) throws FileNotFoundException {
        HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(NEW_FILENAME + "." + data));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] ar = line.trim().split(" ");
                try {

                    int n = Integer.parseInt(ar[1]);

                    if (output.containsKey(n)) {
                        output.put(n, output.get(n) + Integer.parseInt(ar[0]));
                    } else {
                        output.put(n, Integer.parseInt(ar[0]));
                    }

                } catch (Exception e) {
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
        } catch (IOException e) {
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }

        return output;
    }

    public static HashMap<Integer, Integer> loadUniquePlayers(String data) throws FileNotFoundException {

        HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(UNIQUE_FILENAME + "." + data));
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                String[] ar = line.trim().split(" ");
                try {
                    output.put(Integer.parseInt(ar[1]), Integer.parseInt(ar[0]));
                } catch (Exception e) {
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
        } catch (IOException e) {
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }

        return output;
    }

    public static HashMap<Integer, Integer> loadActivePlayers(String data) throws FileNotFoundException {

        HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(ACTIVE_FILENAME + "." + data));
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                String[] ar = line.trim().split(" ");
                try {
                    output.put(Integer.parseInt(ar[1]), Integer.parseInt(ar[0]));
                } catch (Exception e) {
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
        } catch (IOException e) {
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }

        return output;
    }
    
    
}

class BannerLogBean {

    VgoodLogProcessorEnum vlpe;
    Integer count;
    String country;
    Integer app_id;
    String provider;
    String[] ntd_provider;

    BannerLogBean() {
    }

    public VgoodLogProcessorEnum getVlpe() {
        return vlpe;
    }

    public void setVlpe(VgoodLogProcessorEnum vlpe) {
        this.vlpe = vlpe;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getApp_id() {
        return app_id;
    }

    public void setApp_id(Integer app_id) {
        this.app_id = app_id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String[] getNtd_provider() {
        return ntd_provider;
    }

    public void setNtd_provider(String[] ntd_provider) {
        this.ntd_provider = ntd_provider;
    }

    @Override
    public String toString() {
        String pp = "";
        if (ntd_provider != null) {
            for (String s : ntd_provider) {
                pp += s + ",";
            }
        }

        return "[ " + vlpe.name() + " APP:" + app_id + " PROVIDER:" + provider + " COUNTRY:" + country + " NTD_PROVIDER:" + pp + " NN:" + count + " ]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.vlpe != null ? this.vlpe.hashCode() : 0);
        hash = 31 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 31 * hash + (this.app_id != null ? this.app_id.hashCode() : 0);
        hash = 31 * hash + (this.provider != null ? this.provider.hashCode() : 0);
        hash = 31 * hash + Arrays.deepHashCode(this.ntd_provider);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BannerLogBean other = (BannerLogBean) obj;
        if (this.vlpe != other.vlpe) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if (this.app_id != other.app_id && (this.app_id == null || !this.app_id.equals(other.app_id))) {
            return false;
        }
        if ((this.provider == null) ? (other.provider != null) : !this.provider.equals(other.provider)) {
            return false;
        }
        if (!Arrays.deepEquals(this.ntd_provider, other.ntd_provider)) {
            return false;
        }
        return true;
    }

    public BannerLogBean(String[] line) {

//0-DELIVERY_DISPLAY       1-146   2-NEXAGE        3-      4-US  5-[NEODATA]

//0-DELIVERY_DISPLAY_NTD   1-146   2-      3-CN    4-[NEXAGE]      5-

        count = Integer.parseInt(line[0].trim());
        vlpe = VgoodLogProcessorEnum.valueOf(line[1].trim());
        app_id = Integer.parseInt(line[2].trim());

        if (vlpe.equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY)) {
            provider = line[3].trim();
            if(line.length == 7){
                country = line[5].trim();
                ntd_provider = line[6].replace("[", "").replace("]", "").trim().split(",");
            }else if(line.length<7){
                country = line[4].trim();
                ntd_provider = line[5].replace("[", "").replace("]", "").trim().split(",");
            }
            
            
        } else if (vlpe.equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY_NTD)) {
            if(line.length==6){
                country = line[4].trim();
                ntd_provider = line[5].replace("[", "").replace("]", "").trim().split(",");
            }else if(line.length==5){
                country = null;
                ntd_provider = line[4].replace("[", "").replace("]", "").trim().split(",");
            }
            
        }

        }

}

class MethodLogBean {

    VgoodLogProcessorEnum vlpe;
    Integer p_imps = 0;
    Integer u_imps = 0;
    Integer ntds = 0;
    String country;
    Integer app_id;
    String method;

    MethodLogBean() {
        
    }

    public VgoodLogProcessorEnum getVlpe() {
        return vlpe;
    }

    public void setVlpe(VgoodLogProcessorEnum vlpe) {
        this.vlpe = vlpe;
    }

    public Integer getP_imps() {
        return p_imps;
    }

    public void setP_imps(Integer p_imps) {
        this.p_imps = p_imps;
    }

    public Integer getU_imps() {
        return u_imps;
    }

    public void setU_imps(Integer u_imps) {
        this.u_imps = u_imps;
    }

    public Integer getNtds() {
        return ntds;
    }

    public void setNtds(Integer ntds) {
        this.ntds = ntds;
    }

    
    
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getApp_id() {
        return app_id;
    }

    public void setApp_id(Integer app_id) {
        this.app_id = app_id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void add(MethodLogBean mlb){
        
        p_imps += mlb.getP_imps();
        u_imps += mlb.getU_imps();
        ntds += mlb.getNtds();
        
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.vlpe != null ? this.vlpe.hashCode() : 0);
        hash = 17 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 17 * hash + (this.app_id != null ? this.app_id.hashCode() : 0);
        hash = 17 * hash + (this.method != null ? this.method.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodLogBean other = (MethodLogBean) obj;
        if (this.vlpe != other.vlpe) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if (this.app_id != other.app_id && (this.app_id == null || !this.app_id.equals(other.app_id))) {
            return false;
        }
        if ((this.method == null) ? (other.method != null) : !this.method.equals(other.method)) {
            return false;
        }
        return true;
    }
        
    public MethodLogBean(String[] line) {

//    665 DELIVERY_VGOOD_PLAYER   147     NL      null
//    527 DELIVERY_VGOOD_PLAYER   147     SE      CITYDEAL
//   3759 NOTHING_TO_DISPATCH     73      TH 
        
        vlpe = VgoodLogProcessorEnum.valueOf(line[1].trim());
        app_id = Integer.parseInt(line[2].trim());
        country = line[3];
        
        Integer count = Integer.parseInt(line[0].trim());
        
        if (vlpe.equals(VgoodLogProcessorEnum.NOTHING_TO_DISPATCH)) {
            method = "NEW_METHOD";
            ntds = count;
        } else if (vlpe.equals(VgoodLogProcessorEnum.DELIVERY_VGOOD_PLAYER)) {
            p_imps = count;
            if(line[4].equals("null")){
                method = "NEW_METHOD";
            }else{
                method = line[4].trim();
            }
        } else if(vlpe.equals(VgoodLogProcessorEnum.DELIVERY_VGOOD_USER)){
            u_imps = count;
            if(line[4].equals("null")){
                method = "NEW_METHOD";
            }else{
                method = line[4].trim();
            }
        }

    }
}
