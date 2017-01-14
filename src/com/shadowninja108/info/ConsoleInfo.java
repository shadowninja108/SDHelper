package com.shadowninja108.info;

public class ConsoleInfo {

	public type type;
	public ver ver;
	public region region;

	public ConsoleInfo() {
		ver = new ver();
	}

	public class ver {
		public int major, minor, patch, idk;
	}

	public enum region {
		USA, EUR, JPN, KOR, TWN
	}

	public enum type {
		NEW, OLD
	}
}
