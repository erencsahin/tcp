package com.erencsahin.tcp;

import java.util.Random;

public class RateGenerator {
    private final Random random = new Random();
    private double currentAskValue;
    private double currentBidValue;

    // Büyük sıçrama olasılığı ve büyüklüğü
    private final double jumpProbability = 0.1;  // %10
    private final double minJumpFactor  = 0.01;  // %1
    private final double maxJumpFactor  = 0.03;  // %3

    // Küçük dalgalanma için maksimum %0.5 değişim
    private final double smallChangeFactor = 0.005;

    public RateGenerator(double initialAskValue, double initialBidValue) {
        this.currentAskValue = initialAskValue;
        this.currentBidValue = initialBidValue;
    }

    public double getNextAskValue() {
        if (random.nextDouble() < jumpProbability) {
            // Büyük sıçrama: %1–%3 arasında
            double jumpPct = minJumpFactor + (maxJumpFactor - minJumpFactor) * random.nextDouble();
            currentAskValue *= (random.nextBoolean() ? 1 + jumpPct : 1 - jumpPct);
        } else {
            // Küçük oransal dalgalanma: ±%0.5
            double deltaPct = (random.nextDouble() * 2 - 1) * smallChangeFactor;
            currentAskValue *= (1 + deltaPct);
        }
        // Negatife düşmesin
        currentAskValue = Math.max(currentAskValue, 0.0001);
        return currentAskValue;
    }

    public double getNextBidValue() {
        if (random.nextDouble() < jumpProbability) {
            double jumpPct = minJumpFactor + (maxJumpFactor - minJumpFactor) * random.nextDouble();
            currentBidValue *= (random.nextBoolean() ? 1 + jumpPct : 1 - jumpPct);
        } else {
            double deltaPct = (random.nextDouble() * 2 - 1) * smallChangeFactor;
            currentBidValue *= (1 + deltaPct);
        }
        currentBidValue = Math.max(currentBidValue, 0.0001);
        return currentBidValue;
    }
}

