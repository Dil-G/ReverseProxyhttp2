//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPromise;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.FullHttpResponse;
//import io.netty.handler.codec.http2.HttpConversionUtil;
//import io.netty.util.CharsetUtil;
//
//import java.util.Iterator;
//import java.util.Map;
//import java.util.SortedMap;
//import java.util.TreeMap;
//import java.util.concurrent.TimeUnit;
//
//public class Http2ClientResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
//    private SortedMap<Integer, ChannelPromise> streamidPromiseMap;
//    public Http2ClientResponseHandler() {
//        streamidPromiseMap = new TreeMap<Integer, ChannelPromise>();
//    }
//
//    public ChannelPromise put(int streamId, ChannelPromise promise) {
//        return streamidPromiseMap.put(streamId, promise);
//    }
//
//    public void awaitResponses(long timeout, TimeUnit unit) {
//        System.out.println("awaitresponseeeeee");
//        Iterator<Map.Entry<Integer, ChannelPromise>> itr = streamidPromiseMap.entrySet().iterator();
////        while (itr.hasNext()) {
//        System.out.println("msg recieving");
//
//        Map.Entry<Integer, ChannelPromise> entry = itr.next();
//        System.out.println("msg recieving 2");
//
//        ChannelPromise promise = entry.getValue();
//        System.out.println("msg recieving 3");
//
//        if (!promise.awaitUninterruptibly(timeout, unit)) {
//            throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
//        }
//        if (!promise.isSuccess()) {
//            System.out.println("msg recieving 4");
//            throw new RuntimeException(promise.cause());
//        }
//        System.out.println("---Stream id: " + entry.getKey() + " received---");
//        itr.remove();
////        }
//    }
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
//        System.out.println("CHannelread0");
//        System.out.println(msg);
//        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
//        if (streamId == null) {
//            System.err.println("HttpResponseHandler unexpected message received: " + msg);
//            return;
//        }
//
//        ChannelPromise promise = streamidPromiseMap.get(streamId);
//        if (promise == null) {
//            System.err.println("Message received for unknown stream id " + streamId);
//        } else {
//            // Do stuff with the message (for now just print it)
//            ByteBuf content = msg.content();
//            if (content.isReadable()) {
//                int contentLength = content.readableBytes();
//                byte[] arr = new byte[contentLength];
//                content.readBytes(arr);
//                System.out.println(new String(arr, 0, contentLength, CharsetUtil.UTF_8));
//            }
//
//            promise.setSuccess();
//        }
//    }
//}
