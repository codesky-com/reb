package com.codesky.reb.interfaces;

public interface Service {
	
	public boolean isEnabled();

	public void start();
	
	public void stop();
	
	public void tick(long ms);
	
}
