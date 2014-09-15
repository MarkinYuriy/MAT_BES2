package mat;

import java.util.ArrayList;

public class MattInfo {
	long startPoint;
	long endPoint;
	ArrayList<Boolean> slots;
	public MattInfo(int startPoint, int endPoint,
			ArrayList<Boolean> slots) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.slots = slots;
	}
	@Override
	public String toString() {
		return "MattInfo [startPoint=" + startPoint + ", endPoint=" + endPoint
				+ ", slots=" + slots + "]";
	}
	
}
