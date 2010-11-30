

public class Click extends Object {
	private int x;
	private int y;
	private int z;
	private String owner;
	public Click(int x,int y,int z,String owner)
	{
		this.x=x;
		this.y=y;
		this.z=z;
		this.owner=owner;
	}
	

	@Override
	public boolean equals(Object obj) {
		Click c1=(Click)obj;
		if((c1.x==x)&&(c1.y==y)&&(c1.z==z)&&(c1.owner==owner))
			return true;
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return x^(y<<8)^(z<<16)^(owner.hashCode());
	}
}
