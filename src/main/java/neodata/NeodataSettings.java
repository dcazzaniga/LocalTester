/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neodata;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
public class NeodataSettings {
    
    
    public static void main(String[] args){
        
        buildTree();
        
        
    }
/*
    @Publisher
    SDK=Beintoo SDK
        @Website
        BTA=Beintoo Spa
                @Section
                BTS=Beintoo APP
        @Website
        C-47=HalfBrick
                @Section
                A-73=Fruit Ninja Free Android
*/
    
    
    
    private static void buildTree() {
    
        
        EntityManager em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();
        try {
            
            // SDK-
            
            Query q = em.createNativeQuery(
                    " SELECT c.id, c.name, a.id, a.name, a.app_type, a.apikey FROM report_banner rb , app a, customer c  " +
                    " WHERE rb.app_id = a.id AND a.customer_id = c.id AND day >= curdate() - interval 45 day " +
                    " GROUP BY app_id "+
                    " ORDER BY c.id, a.id" );
            List<Object> results = q.getResultList();
        
            Set<String> customers = new TreeSet<String>();
            
            FileOutputStream fos;
            OutputStreamWriter out;
            fos = new FileOutputStream("neodataTree.txt");
            out = new OutputStreamWriter(fos, "UTF-8");
            
            FileOutputStream fos1;
            OutputStreamWriter out1;
            fos1 = new FileOutputStream("locationSet.txt");
            out1 = new OutputStreamWriter(fos1, "UTF-8");
            
            for (Object obj : results) {
              
               Object[] objectArrays = (Object[])obj;
               
               String c = (String) objectArrays[1];
               
               if(!customers.contains(c)){
                   customers.add(c);
                   out.write("@Website\n");
                   out.write("C-"+objectArrays[0]+"="+c+"\n");
               }
               out.write("@Section\n");
               out.write("A-"+objectArrays[2]+"="+objectArrays[3]+" ["+objectArrays[4]+"]\n");
               out1.write("SDK;C-"+objectArrays[0]+";A-"+objectArrays[2]+";"+objectArrays[5]+"\n");
            }
            out.close();
            out1.close();
            
            
        }catch(Exception e){
            
        }

        
        
        
        
        
    }
            
    
    
    
    
}
