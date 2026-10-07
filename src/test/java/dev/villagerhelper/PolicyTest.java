package dev.villagerhelper;
public final class PolicyTest {
 public static void main(String[] args) {
  if (Policy.allowed(7,8,true,true,true,4,100,0)) throw new AssertionError("forged menu accepted");
  if (Policy.allowed(7,7,true,true,true,65,100,0)) throw new AssertionError("distance accepted");
  if (Policy.allowed(7,7,true,false,true,4,100,0)) throw new AssertionError("wrong trader accepted");
  if (Policy.allowed(7,7,true,true,true,4,100,95)) throw new AssertionError("spam accepted");
  if (!Policy.allowed(7,7,true,true,true,4,100,0)) throw new AssertionError("valid rejected");
  System.out.println("PASS request menu/distance/trader/cooldown policy");
 }
}
