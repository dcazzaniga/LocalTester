/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbUpdater;

import com.beintoo.commons.enums.*;
import com.beintoo.commons.helper.ContestHelper;
import com.beintoo.commons.util.ConfigPath;
import com.beintoo.commons.util.StringUtils;
import com.beintoo.commons.util.VgoodLogProcessor;
import com.beintoo.commons.util.WorldMap;
import com.beintoo.entities.*;
import com.sun.media.sound.SF2GlobalRegion;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;

import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author davide
 */
public class QueryToDB {

    EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();
    EntityManager emReadOnly = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

    public static void main(String[] args) throws ParseException {

//        QueryToDB q = new QueryToDB();
        
//        QueryToDB.correct();
        QueryToDB.updateCountry();
        
//        WorldMap wm = WorldMap.getInstance();
//        
//        System.out.println(
//        wm.getCountry(57, -152));
        
        
////        String fileame = "src/resources/1220871.txt";
//
//        QueryToDB corrector = new QueryToDB();
////        corrector.correctReportApp("2012-10-26");
//        corrector.testReportApp("2012-10-29");
    }

    public void insertCodeFromFile(String filename) {

        int vgood_id = 1220871;
        Vgood v = em.find(Vgood.class, vgood_id);

        Date endDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            endDate = sdf.parse("2013-12-31");
        } catch (Exception ex) {
            endDate = v.getEnddate();
        }
        String url = "http://ad.doubleclick.net/clk;263553133;89259573;s";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader buf = null;
        String line = "";

        EntityTransaction transaction = null;
        transaction = em.getTransaction();
        transaction.begin();
        try {

            fis = new FileInputStream(filename);
            isr = new InputStreamReader(fis);
            buf = new BufferedReader(isr);
            while ((line = buf.readLine()) != null) {
                System.out.println("code ::: " + line);
                VgoodPromotioncode vp = new VgoodPromotioncode();
                vp.setVgood(em.find(Vgood.class, vgood_id));
                vp.setCode(line);
                vp.setName(v.getName());
                vp.setDescription(v.getDescription());
                vp.setDeadline(endDate);
                vp.setUrl(url);
                vp.setValue(1);
                vp.setNumber(1);
                vp.setStatus(VgoodPromotioncodeStatusType.LIMITED.getType());
                vp.setCurrency(v.getCurrency());
                vp.setPriority(1);
                String random40 = RandomStringUtils.randomAlphanumeric(40);
                vp.setExtId(random40);
                vp.setCreationdate(new Date(System.currentTimeMillis()));
                vp.setLastupdate(new Date(System.currentTimeMillis()));
                em.persist(vp);
            }
            em.getTransaction().commit();
            em.getTransaction().begin();
            System.out.println("END");

        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
            em.close();
        }



    }

    public static void updateCountry(){
        
        EntityManager emRO = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();
        try {
            
            String query = 
                    " SELECT id, latitude, longitude "
                  + " FROM user "
                  + " WHERE country is null "
                  + " AND latitude is not null and longitude is not null "
                  + " AND id > 1317399 "
                  + " limit 1000";
            
            Query q = emRO.createNativeQuery(query);
            List results = q.getResultList();
            WorldMap wm = WorldMap.getInstance();
                    
            EntityTransaction t = em.getTransaction();
            t.begin();
            
            int j = 0;
            int id = 0 ;
            try{
                
             for (int i = 0; i < results.size(); i++) {

                Object obj = results.get(i);
                Object[] objectArray = (Object[]) obj;
                
                id = ((Number) objectArray[0]).intValue();
                float latitude = ((Number) objectArray[1]).floatValue();
                float longitude = ((Number) objectArray[2]).floatValue();
                
                String country = wm.getCountry(latitude, longitude);
                if(country!=null){
                    j++;
                    if(j%100 == 0){
                        System.out.println("*********  " + j + " "+country+" id "+id);
                        t.commit();
                        t.begin();
    //                    em.createNativeQuery("SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;").executeUpdate();
                    }
                    em.createNativeQuery("UPDATE user SET country = '"+country+"' WHERE id ="+id ).executeUpdate();
//                    System.out.println(""+id+" - "+latitude+" - "+longitude+" - "+country);
                }
             }    
            System.out.println("updated  " + j + "  last id = "+id);
            }catch(Exception e){
                t.rollback();
                em.close();
                e.printStackTrace();
            }
            t.commit();
            em.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void correct(){
        
        EntityManager emRO = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_PRODUZIONE").createEntityManager();
        try {
            
            int contest = 304;
            int max = 694*10;
            
            
            String query = 
                    " SELECT id "
                  + " FROM player_score "
                  + " WHERE contest_id = "+contest+ " "
                  + " AND lastupdate < curdate() - interval 2 month "
                  + " AND balance > 1000000 "
                 //+ " AND ( lastscore > "+max+" OR bestscore > "+max+" ) "
                  + " ";
            
            Query q = emRO.createNativeQuery(query);
            List<Long> results = q.getResultList();
            
            EntityTransaction t = em.getTransaction();
            t.begin();
            
            int i = 0;
            try{
            for (Long id : results) {
                i++;
                if(i%100 == 0){
                    System.out.println("*********  " + i);
                    t.commit();
                    t.begin();
//                    em.createNativeQuery("SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;").executeUpdate();
                }
                em.createNativeQuery("DELETE FROM beintoo_v1.player_score WHERE id="+id ).executeUpdate();             
            }
            System.out.println("deleted  " + i);
            }catch(Exception e){
                t.rollback();
                em.close();
                e.printStackTrace();
            }
            t.commit();
            em.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    
}



