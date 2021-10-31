
public class Node {
	private int number;
	private int x;
	private int y;
	private int demand;

	public Node(int number, int x, int y, int demand) {
		super();
		this.number = number;
		this.x = x;
		this.y = y;
		this.demand = demand;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDemand() {
		return demand;
	}

	public void setDemand(int demand) {
		this.demand = demand;
	}
}
