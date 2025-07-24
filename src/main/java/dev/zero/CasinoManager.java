
package dev.zero;

public class CasinoManager {
    private static String owner = null;


    public static void setOwner(String ownerName) {
        owner = ownerName;
    }


    public static String getOwner() {
        return (owner != null && !owner.isBlank()) ? owner : "Nenhum";
    }
}
