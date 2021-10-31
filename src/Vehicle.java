
public class Vehicle {
	private int number;
	private int maxCapacity;
	private int currentCapacity;

	public Vehicle(int number, int maxCapacity, int currentCapacity) {
		super();
		this.number = number;
		this.maxCapacity = maxCapacity;
		this.currentCapacity = currentCapacity;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public int getCurrentCapacity() {
		return currentCapacity;
	}

	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

}
