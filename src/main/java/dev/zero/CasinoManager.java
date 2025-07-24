// CasinoManager.java
package dev.zero;

public class CasinoManager {
    private static String owner = null;

    /** Define o proprietário do cassino */
    public static void setOwner(String ownerName) {
        owner = ownerName;
    }

    /** Retorna o proprietário ou "Nenhum" se não houver */
    public static String getOwner() {
        return (owner != null && !owner.isBlank()) ? owner : "Nenhum";
    }
}
