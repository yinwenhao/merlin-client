package com.magic.client.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.client.MerlinClient;
import com.magic.client.constants.Constants;
import com.magic.client.exception.TimeoutException;
import com.magic.client.netty.NettyClient;
import com.magic.client.request.GatewayRequest;
import com.magic.client.request.GatewayResponse;
import com.magic.client.sender.ResponseFuture;
import com.magic.client.sender.Sender;

public class MerlinClientImpl implements MerlinClient, Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private OneClient[] ocs;

	private NettyClient[] ncs;

	private int index = 0;

	private long timeout;

	private TimeUnit timeoutUnit;

	/**
	 * 构造函数，会直接连接netty，所有接口使用默认的超时时间
	 * 
	 * @param addresses
	 * @throws Exception
	 */
	public MerlinClientImpl(String[] addresses) throws Exception {
		this(addresses, Constants.DEFAULT_TIMEOUT, Constants.DEFAULT_TIMEOUT_UNIT);
	}

	public MerlinClientImpl(String[] addresses, long timeout, TimeUnit timeoutUnit) throws Exception {
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		int size = addresses.length;
		this.ocs = new OneClient[size];
		this.ncs = new NettyClient[size];
		for (int i = 0; i < size; i++) {
			String address = addresses[i];
			String[] ss = address.split(":");
			int port = Integer.valueOf(ss[1]);
			Sender sender = new Sender();
			NettyClient nc = new NettyClient(ss[0], port, sender, 1, 1000);
			this.ncs[i] = nc;
			this.ocs[i] = new OneClient(sender);
		}
		this.connect();
	}

	@Override
	public void connect() throws Exception {
		if (this.ncs == null || this.ncs.length <= 0) {
			throw new Exception("NettyClient is null");
		}
		Runtime.getRuntime().addShutdownHook(new Thread(this));
		for (NettyClient nc : this.ncs) {
			nc.startClient();
		}
	}

	@Override
	public void close() {
		if (this.ncs != null) {
			for (NettyClient nc : this.ncs) {
				if (nc != null) {
					nc.shutdown();
				}
			}
		}
	}

	private ResponseFuture<GatewayResponse> doRequestAsyn(GatewayRequest request) throws Exception {
		OneClient oc = this.getOneClient();
		ResponseFuture<GatewayResponse> response = null;
		try {
			response = oc.getSender().sendRequest(request);
		} catch (Exception e) {
			log.error("sender send request error:", e);
			response = null;
		}
		this.next();
		if (response == null) {
			oc.setUnreachable();
			oc = this.getOneClient();
			response = oc.getSender().sendRequest(request);
			this.next();
			if (response == null) {
				throw new TimeoutException();
			}
		}
		return response;
	}

	private GatewayResponse doRequest(GatewayRequest request, long timeout, TimeUnit timeoutUnit) throws Exception {
		OneClient oc = this.getOneClient();
		GatewayResponse response = null;
		try {
			response = oc.getSender().sendRequest(request, timeout, timeoutUnit);
		} catch (Exception e) {
			log.error("sender send request error:", e);
			response = null;
		}
		this.next();
		if (response == null) {
			oc.setUnreachable();
			oc = this.getOneClient();
			response = oc.getSender().sendRequest(request, timeout, timeoutUnit);
			this.next();
			if (response == null) {
				throw new TimeoutException();
			}
		}
		return response;
	}

	private OneClient getOneClient() throws Exception {
		OneClient oc = ocs[index];
		if (!oc.isAvailable()) {
			for (int i = 0; i < ocs.length && !oc.isAvailable(); i++) {
				this.next();
				oc = ocs[index];
			}
		}
		if (!oc.isAvailable()) {
			throw new TimeoutException();
		}
		return oc;
	}

	private void next() {
		this.index = (this.index + 1) % this.ocs.length;
	}

	@Override
	public String get(String key, long timeout, TimeUnit timeoutUnit) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.GET, key);
		GatewayResponse response = doRequest(request, timeout, timeoutUnit);
		if (response.getError() == GatewayResponse.OK) {
			return response.getValue();
		} else if (response.getError() == GatewayResponse.NOT_EXIST_ERROR) {
			return null;
		} else if (response.getError() == GatewayResponse.KEY_ERROR) {
			throw new Exception("key can not be bull");
		} else {
			throw new Exception("response error:" + response.getError());
		}
	}

	@Override
	public void set(String key, String value, long timeout, TimeUnit timeoutUnit) throws Exception {
		setWithExpire(key, value, Constants.EXPIRE_TIME_DEFAULT, timeout, timeoutUnit);
	}

	@Override
	public void setWithExpire(String key, String value, long expire, long timeout, TimeUnit timeoutUnit)
			throws Exception {
		if (expire < 0) {
			throw new Exception("response error:" + GatewayResponse.EXPIRE_ERROR);
		}
		if (value == null) {
			throw new Exception("value can not be null");
		}
		GatewayRequest request = new GatewayRequest(Constants.SET, key, value, expire);
		GatewayResponse response = doRequest(request, timeout, timeoutUnit);
		if (response.getError() == GatewayResponse.OK) {
			return;
		} else if (response.getError() == GatewayResponse.KEY_ERROR) {
			throw new Exception("key can not be bull");
		} else if (response.getError() == GatewayResponse.VALUE_ERROR) {
			throw new Exception("value can not be bull");
		} else {
			throw new Exception("response error:" + response.getError());
		}
	}

	@Override
	public void delete(String key, long timeout, TimeUnit timeoutUnit) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.DELETE, key);
		GatewayResponse response = doRequest(request, timeout, timeoutUnit);
		if (response.getError() == GatewayResponse.OK) {
			return;
		} else if (response.getError() == GatewayResponse.KEY_ERROR) {
			throw new Exception("key can not be bull");
		} else {
			throw new Exception("response error:" + response.getError());
		}
	}

	@Override
	public String get(String key) throws Exception {
		return this.get(key, this.timeout, this.timeoutUnit);
	}

	@Override
	public void set(String key, String value) throws Exception {
		this.setWithExpire(key, value, Constants.EXPIRE_TIME_DEFAULT, this.timeout, this.timeoutUnit);
	}

	@Override
	public void setWithExpire(String key, String value, long expire) throws Exception {
		this.setWithExpire(key, value, expire, this.timeout, this.timeoutUnit);
	}

	@Override
	public void delete(String key) throws Exception {
		this.delete(key, this.timeout, this.timeoutUnit);
	}

	@Override
	public void run() {
		this.close();
	}

	@Override
	public ResponseFuture<GatewayResponse> getAsyn(String key) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.GET, key);
		return this.doRequestAsyn(request);
	}

	@Override
	public ResponseFuture<GatewayResponse> setAsyn(String key, String value) throws Exception {
		return setWithExpireAsyn(key, value, Constants.EXPIRE_TIME_DEFAULT);
	}

	@Override
	public ResponseFuture<GatewayResponse> setWithExpireAsyn(String key, String value, long expire) throws Exception {
		if (expire < 0) {
			throw new Exception("response error:" + GatewayResponse.EXPIRE_ERROR);
		}
		if (value == null) {
			throw new Exception("value can not be null");
		}
		GatewayRequest request = new GatewayRequest(Constants.SET, key, value, expire);
		return this.doRequestAsyn(request);
	}

	@Override
	public ResponseFuture<GatewayResponse> deleteAsyn(String key) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.DELETE, key);
		return this.doRequestAsyn(request);
	}

}
