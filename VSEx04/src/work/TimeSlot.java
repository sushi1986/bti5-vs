package work;

public class TimeSlot {
	private Team team;
	private long eta;
	private long ata;
	
	public TimeSlot() {
		
	}

	public long getAta() {
		return ata;
	}

	public void setAta(long ata) {
		this.ata = ata;
	}

	public Team getTeam() {
		return team;
	}

	public long getEta() {
		return eta;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
}
