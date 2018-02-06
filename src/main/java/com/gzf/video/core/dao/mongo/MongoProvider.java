package com.gzf.video.core.dao.mongo;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.ProjectDependent;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.*;
import com.mongodb.connection.*;
import com.mongodb.connection.netty.NettyStreamFactoryFactory;
import com.mongodb.selector.*;
import com.typesafe.config.Config;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import sun.misc.Cleaner;
import sun.nio.ch.DefaultSelectorProvider;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.gzf.video.core.ConfigManager.coreModule;
import static com.gzf.video.core.ProjectDependent.canUseEpoll;
import static com.gzf.video.core.ProjectDependent.canUseKQueue;
import static java.util.stream.Collectors.toList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoProvider {

    private static final Config MONGO_CONFIG =
            ConfigManager.loadConfigFromModule(coreModule, "mongoConfig");
    private static final List<? extends Config> dbsConfig = MONGO_CONFIG.getConfigList("dbs");
    private static final int CONNECTION_POOL_THREADS = MONGO_CONFIG.getInt("connectionPoolThreadNum");


    private static final ThreadFactory threadFactory = new DefaultThreadFactory("Thread-Factory-Mongo-Connection");
    private static final EventLoopGroup MONGO_EVENT_LOOP_GROUP = canUseEpoll()
            ? new EpollEventLoopGroup(CONNECTION_POOL_THREADS, threadFactory)
            : canUseKQueue()
               ? new KQueueEventLoopGroup(CONNECTION_POOL_THREADS, threadFactory)
               : new NioEventLoopGroup(CONNECTION_POOL_THREADS, threadFactory, DefaultSelectorProvider.create());


    private static final CodecRegistry pojoCodecRegistry =
            fromRegistries(MongoClients.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder()
                            .register(MONGO_CONFIG.getStringList("pojoPackages").toArray(new String[0]))
                            .automatic(true)
                            .build()
                    ));


    private static final MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
            .connectionPoolSettings(ConnectionPoolSettings.builder()
                    .maxSize(1024)
                    .maxWaitTime(8, TimeUnit.SECONDS)
                    .minSize(0)
                    .build()
            )
            .credentialList(dbsConfig.stream()
                    .map(c -> MongoCredential.createCredential(
                            c.getString("username"),
                            c.getString("db"),
                            c.getString("password").toCharArray()))
                    .collect(toList())
            )
            .clusterSettings(ClusterSettings.builder()
                    .hosts(MONGO_CONFIG.getStringList("hosts").stream()
                            .map(s -> s.split(":", 2))
                            .map(ss -> new ServerAddress(ss[0], Integer.parseInt(ss[1], 10)))
                            .collect(toList())
                    )
                    .mode(MONGO_CONFIG.getEnum(ClusterConnectionMode.class, "clusterConnectionMode"))
                    .requiredClusterType(MONGO_CONFIG.getEnum(ClusterType.class, "clusterType"))
                    .serverSelector(new ReadPreferenceServerSelector(ReadPreference.primary()))
                    .build()
            )
            .socketSettings(SocketSettings.builder()
                    .connectTimeout(MONGO_CONFIG.getInt("connectTimeout"), TimeUnit.SECONDS)
                    .readTimeout(MONGO_CONFIG.getInt("readTimeout"), TimeUnit.SECONDS)
                    .build()
            )
            .sslSettings(SslSettings.builder()
                    .enabled(MONGO_CONFIG.getBoolean("useSsl"))
                    .build()
            )
            .codecRegistry(pojoCodecRegistry)
            .serverSettings(ServerSettings.builder().build())
            .streamFactoryFactory(NettyStreamFactoryFactory.builder()
                    .socketChannelClass(ProjectDependent.socketChannelClass())
                    .allocator(PooledByteBufAllocator.DEFAULT)
                    .eventLoopGroup(MONGO_EVENT_LOOP_GROUP)
                    .build()
            )
            .applicationName(MONGO_CONFIG.getString("applicationName"))
            .build());


    static {
        Cleaner.create(mongoClient, mongoClient::close);
    }


    private static final MongoDatabase db_0 = mongoClient.getDatabase(dbsConfig.get(0).getString("db"));

    public static MongoDatabase getDefaultDatabase() {
        return db_0;
    }

    public static MongoDatabase getDatabase(final String db) {
        return mongoClient.getDatabase(db);
    }
}
