package org.springframework.async.http;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private HttpMethod method;
	private URI uri;
	private ClientBootstrap clientBootstrap;
	private ChannelFuture future;

	public HttpClient(URI uri) {
		this.uri = uri;
		clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newFixedThreadPool(2),
																																						Executors.newFixedThreadPool(2)));
		clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("http.codec", new HttpClientCodec());
				pipeline.addLast("http.compresor", new HttpContentDecompressor());
				pipeline.addLast("http.chunkAggregator", new HttpChunkAggregator(1048576));
				pipeline.addLast("http.handler", new SimpleChannelUpstreamHandler() {
					@Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
						log.info("Message received: " + e.getMessage());
					}
				});

				return pipeline;
			}
		});
		future = clientBootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
	}

	public void get() {
		future.addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath());
				request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
				request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
				future.getChannel().write(request);
			}
		});

		future.getChannel().getCloseFuture().awaitUninterruptibly();
		clientBootstrap.releaseExternalResources();
	}

}
