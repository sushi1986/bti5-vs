package work;

public class TimeSlot {
	private int team;
	private long eta;
	private long ata;
	
	public TimeSlot(int team, long eta) {
		this.team = team;
		this.eta = eta;
	}

	public long getAta() {
		return ata;
	}

	public void setAta(long ata) {
		this.ata = ata;
	}

	public int getTeam() {
		return team;
	}

	public long getEta() {
		return eta;
	}
}
