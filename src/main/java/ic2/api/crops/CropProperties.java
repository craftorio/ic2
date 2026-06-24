package ic2.api.crops;

public record CropProperties(int tier, int chemistry, int consumable, int defensive, int colorful, int weed)
{


	public int[] getAllProperties()
	{
		return new int[] { this.chemistry(), this.consumable(), this.defensive(), this.colorful(), this.weed() };
	}
}
