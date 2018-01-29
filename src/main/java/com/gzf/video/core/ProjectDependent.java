package com.gzf.video.core;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ProjectDependent {

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String OS_ARCH = System.getProperty("os.arch");

    public static final String CLASSPATH = ProjectDependent.class.getResource("/").getPath();


    public static boolean canUseEpoll() {
        return Epoll.isAvailable();
    }

    public static boolean canUseKQueue() {
        return KQueue.isAvailable();
    }

    public static EventLoopGroup newEventLoopGroup(final int nThreads) {
        return canUseEpoll()
                ? new EpollEventLoopGroup(nThreads)
                : canUseKQueue()
                    ? new KQueueEventLoopGroup(nThreads)
                    : new NioEventLoopGroup(nThreads);
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return canUseEpoll()
                ? EpollServerSocketChannel.class
                : canUseKQueue()
                    ? KQueueServerSocketChannel.class
                    : NioServerSocketChannel.class;
    }

    public static Class<? extends SocketChannel> socketChannelClass() {
        return canUseEpoll()
                ? EpollSocketChannel.class
                : canUseKQueue()
                    ? KQueueSocketChannel.class
                    : NioSocketChannel.class;
    }
}
