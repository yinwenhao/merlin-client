package com.magic.client;

import org.apache.log4j.PropertyConfigurator;

import com.magic.client.impl.MerlinClientImpl;

public class Main {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		MerlinClient client = new MerlinClientImpl(
				new String[] { "127.0.0.1:5612", "127.0.0.1:5613", "127.0.0.1:5614" });

		CommandManager cm = new CommandManager(client);
		cm.begin();

		client.close();
	}

}
