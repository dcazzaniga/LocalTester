package misc;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;


import jxl.*;
import java.util.*;
import jxl.Workbook;
import jxl.write.DateFormat;
import jxl.write.Number;

import jxl.write.*;
import java.text.SimpleDateFormat;
import misc.GoogleAnalyticsRequest;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author davide
 */
public class ReportFlussi {

    private List<FileLine> fileLines = new ArrayList<FileLine>();
    private Map<String, String> template_label = new HashMap<String, String>();
    Long deals = 0L;
    Long vgood = 0L;

    private String[] adility = { "0" , "0" };
    private String[] vouchacha = {"0" ,"0" };

    int row = 1;
    int col = 1;

    EntityManager em ;

    public ReportFlussi() {
        
        em = Persistence.createEntityManagerFactory("BeintooEntitiesPU_LOCAL_LOCALHOST").createEntityManager();

        // from db: tbale mail queue
        template_label.put("/activities_summary", "ActivitiesAummary");
        template_label.put("/challenge_invite","ChallengeInvite");
        template_label.put("/challenge_won","ChallengeWon");
        template_label.put("/get_it_real","GetItReal");
        template_label.put("/gift_received","GiftReceived");
        template_label.put("/inactive_users","InactiveUser");
        template_label.put("/news_dev.html","Newsletter");
        template_label.put("/news_user_en.html","Newsletter");
        template_label.put("/news_user_it.html","Newsletter");
        template_label.put("/user_invitation","UserInvitation");
        template_label.put("/user_registration","UserRegistration");
        template_label.put("/vgood_assigned","VgoodAssigned");

        // from google analytics
        template_label.put("Email:ActivitesSummary","ActivitiesSummary");
        template_label.put("Email:BedollarsAssigned","BedollarsAssigned");
        template_label.put("Email:BusinessRegistration","BusinessRegistration");
        template_label.put("Email:ChallengeInvite","ChallengeInvite");
        template_label.put("Email:ChallengeWon","ChallengeWon");
        template_label.put("Email:CustomerForgotPassword","CustomerForgotPassword");
        template_label.put("Email:DealAssigned","VgoodAssigned");
        template_label.put("Email:DeveloperNewsletter","DeveloperNewsletter");
        template_label.put("Email:NewsletterAdv","NewsletterAdv");
        template_label.put("Email:GetItReal","GetItReal");
        template_label.put("Email:GiftReceived","GiftReceived");
        template_label.put("Email:InactiveUser","InactiveUser");
        template_label.put("Email:MonthlyNewsletter","Newsletter");
        template_label.put("Email:UserForgotPassword","UserForgotPassword");
        template_label.put("Email:UserInvitation","UserInvitation");
        template_label.put("Email:UserRegistration","UserRegistration");
        template_label.put("Email:VgoodAssigned","VgoodAssigned");
        
    }

    public static void main(String[] args){

        Date from = new Date(new Date().getTime() - 1*86400000);
        Date to = new Date(new Date().getTime() +   1*86400000);
        ReportFlussi rf = new ReportFlussi();

        try{

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            File destinationFile = new File("Mail_Adility_Vouchahcha_"+sdf.format(new Date())+".csv");
            FileOutputStream fos = new FileOutputStream(destinationFile);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            String filename = "Mail_Adility_Vouchahcha_"+sdf.format(new Date())+".xls";
            WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filename), ws);
            WritableSheet s = workbook.createSheet("Sheet1", 0);
 
            rf.addMailQueue(from, to);
            rf.addMailView(from, to);
            rf.addAdiVouDate(from, to);
            rf.writeCSV(out);
            rf.writeXLS(s);

            from = new Date(new Date().getTime() - 7*86400000);
            to = new Date(new Date().getTime() +   1*86400000);
            rf.clearFileLines();
            
            System.out.println(".................");
            rf.addMailQueue(from, to);
            rf.addMailView(from, to);
            rf.addAdiVouDate(from, to);
            rf.writeCSV(out);
            rf.writeXLS(s);

