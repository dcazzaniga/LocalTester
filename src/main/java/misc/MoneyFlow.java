/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author davide
 */
public class MoneyFlow implements Serializable{
    
    private long user_vgood_id;
    private long vgood_id;
    private Date date;
    
    private long adv;
    private float adv_quote;
          
    private long res_adv;
    private float res_adv_percentage;
    private float res_adv_quote;
    
    private long publisher;
    private long app;
    private float app_percentage;
    private float publisher_quote;
    
    private long res_pub;
    private float res_pub_percentage;
    private float res_pub_quote;
    
    private long res_aff;
    private float res_aff_percentage;
    private float res_aff_quote;
    
    private float beintoo_percentage;
    private float beintoo_quote;
    
    public MoneyFlow(long vgood_id) {
        this.user_vgood_id = vgood_id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MoneyFlow other = (MoneyFlow) obj;
        if (this.user_vgood_id != other.user_vgood_id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.user_vgood_id ^ (this.user_vgood_id >>> 32));
        return hash;
    }

    void add(long user_vgood_id, long vgood_id, int type, float gain, long customer_id, String name, 
            long app_id,float app_percentage, float res_pub_percentage, float res_adv_percentage, 
            float res_user_percentage, Date date, long backup_app_id) {
        if(customer_id != 0 || name.equals("Beintoo")){
            System.out.println("--- "+customer_id);
        switch (type) {
            case 10:  {
                this.adv_quote = gain;
                this.adv = customer_id;
                this.date = date;
                this.vgood_id = vgood_id;
                if(app==0)
                    app = backup_app_id;
            }
            
            break;
            case 20:    {
                this.beintoo_quote = gain;         
            }
                        
            break;
            case 1:{
                this.publisher_quote = gain;
                this.publisher = customer_id;
                this.app = app_id;                
            }
                        
            break;
            case 3: {
                this.res_pub_quote = gain;
                this.res_pub = customer_id;
            }
                        
            break;
            case 4: {
                this.res_adv_quote = gain;
                this.res_adv = customer_id;
            }
                        
            break;
            case 2: {
                this.res_aff_quote = gain;
                this.res_aff = customer_id;
            }
                        
            break;   
        }
        }
        
    }

    @Override
    public String toString() {
        return "MoneyFlow{" + "user_vgood_id=" + user_vgood_id + ", vgood_id=" + vgood_id + ", date=" + date + ", adv=" + adv + ", adv_quote=" + adv_quote + ", res_adv=" + res_adv + ", res_adv_percentage=" + res_adv_percentage + ", res_adv_quote=" + res_adv_quote + ", publisher=" + publisher + ", app=" + app + ", app_percentage=" + app_percentage + ", publisher_quote=" + publisher_quote + ", res_pub=" + res_pub + ", res_pub_percentage=" + res_pub_percentage + ", res_pub_quote=" + res_pub_quote + ", res_aff=" + res_aff + ", res_aff_percentage=" + res_aff_percentage + ", res_aff_quote=" + res_aff_quote + ", beintoo_percentage=" + beintoo_percentage + ", beintoo_quote=" + beintoo_quote + '}';
    }

    
    public String balance(){
        Float sum = adv_quote+publisher_quote+res_pub_quote+res_adv_quote+res_aff_quote+beintoo_quote;
        return  sum + " =  adv_quote " + adv_quote + " +  res_adv_quote " + res_adv_quote + " publisher_quote " + publisher_quote + " + res_pub_quote " + res_pub_quote + " + res_aff_quote " + res_aff_quote + " +  beintoo_quote " + beintoo_quote ;
    
    }

    public long getAdv() {
        return adv;
    }

    public void setAdv(long adv) {
        this.adv = adv;
    }

    public float getAdv_quote() {
        return adv_quote;
    }

    public void setAdv_quote(float adv_quote) {
        this.adv_quote = adv_quote;
    }

    public long getApp() {
        return app;
    }

    public void setApp(long app) {
        this.app = app;
    }

    public float getApp_percentage() {
        return app_percentage;
    }

    public void setApp_percentage(float app_percentage) {
        this.app_percentage = app_percentage;
    }

    public float getBeintoo_percentage() {
        return beintoo_percentage;
    }

    public void setBeintoo_percentage(float beintoo_percentage) {
        this.beintoo_percentage = beintoo_percentage;
    }

    public float getBeintoo_quote() {
        return beintoo_quote;
    }

    public void setBeintoo_quote(float beintoo_quote) {
        this.beintoo_quote = beintoo_quote;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getPublisher() {
        return publisher;
    }

    public void setPublisher(long publisher) {
        this.publisher = publisher;
    }

    public float getPublisher_quote() {
        return publisher_quote;
    }

    public void setPublisher_quote(float publisher_quote) {
        this.publisher_quote = publisher_quote;
    }

    public long getRes_adv() {
        return res_adv;
    }

    public void setRes_adv(long res_adv) {
        this.res_adv = res_adv;
    }

    public float getRes_adv_percentage() {
        return res_adv_percentage;
    }

    public void setRes_adv_percentage(float res_adv_percentage) {
        this.res_adv_percentage = res_adv_percentage;
    }

    public float getRes_adv_quote() {
        return res_adv_quote;
    }

    public void setRes_adv_quote(float res_adv_quote) {
        this.res_adv_quote = res_adv_quote;
    }

    public long getRes_aff() {
        return res_aff;
    }

    public void setRes_aff(long res_aff) {
        this.res_aff = res_aff;
    }

    public float getRes_aff_percentage() {
        return res_aff_percentage;
    }

    public void setRes_aff_percentage(float res_aff_percentage) {
        this.res_aff_percentage = res_aff_percentage;
    }

    public float getRes_aff_quote() {
        return res_aff_quote;
    }

    public void setRes_aff_quote(float res_aff_quote) {
        this.res_aff_quote = res_aff_quote;
    }

    public long getRes_pub() {
        return res_pub;
    }

    public void setRes_pub(long res_pub) {
        this.res_pub = res_pub;
    }

    public float getRes_pub_percentage() {
        return res_pub_percentage;
    }

    public void setRes_pub_percentage(float res_pub_percentage) {
        this.res_pub_percentage = res_pub_percentage;
    }

    public float getRes_pub_quote() {
        return res_pub_quote;
    }

    public void setRes_pub_quote(float res_pub_quote) {
        this.res_pub_quote = res_pub_quote;
    }

    public long getUser_vgood_id() {
        return user_vgood_id;
    }

    public void setUser_vgood_id(long user_vgood_id) {
        this.user_vgood_id = user_vgood_id;
    }

    public long getVgood_id() {
        return vgood_id;
    }

    public void setVgood_id(long vgood_id) {
        this.vgood_id = vgood_id;
    }
    
    
    
    
    
    
    
    
}
