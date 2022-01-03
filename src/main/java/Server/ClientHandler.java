package Server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final Map<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;
    private final Map<Integer, FullHttpResponse> responseMap = PlatformDependent.newConcurrentHashMap();
    private final Map<Integer, String> payloadMap = PlatformDependent.newConcurrentHashMap();

    public ClientHandler() {
        streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
    }

    public Map.Entry<ChannelFuture, ChannelPromise> put(int streamId, ChannelFuture writeFuture, ChannelPromise promise) {
        return streamidPromiseMap.put(streamId, new AbstractMap.SimpleEntry<ChannelFuture, ChannelPromise>(writeFuture, promise));
    }

    void awaitResponses(long timeout, TimeUnit unit) {
        Iterator<Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
            ChannelFuture writeFuture = entry.getValue().getKey();
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
            }
            if (!writeFuture.isSuccess()) {
                throw new RuntimeException(writeFuture.cause());
            }
            ChannelPromise promise = entry.getValue().getValue();
            if (!promise.awaitUninterruptibly(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
            }
            if (!promise.isSuccess()) {
                throw new RuntimeException(promise.cause());
            }
            itr.remove();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (streamId == null) {
            return;
        }

        Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap.get(streamId);
        if (entry == null) {
        } else {
            payloadMap.put(streamId, parsePayload(msg.content()));
            responseMap.put(streamId, msg);
            entry.getValue().setSuccess();
        }
    }

    public FullHttpResponse getFullResponse(int streamId) {
        return responseMap.get(streamId);
    }

    public String getResponsePayload(int streamId) {
        return payloadMap.get(streamId);
    }

    private String parsePayload(ByteBuf content) {
        if (content.isReadable()) {
            int contentLength = content.readableBytes();
            byte[] arr = new byte[contentLength];
            content.readBytes(arr);
            return new String(arr, 0, contentLength, CharsetUtil.UTF_8);

        }
        return null;
    }
}
