/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package report.sales;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author davide
 */

public class CountryBeanUserPlayer implements Serializable, Comparable{

    private Date from;
    
    private String name ;
    private String iso ;

    private int submits;
    private int logins;
    
    private int days;
    private int impressions;
    private int clicks;
    
    private int male ;
    private int female ;

    private int units ;
    private int users;
    private int players;
    
    private Map<Integer, Integer> apps = new HashMap<Integer, Integer>();
    
    private float percentage ;
    
    private Map<String, Integer> os = new HashMap<String, Integer>();
    private Map<String, Integer> age = new HashMap<String, Integer>();
    
    private int maus =0;
    private int daus =0;
    
    private int maps =0;
    private int daps =0;
    

   
    public CountryBeanUserPlayer(String iso) {
            this.iso = iso;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Map<String, Integer> getAge() {
        return age;
    }

    public void setAge(Map<String, Integer> age) {
        this.age = age;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getDaps() {
        return daps;
    }

    public void setDaps(int daps) {
        this.daps = daps;
    }

    public int getDaus() {
        return daus;
    }

    public void setDaus(int daus) {
        this.daus = daus;
    }

    public int getFemale() {
        return female;
    }

    public void setFemale(int female) {
        this.female = female;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public int getLogins() {
        return logins;
    }

    public void setLogins(int logins) {
        this.logins = logins;
    }

    public int getMale() {
        return male;
    }

    public void setMale(int male) {
        this.male = male;
    }

    public int getMaps() {
        return maps;
    }

    public void setMaps(int maps) {
        this.maps = maps;
    }

    public int getMaus() {
        return maus;
    }

    public void setMaus(int maus) {
        this.maus = maus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getOs() {
        return os;
    }

    public void setOs(Map<String, Integer> os) {
        this.os = os;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getSubmits() {
        return submits;
    }

    public void setSubmits(int submits) {
        this.submits = submits;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public Map<Integer, Integer> getApps() {
        return apps;
    }

    public void setApps(Map<Integer, Integer> apps) {
        this.apps = apps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryBeanUserPlayer other = (CountryBeanUserPlayer) obj;
        if ((this.iso == null) ? (other.iso != null) : !this.iso.equals(other.iso)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.iso != null ? this.iso.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object o) {

        long otherUnits = ((CountryBeanUserPlayer) o).getUnits();

        if(this.getUnits() > otherUnits)
            return -1;
        else if ( this.getUnits() < otherUnits )
            return 1;
        else
            return 0;
    }

    @Override
    public String toString() {
        return "CountryBeanUserPlayer{" + "name=" + name + ", iso=" + iso + ", days=" + days + ", impressions=" + impressions + ", clicks=" + clicks + ", male=" + male + ", female=" + female + ", units=" + units + ", percentage=" + percentage + ", os=" + os + ", age=" + age + ", maus=" + maus + ", daus=" + daus + ", maps=" + maps + ", daps=" + daps + '}';
    }

    
    


}

