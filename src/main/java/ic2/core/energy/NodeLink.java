package ic2.core.energy;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

class NodeLink
{
	Node nodeA;
	Node nodeB;
	Direction dirFromA;
	Direction dirFromB;
	double loss;
	List<Node> skippedNodes = new ArrayList<>();

	NodeLink(Node nodeA1, Node nodeB1, double loss1)
	{
		this(nodeA1, nodeB1, loss1, null, null);
		this.calculateDirections();
	}

	NodeLink(NodeLink link)
	{
		this(link.nodeA, link.nodeB, link.loss, link.dirFromA, link.dirFromB);
		this.skippedNodes.addAll(link.skippedNodes);
	}

	private NodeLink(Node nodeA1, Node nodeB1, double loss1, Direction dirFromA, Direction dirFromB)
	{
		assert nodeA1 != nodeB1;
		this.nodeA = nodeA1;
		this.nodeB = nodeB1;
		this.loss = loss1;
		this.dirFromA = dirFromA;
		this.dirFromB = dirFromB;
	}

	Node getNeighbor(Node node)
	{
		return this.nodeA == node ? this.nodeB : this.nodeA;
	}

	Node getNeighbor(int uid)
	{
		return this.nodeA.uid == uid ? this.nodeB : this.nodeA;
	}

	void replaceNode(Node oldNode, Node newNode)
	{
		if (this.nodeA == oldNode)
		{
			this.nodeA = newNode;
		} else
		{
			if (this.nodeB != oldNode)
			{
				throw new IllegalArgumentException("Node " + oldNode + " isn't in " + this + ".");
			}

			this.nodeB = newNode;
		}
	}

	Direction getDirFrom(Node node)
	{
		if (this.nodeA == node)
		{
			return this.dirFromA;
		} else
		{
			return this.nodeB == node ? this.dirFromB : null;
		}
	}

	void updateCurrent()
	{
		assert !Double.isNaN(this.nodeA.getVoltage());
		assert !Double.isNaN(this.nodeB.getVoltage());
		double currentAB = (this.nodeA.getVoltage() - this.nodeB.getVoltage()) / this.loss;
		this.nodeA.addCurrent(-currentAB);
		this.nodeB.addCurrent(currentAB);
	}

	@Override
	public String toString()
	{
		return "NodeLink:" + this.nodeA + "@" + this.dirFromA + "->" + this.nodeB + "@" + this.dirFromB;
	}

	private void calculateDirections()
	{
		for (IEnergyTile posA : this.nodeA.tile.subTiles)
		{
			for (IEnergyTile posB : this.nodeB.tile.subTiles)
			{
				BlockPos delta = EnergyNet.instance.getPos(posA).subtract(EnergyNet.instance.getPos(posB));

				for (Direction dir : Util.ALL_DIRS)
				{
					if (dir.getStepX() == delta.getX() && dir.getStepY() == delta.getY() && dir.getStepZ() == delta.getZ())
					{
						this.dirFromA = dir;
						this.dirFromB = dir.getOpposite();
						return;
					}
				}
			}
		}

		assert false;
		this.dirFromA = null;
		this.dirFromB = null;
	}
}
