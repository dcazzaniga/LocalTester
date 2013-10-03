/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package misc;

/**
 *
 * @author davide
 */
// Liquidity compensation algoritm

public class Lca {

    public static void main(String[] args){
        
        
        
    }
    
    
    float AdjustmentFactor = 0.05f;
    float liquidityWeight ;
    float liquidityFloor = 10f;
    float liquidityCeil = 60f;

    public Lca() {

    }

    public float getAdjustmentFactor() {
        return AdjustmentFactor;
    }

    public void setAdjustmentFactor(float AdjustmentFactor) {
        this.AdjustmentFactor = AdjustmentFactor;
    }

    public float getLiquidityCeil() {
        return liquidityCeil;
    }

    public void setLiquidityCeil(float liquidityCeil) {
        this.liquidityCeil = liquidityCeil;
    }

    public float getLiquidityFloor() {
        return liquidityFloor;
    }

    public void setLiquidityFloor(float liquidityFloor) {
        this.liquidityFloor = liquidityFloor;
    }

    public float getLiquidityWeight() {
        return liquidityWeight;
    }

    public void setLiquidityWeight(int votes) {
        liquidityWeight = Math.min(
                        Math.max(
                            (votes - liquidityFloor)/liquidityCeil,
                            0),
                        1.0f) * 2 ;
    }



    public float mean(int[] votes){

        float result = 0f;
        for (int i : votes) {
            result += i;
        }
        result = result/votes.length;

        return result;
    }
    
    public float reankMean(int[] votes){
        setLiquidityWeight(votes.length);
        
        float result = 0f;
        for (int i : votes) {
            result += i;
        }
        result = result/votes.length - AdjustmentFactor + liquidityWeight * AdjustmentFactor ;

        return result;
    }

    public float mean(int clicks , int assigned){

        return (float)clicks/assigned;
    }

    public float reankMean(int clicks , int assigned ){

        setLiquidityWeight(assigned);

        float result = clicks;
        
        result = result/assigned - AdjustmentFactor + liquidityWeight * AdjustmentFactor ;

        return result;
    }



}