            out.close();
            workbook.write();
            workbook.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void writeCSV( OutputStreamWriter out){
        try{
            for(FileLine f: this.fileLines){
                if(f.getLabel().equals("vgood_assigned")){
                    Long l = Long.parseLong(f.getView())+ deals;
                    out.write(f.getLabel()+","+f.getSent()+","+l+"\n");
                }else{
                    out.write(f.getLabel()+","+f.getSent()+","+f.getView()+"\n");
                }
            }
            out.write("\n");
            out.write(" , assigned , converted/mail opened \n");
            out.write("ADILITY,"+adility[0]+","+adility[1]+"\n");
            out.write("VOUCHACHA,"+vouchacha[0]+","+vouchacha[1]);
            out.write("\n" );
            out.write("\n" );
            out.write("\n" );
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void writeXLS(WritableSheet s)    throws WriteException {

        /* Format the Font */

        
        WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableCellFormat cf_label = new WritableCellFormat(wf);
        cf_label.setWrap(false);

        Label l ;
        Number n ;
        Formula frm;
        WritableCellFormat cf_pr = new WritableCellFormat(NumberFormats.PERCENT_FLOAT);
        WritableCellFormat cf_long = new WritableCellFormat(NumberFormats.THOUSANDS_INTEGER );

        for(FileLine f: this.fileLines){

            if(f.getLabel().equals("VgoodAssigned")){
                Long lg = Long.parseLong(f.getView())+ deals;
                l = new Label(col,row, f.getLabel() ,cf_label);
                s.addCell(l);
                col++;
                //l = new Label(col,row, f.getSent() ,cf_long);
               if(!f.getSent().equals("")){
                    n = new Number(col, row, Double.parseDouble(f.getSent()));
                    s.addCell(n);
                }
                col++;
                //l = new Label(col,row, lg+"" ,cf_long);
                n = new Number(col, row, lg);
                s.addCell(n);
                col++;
                int frm_row = row+1;
                frm = new Formula(col, row, "IF(C" + frm_row + ">0, D" + frm_row + "/C" + frm_row + ", \" \" )" , cf_pr );
                s.addCell(frm);
            }else if(f.getLabel().equals("mail")){
                l = new Label(col,row, f.getLabel() ,cf_label);
                s.addCell(l);
                col++;
                //l = new Label(col,row, f.getSent() ,cf_long);
                l = new Label(col,row, f.getSent() ,cf_label);
                s.addCell(l);
                col++;
                //l = new Label(col,row, f.getView() ,cf_long);
                l = new Label(col,row, f.getView() ,cf_label);
                s.addCell(l);
                col++;

            } else
             {
                l = new Label(col,row, f.getLabel() ,cf_label);
                s.addCell(l);
                col++;
                //l = new Label(col,row, f.getSent() ,cf_long);
                if(!f.getSent().equals("")){
                    n = new Number(col, row, Double.parseDouble(f.getSent()));
                    s.addCell(n);
                }
                col++;
                //l = new Label(col,row, f.getView() ,cf_long);
                if(!f.getView().equals("")){
                    n = new Number(col, row, Double.parseDouble(f.getView()));
                    s.addCell(n);
                }
                col++;
                int frm_row = row+1;
                frm = new Formula(col, row, "IF(C" + frm_row + ">0, D" + frm_row + "/C" + frm_row + ", \" \" )" , cf_pr );
                s.addCell(frm);
            }
            col = 1;
            row++;
        }

        row++;

        col++;
        l = new Label(col,row, "assigned" ,cf_label);
        s.addCell(l);
        col++;
        l = new Label(col,row, "converted/mail opened" ,cf_label);
        s.addCell(l);

        col = 1;
        row++;

        l = new Label(col,row, "ADILITY" ,cf_label);
        s.addCell(l);
        col++;
        //l = new Label(col,row, adility[0] ,cf_long);
        n = new Number(col, row, Double.parseDouble(adility[0]));
        s.addCell(n);
        col++;
        //l = new Label(col,row, adility[1] ,cf_long);
        n = new Number(col, row, Double.parseDouble(adility[1]));
        s.addCell(n);
        col++;
        int frm_row = row+1;
        frm = new Formula(col, row, "IF(C" + frm_row + ">0, D" + frm_row + "/C" + frm_row + ", \" \" )" , cf_pr );
        s.addCell(frm);
        col = 1;
        row++;

        l = new Label(col,row, "VOUCHACHA" ,cf_label);
        s.addCell(l);
        col++;
        //l = new Label(col,row, vouchacha[0] ,cf_long);
        n = new Number(col, row, Double.parseDouble(vouchacha[0]));
        s.addCell(n);
        col++;
        n = new Number(col, row, Double.parseDouble(vouchacha[1]));
        s.addCell(n);
        col++;
        frm_row = row+1;
        frm = new Formula(col, row, "IF(C" + frm_row + ">0, D" + frm_row + "/C" + frm_row + ", \" \" )" , cf_pr );
        s.addCell(frm);
        col = 1;
        row++;
        row++;
        row++;

        // adding formula




    }

    public void addMailQueue(Date from, Date to){
   
        try{
        String query = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        this.fileLines.add(new FileLine("Data from "+sdf.format(from)+" to "+sdf.format(new Date()), "", ""));
        this.fileLines.add(new FileLine("mail","sent","viewed"));
        
        query = "  select SUBSTRING_INDEX(template,'/',2) template , count(*) n "
                + " from mail_queue  "
                + " where DATE(delivereddate) > '"+sdf.format(from)+"' "
                + " AND DATE(delivereddate) < '"+sdf.format(to)+"' "
                + " AND status = 2 "
                + " group by  SUBSTRING_INDEX(template,'/',2)  "
                + " limit 1000 ";
                
            System.out.println(query);
        Query q = em.createNativeQuery(query);
        List results = q.getResultList();

        for (int i = 0; i < results.size(); i++) {
            Object obj = results.get(i);
            Object[] objectArray = (Object[]) obj;
            try {
                String template = ((String) objectArray[0]).toString();
                String sent = ((Long) objectArray[1]).toString();
                System.out.println(sent);
                if(template_label.containsKey(template)){
                    int index = this.fileLines.indexOf(new FileLine(template_label.get(template),sent,"0"));
                    if(index>-1){
                        Long l = Long.parseLong(this.fileLines.get(index).getSent());
                        l += Long.parseLong(sent);
                        this.fileLines.get(index).setSent(l.toString());
                    }else{
                        FileLine fl = new FileLine(template_label.get(template),sent,"0");
                        this.fileLines.add(fl);
                    }


                }
            }catch (Exception e) { e.printStackTrace();}

        }

        }catch(Exception e){

        }
        
    }

    public void addMailView(Date from, Date to){
        
        GoogleAnalyticsRequest gar = new GoogleAnalyticsRequest();
        List<String> dimensions = new ArrayList<String>();
        List<String> metrics = new ArrayList<String>();
        String segment = null;
        String sort = null;
        String filter = null;
        List<String[]> ga;
        segment = "";
        sort = "";
        filter = "ga:eventCategory==Email,ga:eventAction==View";
        dimensions.add("eventLabel");
        metrics.add("totalEvents");
        //metrics.add("uniqueEvents");
        ga = gar.stringRequest(dimensions, metrics, segment, sort, filter, from, to);

        for(String[] s: ga){

            if(template_label.containsKey(s[0])){

                if(s[0].equals("DealAssigned")){
                    this.deals = Long.parseLong(s[1]);
                }
                if(s[0].equals("VgoodAssigned")){
                    this.vgood = Long.parseLong(s[1]);
                }

                int index = this.fileLines.indexOf(new FileLine(template_label.get(s[0])));
                if(index>-1){
                    this.fileLines.get(index).setView(s[1]);
                }else{
                    this.fileLines.add(new FileLine(template_label.get(s[0]), "0", s[1]));
                }

            }


        }
    }

    public void addAdiVouDate(Date from , Date to){
        try{

        
        String query = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        query = "  select v.customer_id, count(*), sum(IF(uv.status = 4,1,0 )) converted "
                + " from user_vgood uv , vgood v  "
                + " where v.customer_id in (4,45)  AND  "
                + " uv.vgood_id = v.id and uv.status in (1,4) "
                + " AND uv.creationdate > '"+sdf.format(from)+"' "
                + " AND uv.creationdate < '"+sdf.format(to)+"' "
                + " GROUP BY v.customer_id ";

        Query q = em.createNativeQuery(query);
        List results = q.getResultList();

        for (int i = 0; i < results.size(); i++) {
            Object obj = results.get(i);
            Object[] objectArray = (Object[]) obj;
            try {
                Long customer = ((Long) objectArray[0]).longValue();
                String assigned = ((Long) objectArray[1]).toString();
                String converted = ((BigDecimal) objectArray[2]).toString();
                if(customer==4){
                    adility[0] = assigned;
                    adility[1] = deals.toString();
                }else if(customer==45){
                    vouchacha[0] = assigned;
                    vouchacha[1] = converted;
                }

            }catch (Exception e) { e.printStackTrace();}

        }

        }catch(Exception e){

        }
    }

    private void clearFileLines() {
        this.fileLines = new ArrayList<FileLine>();
        this.adility[0] = "0";
        this.adility[1] = "0";
        this.vouchacha[1] = "0";
        this.vouchacha[1] = "0";
        deals = 0L;
        vgood = 0L;

    }

}
