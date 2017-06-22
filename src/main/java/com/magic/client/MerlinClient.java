package com.magic.client;

import java.util.concurrent.TimeUnit;

import com.magic.client.request.GatewayResponse;
import com.magic.client.sender.ResponseFuture;

public interface MerlinClient {

	public void connect() throws Exception;

	public void close();

	public String get(String key, long timeout, TimeUnit timeoutUnit) throws Exception;

	public void set(String key, String value, long timeout, TimeUnit timeoutUnit) throws Exception;

	public void setWithExpire(String key, String value, long expire, long timeout, TimeUnit timeoutUnit)
			throws Exception;

	public void delete(String key, long timeout, TimeUnit timeoutUnit) throws Exception;

	public String get(String key) throws Exception;

	public void set(String key, String value) throws Exception;

	public void setWithExpire(String key, String value, long expire) throws Exception;

	public void delete(String key) throws Exception;

	/**
	 * 异步get
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public ResponseFuture<GatewayResponse> getAsyn(String key) throws Exception;

	/**
	 * 异步set
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public ResponseFuture<GatewayResponse> setAsyn(String key, String value) throws Exception;

	/**
	 * 异步setWithExpire
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 * @return
	 * @throws Exception
	 */
	public ResponseFuture<GatewayResponse> setWithExpireAsyn(String key, String value, long expire) throws Exception;

	/**
	 * 异步delete
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public ResponseFuture<GatewayResponse> deleteAsyn(String key) throws Exception;

}
