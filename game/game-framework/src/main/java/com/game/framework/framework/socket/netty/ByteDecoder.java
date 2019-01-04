package com.game.framework.framework.socket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import com.game.framework.component.log.Log;


/**
 *  协议解码器<br>
 * ByteDecoder.java
 * @author JiangBangMing
 * 2019年1月3日下午4:11:59
 */
public class ByteDecoder extends ByteToMessageDecoder {

	@Override
	protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
			throws Exception {
		// 循环解码
		while (true) {
			Object obj = decode(ctx, in);
			if (obj == null) {
				break; // 解析不出來
			}
			out.add(obj);
		}
	}

	private Object decode(ChannelHandlerContext ctx, ByteBuf in) {
		// 消息头
		if (in.readableBytes() < ByteEncoder.MSG_HEAD_SIZE) {
			return null;
		}

		// 解密头
		ByteBuf buf = in.slice();
		short head = buf.readShort();
		if (head != ByteEncoder.MSG_HEAD) {
			close(ctx);
			in.clear(); // 头消息错误, 清除数据, 等待下一个消息.
			return null;
		}

		// 读取消息长度, 协议号和长度异常,允许0.
		int length = buf.readInt();
		if (length < 0) {
			Log.error("消息长度错误: length=" + length + ",from:" + ctx.channel().remoteAddress());
			close(ctx); // 关闭连接
			return null;
		}
		// 检测数据包大小
		if (length > buf.readableBytes()) {
			return null;
		}

		// 读取消息
		byte[] data = null;
		if (length > 0) {
			data = new byte[length];
			buf.readBytes(data);
		}

		// 数据提取标记
		int readSize = buf.readerIndex();
		in.readerIndex(in.readerIndex() + readSize);

		return data;
	}

	/** 关闭连接 **/
	private void close(ChannelHandlerContext ctx) {
		ctx.channel().close();
	}

}
