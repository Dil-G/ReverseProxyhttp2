import Server.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class ServerHandler extends ChannelDuplexHandler {

    private static  int port=9090 ;
    private final String host="120.0.01";
    private volatile Channel outboundChannel;

    private Http2SettingsHandler settingsHandler;
    private ClientHandler responseHandler;

    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException, SSLException, CertificateException {
        System.out.println("Server Handler Channel Active");

        final Channel inboundChannel = ctx.channel();
        SslContext sslCtx = Util.createClientSSLContext();

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();

        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("Serevr initChannel");
                            settingsHandler = new Http2SettingsHandler(ch.newPromise());
                            responseHandler = new ClientHandler();

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            int maxContentLength = 8192;
                            pipeline.addLast(Util.getClientAPNHandler(maxContentLength, settingsHandler, responseHandler));
                        }
        });

        ChannelFuture channelFuture = b.connect(host, port);
        outboundChannel = channelFuture.channel();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("channel active SUCCESSssss");
                    inboundChannel.read();
                } else {
                    System.out.println("faiiiil");
                    inboundChannel.close();
                }
            }
        });

    }
        @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            System.out.println("Server channelRead");
        if (msg instanceof Http2HeadersFrame) {
            System.out.println("Server channelRead 2");

            Http2HeadersFrame msgHeader = (Http2HeadersFrame) msg;
            if (msgHeader.isEndStream()) {
                System.out.println("Server channelRead 3");

                ByteBuf content = ctx.alloc()
                        .buffer();

                Http2Headers headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText());
                ctx.write(new DefaultHttp2HeadersFrame(headers).stream(msgHeader.stream()));
                ctx.write(new DefaultHttp2DataFrame(content, true).stream(msgHeader.stream()));
            }

        } else {
            System.out.println("Server channel read fail");
            super.channelRead(ctx, msg);
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel Inactive");
        if (outboundChannel != null){
        }
    }
}
