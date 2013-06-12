/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.publisher;

import com.beintoo.commons.util.ConfigPath;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author davide
 */
public class ReportPublisherDeveloper {

    private List<PublisherString> lista = new ArrayList<PublisherString>();
    private int[] threshold = {1, 50, 500, 1000, 5000, 10000, 50000, 500000, 2000000};
    private String noCustomer = "1,2,961,10,12,13,18,58,959";
    private String topCustomer = "47,1199,628,858,811";
    private boolean top = false;
    private Set<Integer> apps = new TreeSet<Integer>();

    public static void main(String[] args) {

        ReportPublisherDeveloper rpd = new ReportPublisherDeveloper();
        rpd.setTop(false);
        rpd.fillList();
        rpd.addLastWeek();
        rpd.addGetReward();
        rpd.addBannerInfo();
        rpd.addNoActivityApp();
        rpd.addAchievementInfo();
        rpd.addGiveBedollarsInfo();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String data = sdf.format(new Date());

        if (!rpd.isTop()) {
            rpd.toCSVsimple("developerList." + data + ".csv", rpd.getLista());
            rpd.toCSV("developerApps." + data + ".csv", rpd.getLista());
            rpd.toCSVstats("developerAppStats." + data + ".csv", rpd.getLista());
        } else {
            rpd.toCSVsimple("TopDeveloperList." + data + ".csv", rpd.getLista());
            rpd.toCSV("TopDeveloperApps." + data + ".csv", rpd.getLista());
            rpd.toCSVstats("TopDeveloperAppStats." + data + ".csv", rpd.getLista());
        }

    }

    public int[] getThreshold() {
        return threshold;
    }

    public void setThreshold(int[] threshold) {
        this.threshold = threshold;
    }

    public String getNoCustomer() {
        return noCustomer;
    }

    public void setNoCustomer(String noCustomer) {
        this.noCustomer = noCustomer;
    }

    public String getTopCustomer() {
        return topCustomer;
    }

