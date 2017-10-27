package com.worldline.asp.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

@RunWith(SpringJUnit4ClassRunner.class)
/*@WebAppConfiguration*/
@ContextConfiguration(locations = {
		"classpath:META-INF/spring/test-context.xml",
})
public class TNettyClient {

    private static class CardlinkByteToMessageDecoder extends ByteToMessageDecoder {

        private Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            LOGGER.info("in.readableBytes():{}", in.readableBytes());

            int frameLength = -1;

            if (in.readableBytes() >= 2) {
                int first = Integer.parseInt(Hex.encodeHexString(new byte[] { in.getByte(in.readerIndex()) }), 16);
                int second = Integer
                        .parseInt(Hex.encodeHexString(new byte[] { in.getByte(in.readerIndex()+1) }), 16);
                first = first << 8; // first * 256
                frameLength = first + second;
                LOGGER.trace("Check Frame Length:{}|in.readableBytes():{}|", frameLength, in.readableBytes());

                if (in.readableBytes() - 2 >= frameLength) {
                    LOGGER.trace("Readable bytes:{}| > Frame Length:{}| Read from the byte buff", in.readableBytes(), frameLength);
                    return in.readSlice(frameLength + 2).retain();
                }
            } else {
                LOGGER.trace("Not enough readable bytes to deter frame length, Frame Length:{}|in.readableBytes():{}|", frameLength, in.readableBytes());
            }

            return null;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
                throws Exception {
            Object decoded = decode(ctx, in);
            if (decoded != null) {
                out.add(decoded);

            }
        }

    }
    public static enum ConnectionStatus {
        CONNECTED, CONNECTING, DISCONNECTED, UNKNOWN;
    }
    private static class InboundMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private static final Logger LOGGER = LoggerFactory.getLogger(InboundMessageHandler.class);

        private TNettyClient emc;
        private BlockingQueue<String> requests;

        public InboundMessageHandler(TNettyClient emc, BlockingQueue<String> requests) {
            this.emc = emc;
            this.requests = requests;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, ByteBuf messageByteBuf)
                throws Exception {
            Long receiveTime = System.currentTimeMillis();
            String message = getMessageFromByteBuf(messageByteBuf);
            Pair<String, Long> ret = Pair.of(message, new Long(receiveTime));
            emc.getResponses().add(ret);
            LOGGER.info("MSG:{}", message);

            message.substring(23, 28);
            requests.add(message);
        }

