
import com.beintoo.commons.enums.LogContextEnum;
import com.beintoo.commons.enums.LogLevelEnum;
import com.beintoo.commons.helper.PlayerHelper;
import com.beintoo.commons.helper.nosql.PlayerAchievementHelperNoSQL;
import com.beintoo.commons.util.Logger;
import com.beintoo.entities.Player;
import com.beintoo.nosql.entity.PlayerAchievementRow;
import com.beintoo.xone.achievement.BadgeAchievement;
import com.beintoo.xone.helper.ApiConnector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class BadgeAchievementUtils {

    public static void main(String[] args) throws Exception {

        //corrector();
        
       computeAverage(Calendar.DAY_OF_YEAR);
        
        
    }

    
    public static void computeAverage(int calendarInt) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        Map<String, String> guidAidext = getVodaPlayer();
        Map<String, Double> achievements = getAchievements();
        
        FileOutputStream fos = new FileOutputStream("parsedAidExt",true);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        
        FileOutputStream fosA = new FileOutputStream("averages.csv",true);
        OutputStreamWriter outA = new OutputStreamWriter(fosA, "UTF-8");
        
        
        List<String> parsed = getAlreadyParsed("parsedAidExt");
        int n = 0;
        Map<Integer, Double[]> issued = new HashMap<Integer, Double[]>(10);
        for(String aidExt : guidAidext.values()){
            if(parsed.contains(aidExt)){
                continue;
            }
            Player player = PlayerHelper.getPlayerByAidExt(em, aidExt);
            PlayerAchievementHelperNoSQL playerAchievementHelper = new PlayerAchievementHelperNoSQL();
            // DYNAMO DB UNLOCKED ACHIEVEMENT
            Map<String, Date> sblocchi = new HashMap<String, Date>(10);
            Calendar first = Calendar.getInstance() ; 
            for(PlayerAchievementRow par : playerAchievementHelper.getPlayerAchievements(player)){
                sblocchi.put(par.getExtId(), par.getUnlockdate());
                if(par.getExtId().equals("cDLZHXFGH0DVTVWo5ERxjadSaro4QuMp5PsPhjng")){
                    first.setTime(par.getUnlockdate());
                }
            };
            
            boolean nuovo = true;
            for(String extId : sblocchi.keySet()){
                
                Calendar s = Calendar.getInstance();
                s.setTime(sblocchi.get(extId));
                
                int day =  s.get(calendarInt) - first.get(calendarInt);
                if(day<0)
                    day = 0;
                
                if(issued.containsKey(day)){
                    issued.get(day)[0] += 1;
                    issued.get(day)[1] += achievements.get(extId);
                    if(nuovo){
                        issued.get(day)[2] += 1;
                        nuovo = false;
                    }
                }else{
                    Double[] d = new Double[3];
                    d[0] = 1d;
                    d[1] = achievements.get(extId);
                    d[2] = 1d;
                    issued.put(day, d);
                }
            }    
            
            out.write(aidExt+"\n");
            if(n++ == 1000)
                    break;
        }
        
        
        for(int i : issued.keySet()){
            outA.write(i+","+issued.get(i)[0]+","+issued.get(i)[1]+"\n");
        }
        
        outA.close();
        out.close();
        
        
    }
    
    public static void corrector() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
         
        Map<String, String> guidAidext = getVodaPlayer();
        Map<String, String> guid5guid = getVodaPlayerSubstring();
        System.out.println("---------------------------------- "+guid5guid.size()+" PLAYERS");
        HashMap<String, ArrayList<String>> unlocked = getUnlocked();
        System.out.println("---------------------------------- "+unlocked.size()+" GUIDs");
        
        
        FileOutputStream fos = new FileOutputStream("parsedGuid",true);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        
        List<String> parsed = getAlreadyParsed("parsedGuid");
        int i = 0;
        for (String guid5 : unlocked.keySet()) {
            if(parsed.contains(guid5)){
                continue;
            }
            try {
                String guid = guid5guid.get(guid5);
//                System.out.println("LOGIN FOR guid:"+guid+" with guid5 "+guid5);                
//                ApiConnector.getInstance().doLogin(guid);
                
                String aidExt = guidAidext.get(guid);
                System.out.println("---------------------------------- FOR aidExt: "+aidExt);
                
                // retrive dynamo db information
                Player player = PlayerHelper.getPlayerByAidExt(em, aidExt);
                
                PlayerAchievementHelperNoSQL playerAchievementHelper = new PlayerAchievementHelperNoSQL();
                // DYNAMO DB UNLOCKED ACHIEVEMENT
                List<PlayerAchievementRow> playerAchievements = playerAchievementHelper.getPlayerAchievements(player);
                List<String> extIds = new ArrayList<String>();
                for(PlayerAchievementRow par : playerAchievements){
                    extIds.add(par.getExtId());
                }
                // LOG UNLOCKED ACHIEVEMENT
                List<String> fromLog = unlocked.get(guid5);
                for (String badgeName : fromLog) {
                    BadgeAchievement badge = BadgeAchievement.loadByName(badgeName);
                    String extId = badge.getExtId();
                    if(!extIds.contains(extId)){
                        Logger.log(" UNLOCKING "+badgeName+" ("+extId+") for "+guid, LogContextEnum.CRON, LogLevelEnum.INFO);
                        System.out.println("---------- UNLOCKING "+badgeName+" ("+extId+") for "+guid);
                        Thread.sleep(20);
                        ApiConnector.getInstance().doUpdateAchievement(guid, extId);
                    }
                }
                
                out.write(guid5+"\n");
                if(i++ == 200)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        
        out.close();
    }
    
    public static HashMap<String, ArrayList<String>> getUnlocked() {
        HashMap<String, ArrayList<String>> guidBadge = new HashMap<String, ArrayList<String>>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream("SAILOR_I");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    
                    int g = line.indexOf("guid:");
                    int e = line.indexOf("unlocked:");
                    
                    String guid = line.substring(g+5, g+10);
                    String extid = line.substring(e+9);
                    
                    if (guidBadge.containsKey(guid)) {
                        guidBadge.get(guid).add(extid);
                    } else {
                        ArrayList<String> b = new ArrayList<String>(1);
                        b.add(extid);
                        guidBadge.put(guid, b);
                    }

                }

            } catch (Exception e) {
                System.out.println("ERROR - " + line);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guidBadge;
    }

    public static Map<String, String> getVodaPlayerSubstring() {
        Map<String, String> guidAidext = new HashMap<String, String>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream("vodaplayer.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split(",");
//                    System.out.println("ADDING "+s[0].substring(0, 5));
                    guidAidext.put(s[0].substring(0, 5), s[0]);
                }

            } catch (Exception e) {
                System.out.println("ERROR - " + line);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guidAidext;
    }
    
    public static Map<String, String> getVodaPlayer() {
        Map<String, String> guidAidext = new HashMap<String, String>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream("vodaplayer.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split(",");
                    guidAidext.put(s[0], s[1]);
                }

            } catch (Exception e) {
                System.out.println("ERROR - " + line);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guidAidext;
    }
   
    public static Map<String, Double> getAchievements() {
        Map<String, Double> guidAidext = new HashMap<String, Double>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream("achievements.csv");
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    String[] s = line.split(",");
                    guidAidext.put(s[0], Double.parseDouble(s[1]));
                }

            } catch (Exception e) {
                System.out.println("ERROR - " + line);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guidAidext;
    }
    
    public static List<String> getAlreadyParsed(String fileBackuo){
        List<String> parsed = new ArrayList<String>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream(fileBackuo);
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    parsed.add(line);
                }

            } catch (Exception e) {
                System.out.println("ERROR - " + line);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsed;
    }
    
}
