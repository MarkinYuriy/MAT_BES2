package mat.sn;

import java.util.ArrayList;

public class MattInfo {

	long startPoint;
	long endPoint;
	ArrayList<Boolean> slots;
	int slotsInHour;
	
	public MattInfo(long startPoint, long endPoint,
			ArrayList<Boolean> slots,	int slotsInHour) {
		this.slotsInHour = slotsInHour;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.slots = slots;
	}

	@Override
	public String toString() {
		return "MattInfo [startPoint=" + startPoint + ", endPoint=" + endPoint
				+ ", slots=" + slots + ", slotsInHour=" + slotsInHour + "]";
	}

}
