package dev.villagerhelper;
public final class Policy {
 public static boolean allowed(int actual,int requested,boolean villager,boolean trader,boolean alive,double distanceSquared,long tick,long previous) { return actual == requested && villager && trader && alive && distanceSquared <= 64 && tick - previous >= 10; }
}
