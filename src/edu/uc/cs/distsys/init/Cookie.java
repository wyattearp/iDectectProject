package edu.uc.cs.distsys.init;

public class Cookie {

	public static final Cookie INVALID_COOKIE = new Cookie(0);
	
	private final long value;
	
	public Cookie(long val) {
		this.value = val;
	}
	
	public long getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cookie other = (Cookie) obj;
		if (value != other.value)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return Long.toString(value);
	}
}
