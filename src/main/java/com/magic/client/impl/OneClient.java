package com.magic.client.impl;

import com.magic.client.sender.Sender;

public class OneClient {

	private Sender sender;

	private Status status;

	private long tryAgainTime = 0;

	public OneClient(Sender sender) {
		this.sender = sender;
		this.status = Status.Normal;
	}

	public Sender getSender() {
		return sender;
	}

	public void setUnreachable() {
		this.tryAgainTime = System.currentTimeMillis() + 60000;
		this.status = Status.Unreachable;
	}

	public boolean isAvailable() {
		if (this.status == Status.Normal) {
			return true;
		} else if (this.status == Status.Unreachable) {
			if (System.currentTimeMillis() >= tryAgainTime) {
				this.status = Status.Normal;
				return true;
			}
		}
		return false;
	}

	private enum Status {
		Normal, Unreachable, Down
	}

}
