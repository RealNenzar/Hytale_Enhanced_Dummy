package de.trainingdummy.core;

public record HitResult(double rawDamage, double effectiveDamage, int hitNumber, double runningTotal,
                        boolean critical, boolean testFinished) {}
