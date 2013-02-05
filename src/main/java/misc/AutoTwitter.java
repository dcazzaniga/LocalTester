/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import com.beintoo.commons.enums.UserConnectorProviderType;
import com.beintoo.commons.helper.TwitterHelper;
import com.beintoo.commons.helper.UserHelper;
import com.beintoo.entities.User;
import com.beintoo.entities.UserConnector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author davide
 */
public class AutoTwitter {
    
    
    public static void main(String[] args) throws TwitterException{
        
        
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        User u = em.find(User.class, 18242);
        boolean ok = TwitterHelper.shareOnTwitter(em, u, "Sharing via TwitterHelper!");
        System.out.println(""+ok+" !");
        
    }
    
}
