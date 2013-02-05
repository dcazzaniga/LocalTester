package locator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author davide
 */
public class WorldMap implements Serializable{

    private static WorldMap instance;
    private Map<String , Field> fields = new HashMap<String, Field>(324);
    private String fileName = "CityCountryLatLon.csv";
    private String isoCountryNameFile = "ISO3166_CountryCodes.csv";
    private Map<String, String> isoMap = new HashMap<String, String>(252);

    public static synchronized WorldMap getInstance() {
        if (instance == null) { instance = new WorldMap(); }
        return instance;
    }

    public WorldMap(){
        System.out.println("Recomputing world map!");
    try{
                FileInputStream fis = null;
                InputStreamReader isr = null;
                BufferedReader buf = null;
                String line = "";
                try {
                        fis = new FileInputStream("conf/"+ fileName);
                        isr = new InputStreamReader(fis);
                        buf = new BufferedReader(isr);
                        while ( (line = buf.readLine()) != null ) {
                            Point c = new Point(line);
                            String field = c.getField();
                            if(fields.containsKey(field)){
                                fields.get(field).addSquare(c);
                            }else{
                                fields.put(field, new Field(c));
                            }  
                        }

                }catch(Exception e){
                    System.out.println(":: "+line);
                    e.printStackTrace();
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
        }catch(Exception e){
            e.printStackTrace();
        }

    try{
                FileInputStream fis = null;
                InputStreamReader isr = null;
                BufferedReader buf = null;
                String line = "";
                try {
                        fis = new FileInputStream("conf/"+ isoCountryNameFile);
                        isr = new InputStreamReader(fis);
                        buf = new BufferedReader(isr);
                        while ( (line = buf.readLine()) != null ) {
                            String[] s = line.split(";");
                            isoMap.put(s[0], s[1]);
                        }

                }catch(Exception e){
                    System.out.println(":: "+line);
                    e.printStackTrace();
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
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public String getCountry(float lat, float lon){

        String[] locatore = getLocatore(lat, lon);
        String field = locatore[0];
        String square = locatore[1];
        String nearest = "NotFound";

        if(fields.containsKey(field)){

            Field f = fields.get(field);
            if(f.getCountries().size() == 1){
                return f.getCountries().iterator().next();
            }else{
                if(f.getSquares().containsKey(square)){
                    Square s = f.getSquares().get(square);
                    if(s.getCountries().size() == 1){
                        return s.getCountries().iterator().next();
                    }else{
                        nearest = s.getNearest(lat, lon);
                    }
                }else{
                    nearest = getNearestNeighbor(field, square, lat, lon);
                }
            }
        }else{
            //System.out.println("NO FIELD :: LAT , LON = ["+(lat-90)+" ,"+( lon-180)+" ]");
        }
//        if(nearest.equals("[NotFound]")){
//            System.out.println(":: LAT , LON = ["+(lat-90)+" ,"+( lon-180)+" ]");
//        }
        return nearest;
    }

    public String getCity(float lat, float lon, String country){

        String[] locatore = getLocatore(lat, lon);
        String field = locatore[0];
        String square = locatore[1];
        String nearest = "NotFound";

        if(fields.containsKey(field)){

            Field f = fields.get(field);
            if(f.getSquares().containsKey(square)){
                    Square s = f.getSquares().get(square);
                    
                    if(s.getNearest(lat, lon).equals(country)){
                        nearest = s.getNearestCity(lat, lon);
                       
                    } 
            }else{
                    nearest = getNearestNeighbor(field, square, lat, lon);
            }
        }

        return nearest;
    }

    public static String[] getLocatore(Float lat, Float lon){

        String[] result = new String[2];
        result[0] =  (lon.intValue()/20) + ":" + (lat.intValue()/10) ;
        result[1] = ( (lon.intValue() - lon.intValue()/20 * 20)/2 )  + ":" + (lat.intValue() - lat.intValue()/10 * 10) ;

        return result;
    }

    public static List<String> getNearbySquare(String field, String square){

        List<String> result = new ArrayList<String>();
        String[] f = field.split(":");
        int fx = Integer.parseInt(f[0]);
        int fy = Integer.parseInt(f[1]);
        String[] s = field.split(":");
        int sx = Integer.parseInt(s[0]);
        int sy = Integer.parseInt(s[1]);

        // NORTH
        if(sy == 9 && fy < 17 ){
            result.add( fx + ":" + ((18 + (fy+1))%18) + "#" + sx + ":0" );
        }else{
            result.add( fx + ":" + fy + "#" + sx + "" + (sy+1));
        }
        // SUD
        if(sy == 0 && fy > 0 ){
            result.add( fx + ":" + ((18 + (fy-1))%18) + "#" + sx + ":9" );
        }else{
            result.add( fx + ":" + fy + "#" + sx + ":" + (sy-1));
        }
        // WEST
        if(sx == 0 ){
            result.add( ((18 + (fx-1))%18) + ":" + fy + "#9:" + sy );
        }else{
            result.add( fx + ":" + fy + "#" + (sx-1) + ":" + sy );
        }
        // EST
        if(sx == 9 ){
            result.add( ((18 + (fx+1))%18) + ":" + fy + "#0:" + sy );
        }else{
            result.add( fx + ":" + fy + "#" + (sx+1) + ":" + sy );
        }

        return result;
    }

    public String getNearestNeighbor(String field, String square , Float lat, Float lon){

        String nearest = "NotFound";
        List<String> nearbySquares = getNearbySquare(field, square);
        List<Point> list = new ArrayList<Point>();

        for(String s: nearbySquares){
            try{
                list.addAll(getSquare(s).getPoints());
            }catch(Exception e){

            }
        }
        nearest = getNearest(list, lat, lon);
        return nearest;
    }

    public Square getSquare(String locatore){
        String field = locatore.substring(0,2);
        String square = locatore.substring(2,4);
        Square s = new Square();
        try{
            s = fields.get(field).getSquares().get(square);
        }catch( Exception e){
        }
        return s;
    }

    public String iso2countryName(String iso){
        return isoMap.get(iso);
    }

    public static String getNearest(List<Point> list, float lat, float lon) {

        String nearest = "NotFound";
        double distance = 0;
        if(!list.isEmpty()){
            nearest = list.get(0).getCityCountry();
            distance = list.get(0).getDistance(lat, lon);
        }
        for(Point p: list){
            double d = p.getDistance(lat, lon);
            if(d<distance){
                distance = d;
                nearest = p.getCityCountry();
            }
        }
        return nearest;

    }

    class Field implements Serializable {

        private String field;
        private Set<String> countries = new TreeSet<String>();
        private String[] boundaryField = new String[8] ;

        private Map<String , Square> squares = new HashMap<String, Square>(100);

        public Field(Point c){
            this.field = c.getField();
            this.countries.add(c.getCityCountry());
            squares.put(c.getSquare(), new Square(c));
        }

        void addSquare(Point c) {
            String key = c.getSquare();
            this.countries.add(c.getCityCountry());
            if(squares.containsKey(key)){
                squares.get(key).addCity(c);
            }else{
                squares.put(c.getSquare(), new Square(c));
            }
        }

        public String[] getBoundaryField() {
            return boundaryField;
        }

        public void setBoundaryField(String[] boundaryField) {
            this.boundaryField = boundaryField;
        }

        public Set<String> getCountries() {
            return countries;
        }

        public void setCountries(Set<String> countries) {
            this.countries = countries;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Map<String, Square> getSquares() {
            return squares;
        }

        public void setSquares(Map<String, Square> squares) {
            this.squares = squares;
        }

    }

    class Square implements Serializable{

        private String square;
        private String[] boundarySquare = new String[8];
        private Set<String> countries = new TreeSet<String>();
        private List<Point> points = new ArrayList<Point>();

        public Square(){

        }

        public Square(Point c){
            this.square = c.getSquare();
            this.countries.add(c.getCityCountry());
            this.points.add(c);
        }

        public void addCity(Point c){
            this.countries.add(c.getCityCountry());
            this.points.add(c);
        }

        public String[] getBoundarySquare() {
            return boundarySquare;
        }

        public void setBoundarySquare(String[] boundarySquare) {
            this.boundarySquare = boundarySquare;
        }

        public Set<String> getCountries() {
            return countries;
        }

        public void setCountries(Set<String> countries) {
            this.countries = countries;
        }

        public List<Point> getPoints() {
            return points;
        }

        public void setPoints(List<Point> list) {
            this.points = list;
        }

        public String getSquare() {
            return square;
        }

        public void setSquare(String square) {
            this.square = square;
        }

        public String getNearest(float lat, float lon) {

            String nearest = "NotFound";
            double distance = 0;
            if(!points.isEmpty()){
                nearest = points.get(0).getCityCountry();
                distance = points.get(0).getDistance(lat, lon);
            }
            for(Point p: points){
                double d = p.getDistance(lat, lon);
                if(d<distance){
                    distance = d;
                    nearest = p.getCityCountry();
                }
            }
            return nearest;

        }

        public String getNearestCity(float lat, float lon) {

            String nearest = "NotFound";
            double distance = 0;
            if(!points.isEmpty()){
                nearest = points.get(0).getCityName();
                distance = points.get(0).getDistance(lat, lon);
            }
            for(Point p: points){
                double d = p.getDistance(lat, lon);
                if(d<distance){
                    distance = d;
                    nearest = p.getCityName();
                }
            }
            return nearest;

        }

    }

    class Point implements Serializable{

        private String cityName;
        private String cityCountry;
        private long population;

        private Float latitude;
        private Float longitude;

        private String field;
        private String square;

        public Point(String s){

            String[] line = s.split(";");
            //System.out.println("line.length  :"+line.length);
            cityName = line[0];
            cityCountry = line[2];

            population = Long.parseLong(line[3]);

            String[] coordinate = line[1].split(",");
            latitude = Float.parseFloat(coordinate[0]) + 90;
            longitude = Float.parseFloat(coordinate[1]) + 180;

            field = longitude.intValue()/20 + ":" + latitude.intValue()/10;
            square = (longitude.intValue() - longitude.intValue()/20 * 20)/2 + ":" + (latitude.intValue() - latitude.intValue()/10 * 10);

        }

        public String getLocatore(){
            return field+"#"+square;
        }

        public String getCityCountry() {
            return cityCountry;
        }

        public void setCityCountry(String cityCountry) {
            this.cityCountry = cityCountry;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public Float getLatitude() {
            return latitude;
        }

        public void setLatitude(Float latitude) {
            this.latitude = latitude;
        }

        public Float getLongitude() {
            return longitude;
        }

        public void setLongitude(Float longitude) {
            this.longitude = longitude;
        }

        public long getPopulation() {
            return population;
        }

        public void setPopulation(long population) {
            this.population = population;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getSquare() {
            return square;
        }

        public void setSquare(String square) {
            this.square = square;
        }

        public double getDistance(Float lat , Float lon){

            double d = 0;
            if( Math.abs(this.longitude - lon) > 180  ){
                d = Math.sqrt(  Math.pow(  (this.longitude - lon - 180) ,2) + Math.pow((this.latitude - lat),2)    );
            }else{
                d = Math.sqrt(  Math.pow(  (this.longitude - lon ) ,2) + Math.pow((this.latitude - lat),2)    );
            }
            return d;

        }

    }

}