    public void setTopCustomer(String topCustomer) {
        this.topCustomer = topCustomer;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public Set<Integer> getApps() {
        return apps;
    }

    public void setApps(Set<Integer> apps) {
        this.apps = apps;
    }

    public List<PublisherString> getLista() {
        return lista;
    }

    public void setLista(List<PublisherString> lista) {
        this.lista = lista;
    }

    public void fillList() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";
            query = "   SELECT cu.id, " // 0
                    + "     cu.name, cu.email , cu.creationdate, " // 1
                    + "     a.id, " // 4
                    + "     a.app_type, a.name, a.creationdate, a.status, " // 5

                    + "     IFNULL(SUM(new_players),0) pp, " // 9 - tot players
                    + "     IFNULL(SUM(users),0) uu, " //  tot user
                    + "     IFNULL(MAX(unique_players),'-') up, " //  active players

                    + "     IFNULL(AVG(new_players),0) ap, " // 12 avg 
                    + "     IFNULL(AVG(users),0) au, " // 13
                    + "     IFNULL(AVG(sessions),0) l, " // 14
                    + "     IFNULL(AVG(submits),0) asu, " // 15
                    + "     IFNULL(AVG(assigned_vgoods),0) auav, " // 16
                    + "     IFNULL(AVG(player_assigned_vgoods),0) apav , " // 17 
                    + "     IFNULL(AVG(marketplace_counter_1),0) amp " // 18
                    + " FROM report_app ra, contest c, app a, customer cu "
                    + " WHERE ra.contest_id = c.id and c.app_id = a.id AND cu.id = a.customer_id "
                    + "     and cu.id not in (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += " and cu.isdeveloper = 1 "
                    + " GROUP BY a.id "
                    + " ORDER BY pp desc; ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);

                as.setAppString(objectArray[5]
                        + "," + objectArray[6].toString().replace(",", "")
                        + "," + objectArray[7].toString().substring(0, 10)
                        + "," + objectArray[8]);

                as.setInfos(objectArray[9]
                        + "," + objectArray[10]
                        + "," + objectArray[11]
                        + "," + objectArray[12]
                        + "," + objectArray[13]
                        + "," + objectArray[14]
                        + "," + objectArray[17]
                        + "," + objectArray[18]+",," //                        +","+objectArray[15]
                        //                        +","+objectArray[16]
                        );

                Integer totPlayers = ((BigDecimal) objectArray[9]).intValue();
                Integer segment = 0;
                for (int i = 0; i < threshold.length; i++) {
                    if (totPlayers < threshold[i]) {
                        segment = i;
                        break;
                    }
                    segment = threshold.length;
                }

                int idx = lista.indexOf(ps);

                if (idx > -1) {
                    if (segment > lista.get(idx).getSegment()) {
                        lista.get(idx).setSegment(segment);
                    }

                    lista.get(idx).addApp(as);

                } else {
                    ps.setInfos(objectArray[1].toString().replace(",", " ")
                            + "," + objectArray[2]
                            + "," + objectArray[3].toString().substring(0, 10));
                    ps.setSegment(segment);
                    ps.addApp(as);
                    lista.add(ps);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void addGetReward() {
        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";
            query = "   SELECT cu.id, " // 0
                    + "     cu.name, cu.email , cu.creationdate, " // 1
                    + "     a.id, " // 4
                    + "     a.app_type, a.name, a.creationdate, a.status, " // 5
                    + "     IFNULL( AVG(u_imps+p_imps),0) ,"
                    + "     IFNULL( AVG(ntds),0) , "
                    + "     IFNULL( SUM( IF( day >= curdate() - interval 1 week , u_imps+p_imps,0 ) ),0 ), "
                    + "     IFNULL( SUM( IF( day >= curdate() - interval 1 week , ntds,0 ) ),0 ) "
                    + " FROM report_method_bycountry rm, app a, customer cu "
                    + " WHERE rm.app_id = a.id AND cu.id = a.customer_id "
                    + "     and cu.id not in (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += " and cu.isdeveloper = 1 "
                    + " GROUP BY a.id  ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);


                int idx = lista.indexOf(ps);

                if (idx > -1) {
                    int appIdx = lista.get(idx).getApps().indexOf(as);
                    if (appIdx > -1) {

                        lista.get(idx).getApps().get(appIdx).addInfos("," + objectArray[9] + "," + objectArray[10]);;
                        lista.get(idx).getApps().get(appIdx).addLastWeekInfo("," + objectArray[11] + "," + objectArray[12]);;
                    } else {

                        as.setAppString(objectArray[5] + "," + objectArray[6].toString().replace(",", "") + "," + objectArray[7].toString().substring(0, 10) + "," + objectArray[8]);

                        lista.get(idx).addApp(as);
                    }
                } else {
                    ps.setInfos(objectArray[1].toString().replace(",", " ") + "," + objectArray[2] + "," + objectArray[3].toString().substring(0, 10));
                    as.setAppString(objectArray[5] + "," + objectArray[6].toString().replace(",", "") + "," + objectArray[7].toString().substring(0, 10) + "," + objectArray[8]);



                    ps.addApp(as);
                    lista.add(ps);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLastWeek() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";
            query = "   SELECT cu.id, " // 0
                    + "     cu.name, cu.email , cu.creationdate, " // 1
                    + "     a.id, " // 4
                    + "     a.app_type, a.name, a.creationdate, a.status, " // 5
                    + "     IFNULL(SUM(new_players),0) pp, " // 9 - tot players
                    + "     IFNULL(SUM(users),0) uu, " //  tot user
                    + "     IFNULL(MAX(unique_players),'-') up, " //  active players

                    + "     IFNULL(AVG(new_players),0) ap, " // 12 avg 
                    + "     IFNULL(AVG(users),0) au, " // 13 
                    + "     IFNULL(AVG(sessions),0) l, " // 14
                    + "     IFNULL(AVG(submits),0) asu, " // 15
                    + "     IFNULL(AVG(assigned_vgoods),0) auav, " // 16
                    + "     IFNULL(AVG(player_assigned_vgoods),0) apav , " // 17
                    + "     IFNULL(AVG(marketplace_counter_1),0) amp "
                    + " FROM report_app ra, contest c, app a, customer cu "
                    + " WHERE ra.contest_id = c.id and c.app_id = a.id AND cu.id = a.customer_id "
                    + "     and cu.id not in (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += "     and cu.isdeveloper = 1 "
                    + " AND ra.day >= curdate() - interval 1 week "
                    + " GROUP BY a.id "
                    + " ORDER BY pp desc; ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);
                String lastWeekInfo =
                        objectArray[9]
                        + "," + objectArray[10]
                        + "," + objectArray[11]
                        + "," + objectArray[12]
                        + "," + objectArray[13]
                        + "," + objectArray[14]
                        + "," + objectArray[17]
                        + "," + objectArray[18]+",," //                        +","+objectArray[15]
                        //                        +","+objectArray[16]
                        ;
                try {
                    int idx = lista.indexOf(ps);
                    int appIdx = lista.get(idx).getApps().indexOf(as);
                    lista.get(idx).getApps().get(appIdx).setLastWeekInfos(lastWeekInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBannerInfo() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";
            query = "   SELECT cu.id, "
                    + "    cu.name, cu.email , cu.creationdate, "
                    + "    a.id, "
                    + "    a.app_type, a.name, a.creationdate, a.status,"
                    + "    IFNULL(AVG(imps+ntds),0) avgImps , "
                    + "    IFNULL(SUM(IF(rb.day >= curdate() - interval 1 week,imps+ntds,0)),0) impsLastWeek "
                    + " FROM report_banner_bycountry rb,  app a, customer cu "
                    + " WHERE rb.provider like 'BEINTOO' and rb.app_id = a.id AND cu.id = a.customer_id "
                    + " AND cu.id not in  (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }

            query += " GROUP BY a.id ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);


                int idx = lista.indexOf(ps);

                if (idx > -1) {

                    int appIdx = lista.get(idx).getApps().indexOf(as);
                    if (appIdx > -1) {
                        lista.get(idx).getApps().get(appIdx).setImps(objectArray[9].toString());
                        lista.get(idx).getApps().get(appIdx).setLastWeekImps(objectArray[10].toString());
                    }
//                        else{
//                            as.setAppString(objectArray[5]+","+objectArray[6].toString().replace(",","")+","+objectArray[7].toString().substring(0,10)+","+objectArray[8]);                          
//                            lista.get(idx).addApp(as);
//                        }

                } 
//                else {
//                    ps.setInfos(objectArray[1].toString().replace(",", " ") + "," + objectArray[2] + "," + objectArray[3].toString().substring(0, 10));
//
//                    as.setAppString(objectArray[5] + "," + objectArray[6].toString().replace(",", "") + "," + objectArray[7].toString().substring(0, 10) + "," + objectArray[8]);
//                    as.setImps(objectArray[9].toString());
//                    as.setLastWeekImps(objectArray[10].toString());
//                    ps.addApp(as);
//                    lista.add(ps);
//                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // achievement 
    public void addAchievementInfo() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";

            query = "   SELECT cu.id, " // 0
                    + "    cu.name, cu.email , cu.creationdate, "
                    + "    a.id, " // 4
                    + "    a.app_type, a.name, a.creationdate, a.status," // 5
                    + "    IFNULL(SUM(ra.player_unlocked),0) avgImps , " // 9 
                    + "    IFNULL(SUM(IF(ra.day >= curdate() - interval 1 week,ra.player_engaged,0)),0) peLastWeek,"
                    + "    MAX(ra.day) "
                    + " FROM achievement ac, report_achievement ra, app a, customer cu "
                    + " WHERE ac.id = ra.achievement_id and ac.app_id = a.id and cu.id = a.customer_id  "
                    + " AND cu.id not in  (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += " GROUP BY a.id ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);
                int idx = lista.indexOf(ps);

                if (idx > -1) {

                    int appIdx = lista.get(idx).getApps().indexOf(as);
                    if (appIdx > -1) {
                        lista.get(idx).getApps().get(appIdx).setAchievements(objectArray[9].toString());
                        lista.get(idx).getApps().get(appIdx).setLastWeekAchievements(objectArray[10].toString());
                    } else {
                        as.setAppString(objectArray[5]
                                + "," + objectArray[6].toString().replace(",", "")
                                + "," + objectArray[7].toString().substring(0, 10)
                                + "," + objectArray[8]);

                        as.setAchievements(objectArray[9].toString());
                        as.setLastWeekAchievements(objectArray[10].toString());

                        lista.get(idx).addApp(as);
                    }


                } else {
                    ps.setInfos(objectArray[1].toString().replace(",", " ")
                            + "," + objectArray[2]
                            + "," + objectArray[3].toString().substring(0, 10));

                    as.setAppString(objectArray[5]
                            + "," + objectArray[6].toString().replace(",", "")
                            + "," + objectArray[7].toString().substring(0, 10)
                            + "," + objectArray[8]);

                    as.setAchievements(objectArray[9].toString());
                    as.setLastWeekAchievements(objectArray[10].toString());
                    ps.addApp(as);
                    lista.add(ps);
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // give bedollars
    public void addGiveBedollarsInfo() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String query = "";

            query = "   SELECT cu.id, "
                    + "    cu.name, cu.email , cu.creationdate, "
                    + "    a.id, "
                    + "    a.app_type, a.name, a.creationdate, a.status,"
                    + "    COUNT(*) tot , "
                    + "    MAX(uc.creationdate) "
                    + " from user_credit uc, app a, customer cu "
                    + " WHERE uc.app_id = a.id AND a.customer_id = cu.id "
                    + " AND reason like 'GIVE_BEDOLLARS%'"
                    + " AND cu.id not in  (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += " and uc.creationdate > now() - interval 1 week  "
                    + " GROUP BY a.id ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                apps.add(a);
                AppString as = new AppString(a);
                int idx = lista.indexOf(ps);

                if (idx > -1) {

                    int appIdx = lista.get(idx).getApps().indexOf(as);
                    if (appIdx > -1) {
                        lista.get(idx).getApps().get(appIdx).setGiveBedollars(objectArray[9].toString());
                        // lista.get(idx).getApps().get(appIdx).setLastWeekAchievements(objectArray[10].toString());
                    } else {
                        as.setAppString(objectArray[5]
                                + "," + objectArray[6].toString().replace(",", "")
                                + "," + objectArray[7].toString().substring(0, 10)
                                + "," + objectArray[8]);

                        as.setGiveBedollars(objectArray[9].toString());
                        // as.setLastWeekAchievements(objectArray[10].toString());

                        lista.get(idx).addApp(as);
                    }


                } else {
                    ps.setInfos(objectArray[1].toString().replace(",", " ")
                            + "," + objectArray[2]
                            + "," + objectArray[3].toString().substring(0, 10));

                    as.setAppString(objectArray[5]
                            + "," + objectArray[6].toString().replace(",", "")
                            + "," + objectArray[7].toString().substring(0, 10)
                            + "," + objectArray[8]);

                    as.setGiveBedollars(objectArray[9].toString());
                    // as.setLastWeekAchievements(objectArray[10].toString());
                    ps.addApp(as);
                    lista.add(ps);
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addNoActivityApp() {

        try {

            EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

            String ss = apps.toString().replace("[", " ").replace("]", " ");

            String query = "";
            query = "   SELECT cu.id, "
                    + "    cu.name, cu.email , cu.creationdate, "
                    + "    a.id, "
                    + "    a.app_type, IFNULL(a.name,'null'), a.creationdate, a.status "
                    + " FROM app a, customer cu "
                    + " WHERE cu.id = a.customer_id "
                    + " AND cu.id not in (" + noCustomer + ") ";
            if (isTop()) {
                query += " and cu.id in (" + topCustomer + ")";
            }
            query += " AND a.id NOT IN (" + ss + ")"
                    + " GROUP BY a.id ";

            System.out.println("::: " + query);
            Query q = em.createNativeQuery(query);

            List<Object> results = q.getResultList();
            for (Object obj : results) {
                Object[] objectArray = (Object[]) obj;

                Integer c = ((Long) objectArray[0]).intValue();
                PublisherString ps = new PublisherString(c);

                Integer a = ((Long) objectArray[4]).intValue();
                AppString as = new AppString(a);
                as.setAppString(objectArray[5] + "," + objectArray[6].toString().replace(",", "") + "," + objectArray[7].toString().substring(0, 10) + "," + objectArray[8]);

                int idx = lista.indexOf(ps);

                if (idx > -1) {
                    lista.get(idx).addApp(as);

                } else {
                    ps.setInfos(objectArray[1].toString().replace(",", " ") + "," + objectArray[2] + "," + objectArray[3].toString().substring(0, 10));
                    ps.addApp(as);
                    lista.add(ps);
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void toCSVsimple(String developerFormattedcsv, List<PublisherString> psList) {

        try {

            /////////// OUTPIUT
            FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() + "/" + developerFormattedcsv);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write("segment,id,name,email,creationdate,n. apps\n");

            for (PublisherString ps : psList) {
                out.write(ps.getSegment() + "," + ps.getId() + "," + ps.getInfos() + "," + (ps.getApps() != null ? ps.getApps().size() : "0") + "\n");
            }

            out.close();

        } catch (Exception e) {
        }

    }

    private void toCSV(String developerFormattedcsv, List<PublisherString> psList) {

        try {

            /////////// OUTPIUT
            FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() + "/" + developerFormattedcsv);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write("segment,id,name,email,creationdate\n"
                    + ",id,os,name,creationdate,status\n\n");

            for (PublisherString ps : psList) {

                out.write(ps.getSegment() + "," + ps.getId() + "," + ps.getInfos() + "\n");
                for (AppString as : ps.getApps()) {
                    out.write("," + as.getId() + "," + as.getAppString() + "\n");
                }
                out.write("\n");

            }

            out.close();

        } catch (Exception e) {
        }

    }

    private void toCSVstats(String developerFormattedcsv, List<PublisherString> psList) {

        try {

            /////////// OUTPUT
            FileOutputStream fos = new FileOutputStream(ConfigPath.getConfigPath() + "/" + developerFormattedcsv);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write("segment,id,customer,email,creationdate"
                    + ",id,os,name,creationdate,status"
                    + ",tot. players,tot. users,daily active players,avg new players,avg new users,avg sessions, avg submits,avg bestore visits,avg getReward,avg ntds getReward,banner imps,achiev.,give bedollars"
                    + ",tot. players,tot. users,daily active players,avg new players,avg new users,avg sessions, avg submits,avg bestore visits,getReward,ntds getReward,banner imps,achiev.,give bedollars"
                    + "\n");
            for (PublisherString ps : psList) {

                
                for (AppString as : ps.getApps()) {
                    out.write(ps.getSegment() + "," + ps.getId() + "," + ps.getInfos() + "");
                    out.write("," + as.getId() + "," + as.getAppString());
                    out.write("," + (as.getInfos() != null ? as.getInfos() : ",,,,,,,,,"));
                    out.write("," + (as.getImps() != null ? as.getImps() : ""));
                    out.write("," + (as.getAchievements() != null ? as.getAchievements() : ""));
                    out.write("," + (as.getGiveBedollars() != null ? as.getGiveBedollars() : ""));

                    out.write("," + (as.getLastWeekInfos() != null ? as.getLastWeekInfos() : ",,,,,,,,,"));
                    out.write("," + (as.getLastWeekImps() != null ? as.getLastWeekImps() : ""));

                    out.write("\n");
                }
                out.write("\n");

            }

            out.close();

        } catch (Exception e) {
        }

    }
}
