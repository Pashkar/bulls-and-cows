package paxus.bnc.android.hint;


public interface IHintView {

	public void setBorder(AlarmBorder border);

	public void setBorderVisible(boolean borderVisible);

	public void toggleBorderVisible();

	public int getWidth();	//comes from View class

	public int getHeight();	//comes from View class
	
}
