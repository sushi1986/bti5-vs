package work;

public class TimeSlot {
	private String team;
	private long eta;
	private long ata;
	private int slot;
	
	public TimeSlot() {
		
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int actualSlot) {
		this.slot = actualSlot;
	}
	
	public void setEta(long eta) {
		this.eta = eta;
	}

	public long getAta() {
		return ata;
	}

	public void setAta(long ata) {
		this.ata = ata;
	}

	public String getTeam() {
		return team;
	}

	public long getEta() {
		return eta;
	}

	public void setTeam(String string) {
		this.team = string;
	}
}
