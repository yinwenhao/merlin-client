package com.magic.client;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;

import com.magic.client.impl.MerlinClientImpl;
import com.magic.client.request.GatewayResponse;
import com.magic.client.sender.ResponseFuture;

public class MainStressTest {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		MerlinClient client = new MerlinClientImpl(
				new String[] { "127.0.0.1:5612", "127.0.0.1:5613", "127.0.0.1:5614" });

		// set 同步接口压测，200多的qps
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			client.set("aaa" + i,
					"aaaaaaaaaaaaaaaaaaaafgsgerfsaifhdsaofhpaydfpgvkvbjsgaysifeorgdlhskcsa;ghsa;ghkcbjsbsgfsgoworywyfueywgjbyr921721738696534y32yrhufsyr296r34");
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);

		// set 异步接口压测，10000左右的qps
		long start2 = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			client.setAsyn("bbb" + i,
					"bbbbbbbbbbbbbbbbbbbsgerfsaifhdsaofhpaydfpgvkvbjsgasfsasdeaysifeorgdlhskcsa;ghsa;ghkcbjsbsgfsgoworywsafdyfueywgjbyr921721738696534y32yrhufsyr296r34");
		}
		long end2 = System.currentTimeMillis();
		System.out.println(end2 - start2);

		// get 随机读，异步并获取结果，接口压测，1000左右的qps
		ExecutorService pool = (ExecutorService) Executors.newFixedThreadPool(6);
		Random ran = new Random();
		String[] keys = new String[10000];
		for (int i = 0; i < 10000; i++) {
			keys[i] = "aaa" + ran.nextInt(10000);
		}
		long start3 = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			pool.execute(new FutureWorker(client.getAsyn(keys[i])));
		}
		pool.shutdown();
		if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
			System.out.println("pool: close threads take too long time.");
		}
		long end3 = System.currentTimeMillis();
		System.out.println(end3 - start3);

		// get 随机读，同步接口压测，1000左右的qps
		long start4 = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			client.get(keys[i]);
		}
		long end4 = System.currentTimeMillis();
		System.out.println(end4 - start4);

		// get 随机读，异步不获取结果，接口压测，20000左右的qps
		long start5 = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			client.getAsyn(keys[i]);
		}
		long end5 = System.currentTimeMillis();
		System.out.println(end5 - start5);
	}

	private static class FutureWorker implements Runnable {
		private ResponseFuture<GatewayResponse> future;

		public FutureWorker(ResponseFuture<GatewayResponse> future) {
			this.future = future;
		}

		@Override
		public void run() {
			try {
				this.future.get(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
