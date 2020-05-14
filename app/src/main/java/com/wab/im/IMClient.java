package com.wab.im;

import com.wab.im.message.Message;
import com.wab.im.ui.login.ConfigUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.Serializable;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @program: WAB_IM_System
 * @description:
 * @author: Mr.Wang Anbang
 * @create: 2020-01-21 10:05
 **/
public class IMClient implements Serializable {
    private String host;
    private int port;
    private Channel channel;
    private Bootstrap b;
    private final EventLoopGroup group = new NioEventLoopGroup();;

    public IMClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = socketChannel.pipeline()
                        .addLast("protobufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder())
                        .addLast("protobufDecoder", new ProtobufDecoder(Message.IMMessage.getDefaultInstance()))
                        .addLast("protobufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender())
                        .addLast("protobufEncoder", new ProtobufEncoder())
                        .addLast(new IdleStateHandler(0, 0, 20, TimeUnit.SECONDS))
                                .addLast(new ChannelInboundHandlerAdapter());
                        pipeline.addLast(new IMClientHandler()); //客户端处理类

                    }
                });
        //发起异步连接请求，绑定连接端口和host信息
        final ChannelFuture future = b.connect(host, port).sync();

        future.addListener((ChannelFutureListener) arg0 -> {
            if (future.isSuccess()) {
                System.out.println("连接服务器成功");

            } else {
                System.out.println("连接服务器失败");
                future.cause().printStackTrace();
                group.shutdownGracefully(); //关闭线程组
            }
        });



        this.channel = future.channel();
    }

    public Channel getChannel() {
        return channel;
    }

    public void auth(String key, String uid) {
        Message.IMMessage.Builder builder = Message.IMMessage.newBuilder();
        Message.IMAuthMessage authMessage = Message.IMAuthMessage.newBuilder()
                .setMsgId(System.currentTimeMillis() + "")
                .setSource(ConfigUtil.IM_SOURCE)
                .setToken(key)
                .setUserId(uid).build();
        Message.IMMessage imMessage = builder.setDataType(Message.IMMessage.DataType.IMAuthMessage).setAuthMessage(authMessage).build();
        getChannel().writeAndFlush(imMessage);
    }

    public void closeChannel(){
        if (channel!= null){
            channel.close();
        }
        group.shutdownGracefully();
    }


//    public static void main(String[] args) throws Exception {
//        IMClient client = new IMClient("127.0.0.1", 5280);
//        client.start();
//        //认证
//        client.auth("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTA1NjQzMjksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiU1lTVEVNIiwiVVNFUiIsIkFETUlOIl0sImp0aSI6IjRiYjU2MTJjLTE0ZGUtNDY5Zi05MzRmLThmZTFiNmVjNTkxNyIsImNsaWVudF9pZCI6ImFwcGNsaWVudCIsInNjb3BlIjpbIm9wZW5pZCJdfQ.LV5V7uUKrIWkQmztIQdXSNWPQqS9vHRoIf7WYDhbJDqMmJ_86ybR9hSX1VhDTJy2w_0KKiar8wCslssrlQWqEVWJyUeRBPsIs7PsX8sx8L2bDWBYn4DruPLrvaDVfjhtEvPcu6wMGOM8OpnmzwWEt4kmZ_8PzPHETqkoR2D0jnHjmYZLYCdQQ7Nm0FNpQuvM6wkN3ide-nwGBnrajj03q98_Pz0QF9ay9MFdORQrfMrwShVUDe2PV_gI8mfgthI3Jk_M4poSI25_dRorTR037UK7CxQlcRmzKI84eGsMpUebDVAM2bZP1cZW0_a4ELVmrN6s7rgCs5LoEGqlT0OSBg");
//        Scanner scanner = new Scanner(System.in);
//        while (true) {
//            System.out.println("输入一条消息：");
//            String msg = scanner.nextLine();
//            Message.IMMessage.Builder builder = Message.IMMessage.newBuilder();
//            Message.IMChatMessage chartMSG = Message.IMChatMessage.newBuilder()
//                    .setMsgId(System.currentTimeMillis() + "")
//                    .setBody(msg)
//                    .setFrom("1213131")
//                    .setType(Message.MessageType.TextMessage)
//                    .setTo("20201242307000001")
//                    .setNick("李德").build();
//            System.out.println("发送聊天消息 ---> " + chartMSG);
//            Message.IMMessage imMessage = builder.setDataType(Message.IMMessage.DataType.IMChatMessage).setChatMessage(chartMSG).build();
//            client.getChannel().writeAndFlush(imMessage);
//        }
//    }
}
