package be.pxl.rest.testjsonrest;

public class TimeDateEntity {

	private String time;
	private String date;
	private long milliseconds_since_epoch;
	
	public TimeDateEntity(String time, String date, long milliseconds_since_epoch){
		super();
		this.time = time;
		this.date = date;
		this.milliseconds_since_epoch = milliseconds_since_epoch;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public long getMilliseconds_since_epoch() {
		return milliseconds_since_epoch;
	}

	public void setMilliseconds_since_epoch(long milliseconds_since_epoch) {
		this.milliseconds_since_epoch = milliseconds_since_epoch;
	}
}
