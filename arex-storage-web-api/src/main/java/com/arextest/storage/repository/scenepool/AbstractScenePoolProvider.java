package com.arextest.storage.repository.scenepool;

import com.arextest.model.scenepool.Scene;
import com.mongodb.client.MongoCollection;
import javax.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

public abstract class AbstractScenePoolProvider implements ScenePoolProvider {
  @Resource
  private MongoTemplate mongoTemplate;

  MongoTemplate getTemplate() {
    return mongoTemplate;
  }

  void setMongoDataBase(MongoTemplate mongoDatabase) {
    this.mongoTemplate = mongoDatabase;
  }

  MongoCollection<Scene> getCollection() {
    String scenePoolName = this.getProviderName() + "ScenePool";
    return getTemplate().getMongoDatabaseFactory().getMongoDatabase().getCollection(scenePoolName, Scene.class);
  }
}
