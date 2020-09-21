package com.nio.wechat.sever;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WeChatServer {
	
	private ServerSocketChannel  serverSocketChannel;
	private Selector selector;
	private  int port;
	public WeChatServer(int port) {
		this.port = port;
		init();
	}
	private void init() {
		try {
			serverSocketChannel=ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
			selector= Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void listen() throws IOException {
		while(selector.select()>0){ // 阻塞
			Set<SelectionKey> keys=selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while(iterator.hasNext()){
				SelectionKey next = iterator.next();
				if(next.isAcceptable()){ // 连接事件
					 SocketChannel channel = serverSocketChannel.accept();
					 SocketAddress remoteAddress = channel.getRemoteAddress();
					 String message=remoteAddress+"：上线！";
					 System.out.println(message);
					 channel.configureBlocking(false);
					 channel.register(selector, SelectionKey.OP_READ);
					 handleSync(message, next);
				} else if(next.isReadable()){
					handleRead(next);
				}
				iterator.remove();
			}
		}
	}
	private void handleSync(String message,SelectionKey ignoreKey) throws IOException {
		Set<SelectionKey> keys = selector.keys();
		for(SelectionKey key:keys){
			Channel channel = key.channel();
			if(channel instanceof SocketChannel && key != ignoreKey){
				((SocketChannel)channel).write(ByteBuffer.wrap(message.getBytes("UTF-8")));
			}
		}
			
		}
	
	private void handleRead(SelectionKey key) throws IOException {
		try {
			SocketChannel sc=(SocketChannel) key.channel();
			ByteBuffer allocate = ByteBuffer.allocate(1024);
			while(sc.read(allocate)>0){
				allocate.flip();
				byte[] data = new byte[allocate.limit()];
				allocate.get(data);
				String dataStr=new String(data);
				String message=sc.getRemoteAddress().toString()+"："+dataStr;
				System.out.println(message);
				//转发消息
				handleSync(message,key);
				allocate.clear();
			}
			System.out.println();
		} catch (IOException e) {
			// 处理客户端断开产生的异常
			
		}
		
	}
	
	public static void main(String[] args) {
		try {
			new WeChatServer(9951).listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
