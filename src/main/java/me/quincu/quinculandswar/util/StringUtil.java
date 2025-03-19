package me.quincu.quinculandswar.util;

import org.bukkit.ChatColor;

public class StringUtil {

    public static String color(String old){
        return ChatColor.translateAlternateColorCodes('&', old);
    }

}
