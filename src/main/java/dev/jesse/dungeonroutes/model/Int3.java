package dev.jesse.dungeonroutes.model;

public record Int3(int x, int y, int z) {
    public long squaredDistance(double ox, double oy, double oz) {
        double dx = x + 0.5 - ox;
        double dy = y + 0.5 - oy;
        double dz = z + 0.5 - oz;
        return (long) (dx * dx + dy * dy + dz * dz);
    }
}
