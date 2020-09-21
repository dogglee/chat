package com.nio.wechat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class WeChatClient {
	private SocketChannel channel;
	private Selector selector;
	private String ip;
	private int port;
	public WeChatClient(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
		init();
	}
	public void init() {
		try {
			channel=SocketChannel.open(new InetSocketAddress(ip, port));
			channel.configureBlocking(false);
			selector=Selector.open();
			channel.register(selector, SelectionKey.OP_READ);
			new Thread(new Runnable() {
				public void run() {
					keepRead();
				}
			}){}.start();
			keepWrite();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void keepWrite() throws IOException{
		Scanner scan=new Scanner(System.in);
		
		while(true){
			String line=scan.nextLine();
			byte[] bytes = line.getBytes();
			channel.write(ByteBuffer.wrap(bytes));
			if("quit".equalsIgnoreCase(line)){
				scan.close();
				channel.close();
				break;
			}
		}
	}
	private void keepRead(){
		try {
			while(selector.select()>0){
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while(iterator.hasNext()){
					SelectionKey next = iterator.next();
					SocketChannel channel = (SocketChannel) next.channel();
					if(next.isReadable()){
						ByteBuffer readBuff = ByteBuffer.allocate(1024);
						while(channel.read(readBuff)>0){
							//readBuff.flip();
							System.out.println(new String(readBuff.array()));
							readBuff.clear();
						}
					}
				}
				iterator.remove();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void main(String[] args) {
		WeChatClient nioClient = new WeChatClient("127.0.0.1", 9951);
		nioClient.init();
	}
}
