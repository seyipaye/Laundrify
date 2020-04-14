package ng.com.laundrify.laundrify.utils;

import ng.com.laundrify.laundrify.R;

public class Utils {

    public static int tryGetingResource(String priceKey) {
        if (priceKey.equalsIgnoreCase("Bedset")) {
            return R.drawable.bedset;
        }
        if (priceKey.equalsIgnoreCase("Bedspread")) {
            return R.drawable.bedspread;
        }
        if (priceKey.equalsIgnoreCase("Blanket")) {
            return R.drawable.blanket;
        }
        if (priceKey.equalsIgnoreCase("Covercloth")) {
            return R.drawable.covercloth;
        }
        if (priceKey.equalsIgnoreCase("Duvet")) {
            return R.drawable.duvet;
        }
        if (priceKey.equalsIgnoreCase("Jeans")) {
            return R.drawable.jeans;
        }
        if (priceKey.equalsIgnoreCase("Short")) {
            return R.drawable.shorts;
        }
        if (priceKey.equalsIgnoreCase("Skirt")) {
            return R.drawable.skirts;
        }
        if (priceKey.equalsIgnoreCase("Trousers")) {
            return R.drawable.trousers;
        }
        if (priceKey.equalsIgnoreCase("Agbada")) {
            return R.drawable.agbada;
        }
        if (priceKey.equalsIgnoreCase("AgbadaOnly")) {
            return R.drawable.agbadaonly;
        }
        if (priceKey.equalsIgnoreCase("Buba")) {
            return R.drawable.buba;
        }
        if (priceKey.equalsIgnoreCase("Gele")) {
            return R.drawable.gele;
        }
        if (priceKey.equalsIgnoreCase("Iro")) {
            return R.drawable.iro;
        }
        if (priceKey.equalsIgnoreCase("Jackets")) {
            return R.drawable.jackets;
        }
        if (priceKey.equalsIgnoreCase("Blouse")) {
            return R.drawable.blouse;
        }
        if (priceKey.equalsIgnoreCase("Dress")) {
            return R.drawable.dress;
        }
        if (priceKey.equalsIgnoreCase("Shirt")) {
            return R.drawable.shirt;
        }
        if (priceKey.equalsIgnoreCase("Suit")) {
            return R.drawable.suit;
        }
        if (priceKey.equalsIgnoreCase("T-shirt")) {
            return R.drawable.tshirt;
        }

        return 0;
    }

    public static double chgD(Object obj) {
        if (obj != null) {
            try {
                return Double.valueOf(String.valueOf(obj));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            return -1;
        }
    }

    public static String chgS(Object obj) {
        if (obj != null) {
            return String.valueOf(obj);
        } else {
            return null;
        }
    }

    public static boolean chgB(Object obj) {
        try {
            return Boolean.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int chgI(Object obj) {
        if (obj != null) {
            try {
                return Integer.parseInt((obj).toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            return -1;
        }
    }
}
