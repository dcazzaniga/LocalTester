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

public class OsPlayerBean implements Serializable, Comparable{

   
    private int id;
    private String name ;
    private String app_type ;

    private int dailySubmits;
    
    private int players = 0;
    private int playersForSplit = 0;
    private int users = 0;
    
    private int maus =0;
    private int daus =0;
    private int daps =0;
    
    public OsPlayerBean(int id) {
            this.id = id;
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

    public String getApp_type() {
        return app_type;
    }

    public void setApp_type(String app_type) {
        this.app_type = app_type;
    }

    public int getDailySubmits() {
        return dailySubmits;
    }

    public void setDailySubmits(int dailySubmits) {
        this.dailySubmits = dailySubmits;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getPlayersForSplit() {
        return playersForSplit;
    }

    public void setPlayersForSplit(int playersForSplit) {
        this.playersForSplit = playersForSplit;
    }

    public int compareTo(Object o) {

        long otherUnits = ((OsPlayerBean) o).getDaps();

        if(this.getDaps() > otherUnits)
            return -1;
        else if ( this.getDaps() < otherUnits )
            return 1;
        else
            return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OsPlayerBean other = (OsPlayerBean) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        return hash;
    }

}

