package com.magic.client.constants;

import java.util.concurrent.TimeUnit;

public class Constants {

	/**
	 * 协议版本号
	 */
	public static final int PROTOCOL_VERSION = 0;

	/**
	 * magic database server name head
	 */
	public static final String SERVICE_NAME_HEAD = "md-server#";

	public static final String GET = "get";

	public static final String SET = "set";

	public static final String BEFORE_SET = "before_set";

	public static final String RESPONSE_OK = "ok";

	public static final String DELETE = "delete";

	public static final String GOSSIP = "gossip";

	public static final String GOSSIP_REQUIRE = "gossip_require";

	public static final String GOSSIP_SET = "gossip_set";

	public static final String HEART_BEAT = "heart_beat";

	/**
	 * 过期时间——默认0，表示不限制
	 */
	public static final long EXPIRE_TIME_DEFAULT = 0;

	/**
	 * 命令默认的超时时间
	 */
	public static final long DEFAULT_TIMEOUT = 5;

	/**
	 * 命令默认的超时时间单位
	 */
	public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

}
