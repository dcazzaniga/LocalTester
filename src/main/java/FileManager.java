
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author
 * dcazzaniga
 */
public class FileManager {
    
   
    public static void main(String[] args)  {
        
            String codesfileName = "testCodes";
            List<String> lines = new ArrayList<String>();
            
            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(codesfileName));
                String in;
                while ((in = r.readLine()) != null) {
                    lines.add(in);
                }
                r.close();
            } catch (Exception ex) {
                
            }
            
            System.out.println("FIRING: "+lines.remove(0));
            
           
            try {
                int remainingCodes = lines.size();
                BufferedWriter w = new BufferedWriter(new FileWriter(codesfileName));
                for (String code : lines) {
                    w.append(code);
                    w.newLine();
                }
                w.close();
            } catch (Exception ex) {
                
            }
            
    }
    
    
    
    
    
}
