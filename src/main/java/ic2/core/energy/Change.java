// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import net.minecraft.util.EnumFacing;

class Change
{
    Node node;
    final EnumFacing dir;
    private double amount;
    private double voltage;
    
    Change(final Node node, final EnumFacing dir, final double amount, final double voltage) {
        this.node = node;
        this.dir = dir;
        this.setAmount(amount);
        this.setVoltage(voltage);
    }
    
    @Override
    public String toString() {
        return this.node + "@" + this.dir + " " + this.amount + " EU / " + this.voltage + " V";
    }
    
    double getAmount() {
        return this.amount;
    }
    
    void setAmount(double amount) {
        final double intAmount = Math.rint(amount);
        if (Math.abs(amount - intAmount) < 0.001) {
            amount = intAmount;
        }
        assert !Double.isInfinite(amount) && !Double.isNaN(amount);
        this.amount = amount;
    }
    
    double getVoltage() {
        return this.voltage;
    }
    
    private void setVoltage(double voltage) {
        final double intVoltage = Math.rint(this.amount);
        if (Math.abs(voltage - intVoltage) < 0.001) {
            voltage = intVoltage;
        }
        assert !Double.isInfinite(voltage) && !Double.isNaN(voltage);
        this.voltage = voltage;
    }
}
