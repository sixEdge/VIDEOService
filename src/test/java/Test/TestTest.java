/*
 * Copyright (c) 2017 Six Edge.
 *
 * This Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package Test;

import com.alibaba.fastjson.JSONObject;
import com.gzf.video.core.dao.MongoProvider;
import com.gzf.video.core.dispatcher.ActionDispatcher;
import com.gzf.video.core.dispatcher.CustomParametersParser;
import com.gzf.video.pojo.component.enums.Level;
import com.gzf.video.pojo.component.enums.Sex;
import com.gzf.video.pojo.entry.LoginUserInfo;
import com.gzf.video.core.session.SessionManager;
import com.mongodb.async.client.MongoCollection;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import org.bson.Document;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.Collections;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class TestTest {

    @Test
    public void typeSafeConfigTest() {
        Config config = ConfigFactory.defaultApplication();
        System.out.println("appName = " + config.getString("appName"));
    }

    @Test
    public void cacheTest() {
        SessionManager sessionManager = SessionManager.getINSTANCE();

        String key   = "theSessionId",
               value = "theUserId";
        sessionManager.createLoginCache(key, value);

        String userId = sessionManager.getLoginUserIdCache(key);
        assert value.equals(userId);
    }

    @Test
    public void jsonTest() {
        LoginUserInfo loginInfo = new LoginUserInfo();
        loginInfo.setUserId(2703343);
        loginInfo.setUserName("Six Edge");
        loginInfo.setSex(Sex.BOY);
        loginInfo.setLevel(Level.LEVEL_0);
        loginInfo.setFace("http://static.hdslb.com/images/member/noface.gif");
        loginInfo.setMoney(493);

        System.out.println(JSONObject.toJSONString(loginInfo, true));
    }

    @Test
    public void mongoTest() throws InterruptedException {
        Document document = new Document();
        document.append("userId", 2703343)
                .append("username", "Six Edge")
                .append("face", "http://static.hdslb.com/images/member/noface.gif");

        MongoProvider mongoProvider = MongoProvider.getINSTANCE();
        MongoCollection<Document> collection = mongoProvider.getCollection("user");

        // insert
        collection.insertMany(Collections.singletonList(document), (result1, t1) -> {
            if (t1 != null) {
                t1.printStackTrace();
            }
            System.out.println("insert result = " + result1);


            // find
            collection.find(document).first((result2, t2) -> {
                if (t2 != null) {
                    t2.printStackTrace();
                }
                System.out.println("find result = " + result2);


                // delete
                collection.deleteOne(document, ((result3, t3) -> {
                    if (t3 != null) {
                        t3.printStackTrace();
                    }
                    System.out.println("delete result = " + result3);
                }));
            });
        });

        Thread.sleep(2000);
    }
}