        private String getMessageFromByteBuf(ByteBuf messageByteBuf) {
            Integer messageLengthOffset = emc.getMessageLengthOffset();
            Preconditions.checkNotNull(messageLengthOffset);

            String originalMsg = messageByteBuf.toString(Charset.forName(emc.getCharset()));
            String message = StringUtils.defaultString(StringUtils.substring(originalMsg, messageLengthOffset));
            return message;
        }

    }
    private static final Logger LOGGER = LoggerFactory.getLogger(TNettyClient.class);

    /**
     * Can create Channel and TCP Connections.
     */
    private Bootstrap bootstrap;
    private CardlinkByteToMessageDecoder cardlinkByteToMessageDecoder;
    private OioSocketChannel channel;
    private String charset;
    private volatile ConnectionStatus connectionStatus;

    private String host;
    private InboundMessageHandler inboundMessageHandler;

    private volatile boolean isInit;
    private volatile boolean isShutdown;
    private Integer messageLengthOffset;

    private Integer port;

    // FIXME: private attributes should configurable in application config (applicationContext)
    private Integer reconnectDelaySeconds; //default 15s

    private Executor reconnectExecutor;

    private BlockingQueue<String> requests;

    private BlockingQueue<Pair<String, Long>> responses;

    private EventLoopGroup workerGroup;

    public synchronized void connect() {
        // close previous channel if not null to avoid memory leak
        if (connectionStatus != ConnectionStatus.DISCONNECTED) {
            LOGGER.error("em is in {}, cannot connect", connectionStatus);
            throw new IllegalStateException("cannot connect when em is not disconnected");
        } else {
            connectionStatus = ConnectionStatus.CONNECTING;
            doConnect();
        }
    }

    public long defaultTimeout() {
        // default timeout is 60 seconds
        return TimeUnit.SECONDS.toMillis(60L);
    }

    @PreDestroy
    public void destroy(){
        LOGGER.info("PreDestroy to close the EM connection");
        isShutdown = true;
    }

    private void doConnect() {
        if(connectionStatus != ConnectionStatus.CONNECTING){
            LOGGER.error("perform doConnect, emc must be in CONNECTING state");
            throw new IllegalStateException("perform doConnect, emc must be in CONNECTING state");
        }

        LOGGER.info("attempt to connect... ");
        ChannelFuture future = bootstrap.connect();

        if(future.channel() instanceof OioSocketChannel){
            channel = (OioSocketChannel) future.channel();
        } else {
            connectionStatus = ConnectionStatus.UNKNOWN;
            throw new RuntimeException("channel is not an instance of " + OioSocketChannel.class);
        }

        try {
            future.sync();
            if(future.isSuccess()){
                LOGGER.info("connected with remoteAddress:{}, localAddress:{}",
                        channel.remoteAddress().toString(), channel.localAddress().toString());

                if(channel.isActive()){
                    connectionStatus = ConnectionStatus.CONNECTED;
                    LOGGER.info("connected to host in active:{}, local address:{}",
                            getSocketAddress(channel.remoteAddress()),
                            getSocketAddress(channel.localAddress()));
                } else {
                    connectionStatus = ConnectionStatus.UNKNOWN;
                    LOGGER.error("cannot connect to Host, channel is not active after connect is finished");
                    throw new RuntimeException("cannot connect to Host, channel is not active after connect is finished");
                }

            } else {
                connectionStatus = ConnectionStatus.UNKNOWN;
                LOGGER.error("cannot connect to Host, schedule reconnect after:{} s", reconnectDelaySeconds);
            }

        } catch (InterruptedException e) {
            connectionStatus = ConnectionStatus.UNKNOWN;
            LOGGER.error("interrupted when connecting", e);
            throw new RuntimeException("interrupted when connecting", e);
        } catch (Exception e){
            connectionStatus = ConnectionStatus.UNKNOWN;
            LOGGER.error("cannot connect to Host", e);
        } finally {
            if(connectionStatus == ConnectionStatus.UNKNOWN){
                LOGGER.info("schedule reconnect as the status is not CONNECTED");
                scheduleReconnect();
            }
        }
    }

    public String getCharset() {
        return charset;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public String getHost() {
        return host;
    }

    /**
     * Ben Ng: Big Endian
     * Hudson Hui: CardLink used Big Endian design, I guess Ben put this comment here because it
     * related to the message length, but this comment should also put to the message encoder and
     * decoder.
     *
     * @return message length offset - default value is 2
     */
    public Integer getMessageLengthOffset() {
        return ObjectUtils.defaultIfNull(messageLengthOffset, 2);
    }

    public Integer getPort() {
        return port;
    }

    public Integer getReconnectDelaySeconds() {
        return ObjectUtils.defaultIfNull(reconnectDelaySeconds, 60);
    }

    public Executor getReconnectExecutor() {
        return reconnectExecutor;
    }

    public BlockingQueue<String> getRequests(){
        return requests;
    }

    public BlockingQueue<Pair<String, Long>> getResponses() {
        return responses;
    }

    private String getSocketAddress(InetSocketAddress address){
        if(address != null){
            return address.getHostString() + ":" + address.getPort();
        } else {
            return "n\\a";
        }
    }

    public synchronized void init(){
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        this.channel = null;
        this.isShutdown = false;

        responses = Queues.newLinkedBlockingQueue();

        if(reconnectExecutor == null){
            this.reconnectExecutor = Executors.newSingleThreadExecutor();
        }

        if(bootstrap == null){
            this.workerGroup = new OioEventLoopGroup();
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(workerGroup);
            this.bootstrap.remoteAddress(host, port);
            this.bootstrap.channel(OioSocketChannel.class);
            this.bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);
            this.bootstrap.option(ChannelOption.ALLOW_HALF_CLOSURE, false);

            this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                    ChannelPipeline pipeline = ch.pipeline();
                    internalInit();
                    pipeline.addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            super.channelInactive(ctx);
                            LOGGER.error("channel-localAccress:{} becomes inactive", getSocketAddress(ch.localAddress()));

                            if(isShutdown){
                                LOGGER.info("system shutdown, do not schedule reconnect");
                            } else {
                                if(connectionStatus == ConnectionStatus.CONNECTED){
                                    connectionStatus = ConnectionStatus.UNKNOWN;
                                    scheduleReconnect();
                                }
                            }
                        }

                    });
                    pipeline.addLast(cardlinkByteToMessageDecoder);
                    pipeline.addLast(inboundMessageHandler);

                }
            });
        }

        isInit = true;

    }

    public synchronized void initAndConnect() {
        if(!isInit){
            init();
        }
        isShutdown = false;
        connect();
    }

    private void internalInit() {
        requests = Queues.newLinkedBlockingQueue();
        inboundMessageHandler = new InboundMessageHandler(this, requests);
        cardlinkByteToMessageDecoder = new CardlinkByteToMessageDecoder();
    }

    private synchronized void scheduleReconnect(){

        reconnectExecutor.execute(() -> {
            if(this.connectionStatus == ConnectionStatus.UNKNOWN){
                LOGGER.info("if connection status is unknown, close first");
            }

            if(this.connectionStatus == ConnectionStatus.DISCONNECTED){
                try {
                    Thread.sleep(getReconnectDelaySeconds() * 1000);
                } catch (Exception e) {
                    LOGGER.error("interrupt while waiting reconnect", e);
                } finally{
                    connect();
                }
            } else {
                LOGGER.info("reconnect can only be scheduled when connection is disconnected, ");
            }
        });


    }

    public final void sendMessage(String request){
        if(channel == null || connectionStatus != ConnectionStatus.CONNECTED){
            LOGGER.error("cannot send message when channel is null or not connected");
        }

        int messageLength = request.toCharArray().length;
        ByteBuf byteBuffer = Unpooled.buffer(messageLength + 2);
        byteBuffer.writeByte(messageLength / 256);
        byteBuffer.writeByte(messageLength);
        byteBuffer.writeBytes(Unpooled.copiedBuffer(request, Charset.forName(charset)));

        ChannelFuture result = null;

        result = channel.writeAndFlush(byteBuffer);

        try {
            result.get(); // blocking until the message is sent out (ACK), != received response
        } catch (InterruptedException e){
            LOGGER.error("cannot write and flush to channel", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            LOGGER.error("cannot write and flush to channel", e);
            throw new RuntimeException(e);
        }

    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setMessageLengthOffset(Integer messageLengthOffset) {
        this.messageLengthOffset = messageLengthOffset;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setReconnectDelaySeconds(Integer reconnectDelaySeconds) {
        this.reconnectDelaySeconds = reconnectDelaySeconds;
    }

    public void setReconnectExecutor(Executor reconnectExecutor) {
        this.reconnectExecutor = reconnectExecutor;
    }


}
