package com.magic.client.sender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.magic.client.exception.UnconnectException;
import com.magic.client.request.GatewayRequest;
import com.magic.client.request.GatewayResponse;

import io.netty.channel.ChannelHandlerContext;

public class Sender {

	private Map<String, ResponseFuture<GatewayResponse>> futureMap = new HashMap<String, ResponseFuture<GatewayResponse>>();

	private ChannelHandlerContext ctx;

	/**
	 * 发送请求，异步获取结果
	 * 
	 * @param request
	 * @return
	 * @throws UnconnectException
	 */
	public ResponseFuture<GatewayResponse> sendRequest(GatewayRequest request) throws UnconnectException {
		if (ctx == null) {
			throw new UnconnectException();
		}
		ResponseFuture<GatewayResponse> future = this.createFuture(request.getGuid());
		ctx.writeAndFlush(request);
		return future;
	}

	/**
	 * 发送请求，同步获取结果
	 * 
	 * @param request
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	public GatewayResponse sendRequest(GatewayRequest request, long timeout, TimeUnit unit) throws Exception {
		GatewayResponse result = this.sendRequest(request).get(timeout, unit);
		return result;
	}

	public void setResponse(String guid, GatewayResponse response) {
		if (futureMap.containsKey(guid)) {
			futureMap.get(guid).setResponse(response);
			futureMap.remove(guid);
		}
	}

	private ResponseFuture<GatewayResponse> createFuture(String guid) {
		ResponseFuture<GatewayResponse> future = new ResponseFuture<GatewayResponse>();
		futureMap.put(guid, future);
		return future;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}
