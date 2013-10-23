/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package misc;


import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;


/**
 *
 * @author davide
 */
public class Tester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println(
        UUID.randomUUID().toString());
        System.out.println(
        UUID.randomUUID().toString());
        
        for(int i = 0; i<5; i++){
            System.out.println(""+RandomStringUtils.randomAlphanumeric(40));
        }
        
        
        
        
        
//        Lca lca = new Lca();
//
//        int[] votes = {4,5,5};
//
//        System.out.println(lca.mean(votes));
//        System.out.println(lca.reankMean(votes));
//
//        System.out.println("liquidity ceil "+lca.getLiquidityCeil());
//        lca.setLiquidityCeil(1500f);
//        lca.setLiquidityFloor(1000);
//        System.out.println(lca.mean(60,1000));
//        System.out.println(lca.reankMean(60,1000));
//
//        
//        Logger.log("CIAO", LogContextEnum.WEBSITE_ADMIN, LogLevelEnum.ERROR);
//        
//        for(int i=0;i<10;i++){
//            System.out.println(""+System.nanoTime());
//            
//        }
//        
        
    }

}
