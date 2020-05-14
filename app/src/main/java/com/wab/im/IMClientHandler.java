package com.wab.im;

import android.os.Bundle;

import com.wab.im.message.Message;
import com.wab.im.ui.login.ChatActivity;
import com.wab.im.ui.login.ConfigUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @program: WAB_IM_System
 * @description:
 * @author: Mr.Wang Anbang
 * @create: 2020-01-17 10:37
 **/
public class IMClientHandler extends SimpleChannelInboundHandler<Message.IMMessage> {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("SDY_IM channel注册");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("SDY_IM channel活跃状态");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("SDY_IM 客户端与服务端断开连接之后");
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.ALL_IDLE) {
            Message.IMPingMessage pingMessage = Message.IMPingMessage.newBuilder()
                    .setMsgId(System.currentTimeMillis()+"")
                    .setSource(ConfigUtil.IM_SOURCE)
                    .setUserId(ConfigUtil.USER_ID).build();
            Message.IMMessage pingMsg = Message.IMMessage.newBuilder()
                    .setDataType(Message.IMMessage.DataType.IMPingMessage)
                    .setPingMessage(pingMessage).build();
            ctx.writeAndFlush(pingMsg);
            System.out.println("SDY_IM 发送ping消息");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message.IMMessage msg) throws Exception {
        Message.IMMessage.DataType dataType = msg.getDataType();
        System.out.println("收到消息----> " + dataType);
        switch (dataType) {
            case IMAuthMessage:
                System.out.println("SDY_IM 收到消息 " + dataType + " : " + msg.getAuthMessage());
                break;
            case IMChatMessageACK:
                System.out.println("SDY_IM 收到消息: " + dataType + " : " +  msg.getChatMessageAck());
                android.os.Message messageAck =new android.os.Message();
                messageAck.what=2;
                Bundle bundleack=new Bundle();
                bundleack.putSerializable("msg",msg.getChatMessageAck());
                messageAck.setData(bundleack);
                ChatActivity.getMainActivity().getMsghandler().sendMessage(messageAck);
                break;
            case IMAuthMessageAck:
                System.out.println("SDY_IM 收到消息: " + dataType + " : " + msg.getAuthMessageAck());
                System.out.println("SDY_IM 收到消息: " + dataType + " : " + msg.getAuthMessageAck().getMessageBytes().toString("UTF-8"));
                break;
            case IMChatMessage:
                System.out.println("SDY_IM 收到消息: " + dataType + " : " + msg.getChatMessage());
                android.os.Message message=new android.os.Message();
                message.what=1;
                Bundle bundle=new Bundle();
                bundle.putSerializable("msg",msg.getChatMessage());
                message.setData(bundle);
                ChatActivity.getMainActivity().getMsghandler().sendMessage(message);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("捕获channel异常");
        super.exceptionCaught(ctx, cause);
    }
}
