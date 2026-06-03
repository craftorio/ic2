package ic2.core.energy;

import net.minecraft.util.EnumFacing;

class Change {
  Node node;
  
  final EnumFacing dir;
  
  private double amount;
  
  private double voltage;
  
  Change(Node node, EnumFacing dir, double amount, double voltage) {
    this.node = node;
    this.dir = dir;
    setAmount(amount);
    setVoltage(voltage);
  }
  
  public String toString() {
    return this.node + "@" + this.dir + " " + this.amount + " EU / " + this.voltage + " V";
  }
  
  double getAmount() {
    return this.amount;
  }
  
  void setAmount(double amount) {
    double intAmount = Math.rint(amount);
    if (Math.abs(amount - intAmount) < 0.001D)
      amount = intAmount; 
    assert !Double.isInfinite(amount) && !Double.isNaN(amount);
    this.amount = amount;
  }
  
  double getVoltage() {
    return this.voltage;
  }
  
  private void setVoltage(double voltage) {
    double intVoltage = Math.rint(this.amount);
    if (Math.abs(voltage - intVoltage) < 0.001D)
      voltage = intVoltage; 
    assert !Double.isInfinite(voltage) && !Double.isNaN(voltage);
    this.voltage = voltage;
  }
}
