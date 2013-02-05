/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbUpdater;

import com.beintoo.commons.enums.LogContextEnum;
import com.beintoo.commons.enums.LogLevelEnum;
import com.beintoo.commons.enums.VgoodLogProcessorEnum;
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
// report_app_bycountry and report_app    
    private static String ACTIVE_FILENAME = "/Users/davide/comandi/ps_parser_active_output.log";
    private static String NEW_FILENAME = "/Users/davide/comandi/ps_parser_new_output.log";
    private static String UNIQUE_FILENAME = "/Users/davide/comandi/ps_parser_output.log";
    private static String data = "2013-02-03";

    public static void main(String[] args) throws FileNotFoundException {

        System.out.println("::::: " + data);

        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();
        //writeNewPlayersByCountry(em, data);
//        writeActivePlayers(em, data);
//        writeUniquePlayers(em, data);
        writeNewPLayers(em, data);

        //loadBannerStats();
        //writeBannerDisplayByCountry(data);

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
            em.close();
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
            em.close();
        } catch (Exception e) {
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
            em.close();
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
            em.close();
        } catch (Exception e) {
            System.out.println("Unable to calculate unique player: " + e.toString());
        }

        return true;
    }

    public static void writeBannerDisplayByCountry(String data) throws FileNotFoundException {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();

            List<BannerLogBean> lista = loadBannerStats();
            int i = 0;
            EntityTransaction entityTransaction = em.getTransaction();

            try {

                entityTransaction.begin();
                for (BannerLogBean blb : lista) {
                    i++;
                    if (i % 50 == 0) {
                        System.out.println(" " + i);
                        entityTransaction.commit();
                        entityTransaction.begin();
                    }
                    String country = blb.getCountry();
                    if (blb.getCountry() == null || blb.getCountry().equals("null") || blb.getCountry().equals("")) {
                        country = null;
                    }


                    if (blb.getVlpe().equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY)) {
                        try {
                            Query query = em.createNativeQuery(""
                                    + "INSERT INTO report_banner_bycountry(day,app_id,provider,country,imps) "
                                    + "VALUES ('" + data + "',?,?,?,?) "
                                    + "ON DUPLICATE KEY UPDATE imps = COALESCE(imps,0) + ? ");
                            query.setParameter(1, blb.getApp_id());
                            query.setParameter(2, blb.getProvider().trim());
                            query.setParameter(3, country);
                            query.setParameter(4, blb.getCount());
                            query.setParameter(5, blb.getCount());
                            query.executeUpdate();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                        }
                    }

                    // scrivo le eventuali ntd in entrambi i casi
                    try {

                        if (blb.getNtd_provider() != null) {
                            for (String provider : blb.getNtd_provider()) {
                                if (!provider.equals("")) {
                                    try {
                                        Query query = em.createNativeQuery(""
                                                + "INSERT INTO report_banner_bycountry(day,app_id,provider,country,ntds) "
                                                + "VALUES ('" + data + "',?,?,?,?) "
                                                + "ON DUPLICATE KEY UPDATE ntds = COALESCE(ntds,0) + ? ");
                                        query.setParameter(1, blb.getApp_id());
                                        query.setParameter(2, provider.trim());
                                        query.setParameter(3, country);
                                        query.setParameter(4, blb.getCount());
                                        query.setParameter(5, blb.getCount());
                                        query.executeUpdate();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("error");
                    }
                }
                entityTransaction.commit();
                em.close();
            } catch (Exception e) {
                if (entityTransaction.isActive()) {
                    entityTransaction.rollback();
                }
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("report_app_bycountry execution is over");
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
                    Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.log(e, LogContextEnum.CRON, LogLevelEnum.INFO);
            }
        }
        for (BannerLogBean blb : output) {
            System.out.println(blb.toString());
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

            country = line[5].trim();
            provider = line[3].trim();
            ntd_provider = line[6].replace("[", "").replace("]", "").trim().split(",");

        } else if (vlpe.equals(VgoodLogProcessorEnum.DELIVERY_DISPLAY_NTD)) {
            country = line[4].trim();
            // split anche se dovrei trovare solo un provider
            ntd_provider = line[5].replace("[", "").replace("]", "").trim().split(",");

        }

    }
}