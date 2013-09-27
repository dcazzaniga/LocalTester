
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PromocodeInsert {

    public static void main(String[] args) throws Exception {
         
        String insert = "INSERT INTO `beintoo_v1`.`vgood_promotioncode` "
                + "(`vgood_id`, `name`, `description`, `code`, `deadline`, `value`, `currency`, `number`, `url`, `status`, `creationdate`, `ext_id`) "
                + " VALUES ( "
                + "VGOOD_ID, "
                + "'NAME', "
                + "'DESCRIPTION', "
                + "'CODE', "
                + "'2014-01-31', "
                + "7, "
                + "'EUR', "
                + "1,"
                + "'URL', "
                + "2, "
                + "'2013-09-26', "
                + "'EXT_ID');";
        
        
        List<String> codes = getCodes("corriere");
        
        String vgood_id = "1467401";
        String name = "La Digital Edition per sempre a 1,99€!";
        String description = "Leggi il Corriere della Sera, i settimanali e gli inserti speciali e resta sempre informato al 60% di sconto per sempre solo per utenti VodafoneBe; Abbonati subito!";
        String url = "http://voda.it/qix6hm";
        
        insert = insert.replace("VGOOD_ID", vgood_id);
        insert = insert.replace("NAME", name);
        insert = insert.replace("DESCRIPTION", description);
        insert = insert.replace("URL", url);
        
        FileOutputStream fos = new FileOutputStream("INSERT_CORRIERE",true);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        
        
        for (String code : codes) {
            String line = insert.replace("CODE", code);
            line = line.replace("EXT_ID", UUID.randomUUID().toString());
            out.write(line+"\n");
        }
        out.close();
        
    }

    public static List<String> getCodes(String fileName) {
        List<String> codes = new ArrayList<String>();
        try {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader buf = null;
            String line = "";
            try {
                fis = new FileInputStream(fileName);
                isr = new InputStreamReader(fis);
                buf = new BufferedReader(isr);
                while ((line = buf.readLine()) != null) {
                    
                    codes.add(line.trim());
                  
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
        return codes;
    }

   
    
}
