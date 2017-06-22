package com.magic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandManager {

	private MerlinClient client;

	public CommandManager(MerlinClient client) {
		this.client = client;
	}

	public void begin() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean done = false;
		while (!done) {
			try {
				System.out.print("> ");

				String command = in.readLine().trim();
				if (command.isEmpty()) {
					continue;
				}

				String[] parts = command.split("\\s");
				if (parts.length == 0) {
					continue;
				}

				if (parts[0].equals("q") || parts[0].equals("quit")) {
					done = true;
					return;
				}

				switch (parts[0]) {
				case "get":
					String ke = parts[1];
					if (ke.equals("null")) {
						ke = null;
					}
					System.out.println(client.get(ke));
					break;
				case "set":
					String k = parts[1];
					if (k.equals("null")) {
						k = null;
					}
					String v = parts[2];
					if (v.equals("null")) {
						v = null;
					}
					if (parts.length > 3) {
						client.setWithExpire(k, v, Integer.valueOf(parts[3]));
					} else {
						client.set(k, v);
					}
					System.out.println("ok");
					break;
				case "delete":
					client.delete(parts[1]);
					System.out.println("ok");
					break;
				default:
					System.out.println("method error");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
	}
}
