package com.overtime.aerothrusters.content.thruster;

public enum ThrusterTier {
    BASIC(2000.0, 20.0, 4000, 2),
    ADVANCED(8000.0, 35.0, 8000, 4),
    SUPERIOR(32000.0, 50.0, 16000, 8);

    public final double maxThrust;
    public final double airflow;
    public final int tankCapacity;
    public final int burnRateMax;

    ThrusterTier(double maxThrust, double airflow, int tankCapacity, int burnRateMax) {
        this.maxThrust = maxThrust;
        this.airflow = airflow;
        this.tankCapacity = tankCapacity;
        this.burnRateMax = burnRateMax;
    }
}
