/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.publisher;

/**
 *
 * @author davide
 */
public class AppString {
    
    private Integer id ;
    private String appString;
    private String infos;
    private String lastWeekInfos;
    
    private String imps;
    private String lastWeekImps;
    
    private String achievements;
    private String lastWeekAchievements;
    
    private String giveBedollars;
    private String lastWeekGiveBedollars;
 
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public String getLastWeekInfos() {
        return lastWeekInfos;
    }

    public void setLastWeekInfos(String lastWeekInfos) {
        this.lastWeekInfos = lastWeekInfos;
    }

    public String getAppString() {
        return appString;
    }

    public void setAppString(String appString) {
        this.appString = appString;
    }

    public String getImps() {
        return imps;
    }

    public void setImps(String imps) {
        this.imps = imps;
    }

    public String getLastWeekImps() {
        return lastWeekImps;
    }

    public void setLastWeekImps(String lastWeekImps) {
        this.lastWeekImps = lastWeekImps;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getLastWeekAchievements() {
        return lastWeekAchievements;
    }

    public void setLastWeekAchievements(String lastWeekAchievements) {
        this.lastWeekAchievements = lastWeekAchievements;
    }

    public String getGiveBedollars() {
        return giveBedollars;
    }

    public void setGiveBedollars(String giveBedollars) {
        this.giveBedollars = giveBedollars;
    }

    public String getLastWeekGiveBedollars() {
        return lastWeekGiveBedollars;
    }

    public void setLastWeekGiveBedollars(String lastWeekGiveBedollars) {
        this.lastWeekGiveBedollars = lastWeekGiveBedollars;
    }

    public AppString(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppString other = (AppString) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    
}
