package work;

public class TimeSlot {
    private String team;
    private int slot;

    public TimeSlot() {

    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int actualSlot) {
        this.slot = actualSlot;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String string) {
        this.team = string;
    }
}
