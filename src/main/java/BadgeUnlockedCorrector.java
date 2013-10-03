
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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class BadgeUnlockedCorrector {

    public static void main(String[] args) throws Exception {

        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
         
        Map<String, String> guidAidext = getVodaPlayer();
        Map<String, String> guid5guid = getVodaPlayerSubstring();
        System.out.println("---------------------------------- "+guid5guid.size()+" PLAYERS");
        HashMap<String, ArrayList<String>> unlocked = getUnlocked();
        System.out.println("---------------------------------- "+unlocked.size()+" GUIDs");
        
        
        FileOutputStream fos = new FileOutputStream("parsedGuid",true);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        
        List<String> parsed = getAlreadyParsed();
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
                if(i++ == 100)
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
                fis = new FileInputStream("unlockedBadges");
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
   
    public static List<String> getAlreadyParsed(){
        List<String> parsed = new ArrayList<String>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream("parsedGuid");
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
