package com.ibm.research.kar.reeferserver.service;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonValue;

import com.ibm.research.kar.Kar;
import com.ibm.research.kar.actor.ActorRef;

public abstract class PersistentService {
    private ActorRef aref = Kar.actorRef("resthelper", "reeferservice");
    private Map<String, JsonValue> persistentData=null;
    protected JsonValue get(String key) {
        if (null == persistentData) {
            persistentData = new HashMap<>();
            persistentData.putAll(Kar.actorGetAllState(aref));
        }
        return persistentData.get(key);
      }
    
      // local utility to update local cache and persistent state
      protected JsonValue set(String key, JsonValue value) {
        if (null == persistentData) {
          persistentData = new HashMap<>();

        }
        persistentData.put(key, value);
        return Json.createValue(Kar.actorSetState(aref, key, value));
      }
}