package ic2.core.energy.leg;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.energy.grid.Node;
import ic2.core.energy.grid.NodeLink;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

class EnergyPath
{
	final Node source;
	final Node target;
	final List<Node> conductors;
	final double loss;
	final EnumFacing targetDirection;
	final double minEffectEnergy;
	final double minInsulationEnergyAbsorption;
	final double minInsulationBreakdownEnergy;
	final double minConductorBreakdownEnergy;
	final int minX;
	final int minY;
	final int minZ;
	final int maxX;
	final int maxY;
	final int maxZ;
	int lastCalcId = -1;
	double energySupplied;
	double maxPacketConducted;

	EnergyPath(Node source, Node target, List<Node> conductors, double loss)
	{
		this.source = source;
		this.target = target;
		this.conductors = conductors;
		this.loss = loss;
		NodeLink lastLink = conductors.isEmpty() ? source.getLinkTo(target) : target.getLinkTo(conductors.get(conductors.size() - 1));
		this.targetDirection = lastLink.getDirFrom(target);
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		double minInsulationEnergyAbsorption = Double.POSITIVE_INFINITY;
		double minInsulationBreakdownEnergy = Double.POSITIVE_INFINITY;
		double minConductorBreakdownEnergy = Double.POSITIVE_INFINITY;

		for (Node node : conductors)
		{
			IEnergyConductor conductor = (IEnergyConductor) node.getTile().getMainTile();
			minInsulationEnergyAbsorption = Math.min(minInsulationEnergyAbsorption, conductor.getInsulationEnergyAbsorption());
			minInsulationBreakdownEnergy = Math.min(minInsulationBreakdownEnergy, conductor.getInsulationBreakdownEnergy());
			minConductorBreakdownEnergy = Math.min(minConductorBreakdownEnergy, conductor.getConductorBreakdownEnergy());

			for (IEnergyTile tile : node.getTile().getSubTiles())
			{
				BlockPos pos = EnergyNet.instance.getPos(tile);
				minX = Math.min(minX, pos.getX());
				minY = Math.min(minY, pos.getY());
				minZ = Math.min(minZ, pos.getZ());
				maxX = Math.max(maxX, pos.getX());
				maxY = Math.max(maxY, pos.getY());
				maxZ = Math.max(maxZ, pos.getZ());
			}
		}

		this.minEffectEnergy = Math.min(Math.min(minInsulationEnergyAbsorption, minInsulationBreakdownEnergy), minConductorBreakdownEnergy);
		this.minInsulationEnergyAbsorption = minInsulationEnergyAbsorption;
		this.minInsulationBreakdownEnergy = minInsulationBreakdownEnergy;
		this.minConductorBreakdownEnergy = minConductorBreakdownEnergy;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
}
