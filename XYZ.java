public class XYZ {
	public Integer x;
	public Integer y;
	public Integer z;
	public XYZ(Integer x,Integer y,Integer z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	@Override
	public boolean equals(Object obj) {
		XYZ c1=(XYZ)obj;
        if(c1.x.equals(x) && c1.y.equals(y) && c1.z.equals(z))
			return true;
		return false;
	}
	@Override
	public int hashCode()
	{
		return x^(y<<8)^(z<<16);
	}

}
