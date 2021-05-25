package org.example.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class MongoTest {
    public static MongoClient getClient() {
        List<ServerAddress> adds = new ArrayList<>();
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress1 = new ServerAddress("10.40.80.156", 27017);
        ServerAddress serverAddress2 = new ServerAddress("10.40.80.157", 27017);
        ServerAddress serverAddress3 = new ServerAddress("10.40.80.158", 27017);
        adds.add(serverAddress1);
        adds.add(serverAddress2);
        adds.add(serverAddress3);

        //通过连接认证获取MongoDB连接
        return new MongoClient(adds);
    }

    public static MongoDatabase getConnectionDB(String dbname, String username, String passwd) {
        List<ServerAddress> adds = new ArrayList<>();
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress1 = new ServerAddress("10.40.80.156", 27017);
        ServerAddress serverAddress2 = new ServerAddress("10.40.80.157", 27017);
        ServerAddress serverAddress3 = new ServerAddress("10.40.80.158", 27017);
        adds.add(serverAddress1);
        adds.add(serverAddress2);
        adds.add(serverAddress3);

        List<MongoCredential> credentials = new ArrayList<>();
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, dbname,
                passwd.toCharArray());
        credentials.add(mongoCredential);

        //通过连接认证获取MongoDB连接
        MongoClient mongoClient = new MongoClient(adds, credentials);

        // 连接到数据库
        return mongoClient.getDatabase(dbname);
    }

    public static void insertDoc() {
        MongoDatabase mongoDatabase = getConnectionDB("testdb", "test01", "123456");
        MongoCollection<Document> col1 = mongoDatabase.getCollection("col1");
        //创建文档
        Document document = new Document("name","张三")
                .append("sex", "男")
                .append("age", 18);

        col1.insertOne(document);
    }

    public static void deleteDoc() {
        MongoDatabase mongoDatabase = getConnectionDB("testdb", "test01", "123456");
        //获取集合
        MongoCollection<Document> collection = mongoDatabase.getCollection("col1");
        //申明删除条件
        Bson filter = Filters.eq("age",18);
        //删除与筛选器匹配的单个文档
        collection.deleteOne(filter);
    }

    public static void findDoc() {
        MongoDatabase mongoDatabase = getConnectionDB("testdb", "test01", "123456");
        //获取集合
        MongoCollection<Document> collection = mongoDatabase.getCollection("col1");
        //查找集合中的所有文档
        FindIterable findIterable = collection.find();
        MongoCursor cursor = findIterable.iterator();
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }

    }

    public static void main(String[] args) {
        // 不通过认证连接mongoDB服务
//        MongoClient mongoClient = new MongoClient("localhost", 27017);
//        insertDoc();
        findDoc();
    }
}
