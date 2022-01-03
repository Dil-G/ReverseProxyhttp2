import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class Http2ProxyServer {

    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        SslContext sslCtx = Util.createSSLContext();
        System.err.println("Started at  *: " +  PORT);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("initChannel");
                            ch.pipeline()
                                    .addLast(sslCtx.newHandler(ch.alloc()), Util.getServerAPNHandler());
                        }
                    });
//                    .childOption(ChannelOption.AUTO_READ, false);
//                    .bind(PORT).sync()
//                    .channel().closeFuture().sync();
            ChannelFuture f = b.bind(PORT).sync();

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();        }
    }
}