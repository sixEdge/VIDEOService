package com.gzf.video.core;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.PlatformDependent;

public class ProjectDependent {

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String OS_ARCH = System.getProperty("os.arch");

    public static final String CLASS_PATH = ProjectDependent.class.getResource("/").getPath();


    public static boolean canUseEpoll() {
        return "Linux".equals(OS_NAME) && OS_ARCH.indexOf("64") > 0;
    }

    public static EventLoopGroup newEventLoopGroup(final int nThreads) {
        return canUseEpoll()
                ? new EpollEventLoopGroup(nThreads)
                : new NioEventLoopGroup(nThreads);
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return canUseEpoll()
                ? EpollServerSocketChannel.class
                : NioServerSocketChannel.class;
    }

    public static Class<? extends SocketChannel> socketChannelClass() {
        return canUseEpoll()
                ? EpollSocketChannel.class
                : NioSocketChannel.class;
    }
}
