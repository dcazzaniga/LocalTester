/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.publisher;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author davide
 */
public class PublisherString {
    
    
    private Integer id ;
    private Integer segment;
    private String infos = "";
    
    private List<AppString> apps = new ArrayList<AppString>();
    
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

    public Integer getSegment() {
        return segment;
    }

    public void setSegment(Integer segment) {
        this.segment = segment;
    }

    public List<AppString> getApps() {
        return apps;
    }

    public void setApps(List<AppString> apps) {
        this.apps = apps;
    }

    public PublisherString(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final PublisherString other = (PublisherString) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    public void addApp(AppString as){
        
        int idx = apps.indexOf(as);
                
        if(idx>-1){
            
            System.out.println(" !!!!!!!!!!!!!! ");

        }else{
            apps.add(as);
            
        }
        
        
    }
    
    
}
