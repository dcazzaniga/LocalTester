/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import com.beintoo.commons.bean.FacebookBean;
import com.beintoo.commons.setting.CommonSetting;
import com.beintoo.commons.util.ConfigPath;
import com.beintoo.commons.util.FbUserManager;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author davide
 */
public class FbUserUpdater {
    
    private Map<String, String> isoMap = new HashMap<String, String>(252);
    
    public static void main(String[] args) {
        try{
            FbUserUpdater fbu = new FbUserUpdater();
            fbu.updater();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public FbUserUpdater() {
        try{
                FileInputStream fis = null;
                InputStreamReader isr = null;
                BufferedReader buf = null;
                String line = "";
                try {
                        fis = new FileInputStream("conf/ISO3166_CountryCodes_FB.csv");
                        isr = new InputStreamReader(fis);
                        buf = new BufferedReader(isr);
                        while ( (line = buf.readLine()) != null ) {
                            String[] s = line.split(";");
                            isoMap.put(s[1], s[0]);
                        }

                }catch(Exception e){
                    System.out.println(":: "+line);
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
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void updater() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        
//        FileOutputStream fos;
//        OutputStreamWriter out;
//        fos = new FileOutputStream("fbCountryUpdater.log");
//        out = new OutputStreamWriter(fos, "UTF-8");
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        Query q = em.createNativeQuery("SELECT fb.uid, u.id "
                + " FROM user u, user_fb fb WHERE u.id = fb.user_id "
                + " AND u.id = 331299 ");
        List usersList = q.getResultList();
        
        String token = "152837841401121|9g492UvmuM6gmw1gY_SPFM9aFqk";
        FacebookClient facebookClient = new DefaultFacebookClient(token);
          
        int counter = 0;
        int req = 0;
        for (Object obj : usersList) {
            
            Object[] objectArray = (Object[]) obj;
            long uid = (Long) objectArray[0];
            long id = (Long) objectArray[1];
            // out.println(queryResults.get(0).getString("name"));
            try {
                String query = "SELECT created_time , app_data, attachment, post_id, actor_id, message, impressions FROM stream WHERE actor_id = "+uid+" and source_id = "+uid+" ";
                List<JsonObject> queryResults = facebookClient.executeQuery(query, JsonObject.class);
                 for(JsonObject jo : queryResults){
                     System.out.println(""+jo.toString());
                 }
                
                
            } catch (Exception e) {
                
            }
            
            
            
//            try {
//                System.out.println("------ "+ (req++));
//                String query = "SELECT uid, name, locale, current_location, birthday_date "
//                        + " FROM user WHERE uid="+uid;
//                List<JsonObject> queryResults = facebookClient.executeQuery(query, JsonObject.class);
//                // current_location example {"id":xx,"zip":"xx","name":"xx","state":"xx","city":"xx","country":"xx"} 
//                String cl = queryResults.get(0).getString("current_location");
//                JsonObject current_location ;
//                if(!cl.equals("null")){
//                    current_location = new JsonObject(cl);
//                    if(!current_location.getString("country").equals("")){
//                       String isoCountry = countryName2iso(current_location.getString("country"));
//                       out.write("UPDATE user SET country ='"+isoCountry+"' WHERE id ="+id+" ;"
//                               + " -- "+uid+","+current_location.getString("country")+" \n");
//                       counter++;
//                    }
//                }
//                
//            } catch (Exception e) {
//                
//            }
        }
        
//        out.write("/* ------------------------------------ \n");
//        out.write("------------------------------------ \n");
//        out.write("------------------------------------ \n");
//        out.write("country updatable: "+counter+" of "+usersList.size()+"\nxxxxxxxxxxxxx */");
//        out.close();
        
    }
    
    public String countryName2iso(String country){
        return isoMap.get(country);
    }
    
}
