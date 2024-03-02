package de.intranda.goobi.plugins.licence;

import lombok.Data;

@Data
public class Contract {
	private String name;
	private String type;
	private long size;
	private long usedSize;
	private long usedPercent;
	private String ordernumber;
	private boolean enabled;	
}
