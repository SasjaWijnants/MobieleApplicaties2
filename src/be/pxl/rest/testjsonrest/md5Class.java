package be.pxl.rest.testjsonrest;

public class md5Class {

	private String original;
	private String md5;
	
	public md5Class(String original, String md5){
		super();
		this.original = original;
		this.md5 = md5;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
