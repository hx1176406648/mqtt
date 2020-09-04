
package com.hzdsn.mqtt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.Valid;

import org.omg.CORBA.DynAnyPackage.Invalid;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Title: MqttFactory.java
 * @description:MQTT工厂类
 * @author: WQ
 * @date: 2019年9月3日
 */
@Slf4j
public class MqttFactory {

	/** 连接池最大数 */
	private static int maxPool = 10;

	/** 连接池 */
	private static LinkedHashMap<MqttConfig, MqttSender> pool = new LinkedHashMap<>(maxPool);

	/** 配置文件缓存 */
	private static Map<String, MqttConfig> configMap = new HashMap<>(16);

	/** 工厂对象 */
	private static MqttFactory mqttFactory = new MqttFactory();

	private MqttFactory() {

	}

	/**
	 * 获取MqttFactory实例
	 * 
	 * @return
	 */
	public static MqttFactory getInstance() {
		return mqttFactory;
	}

	/**
	 * 获取MQTT发送提供者
	 * 
	 * @param config MQTT配置信息
	 * @return
	 */
	public MqttSender getSender(MqttConfig config) {
		if (pool.containsKey(config)) {
			return pool.get(config);
		}
		return register(config);
	}

	/**
	 * 获取缓存配置
	 * 
	 * @param serverId
	 * @return
	 */
	public MqttConfig getConfig(String serverId) {
		return configMap.get(serverId);
	}

	/**
	 * 注册服务实例
	 * 
	 * @param config
	 * @return
	 */
	public synchronized MqttSender register(MqttConfig config) {
		MqttSender sender = null;
		while (pool.size() >= maxPool) {
			// 删除第一个元素
			Map.Entry<MqttConfig, MqttSender> entry = pool.entrySet().iterator().next();
			if (entry != null) {
				remove(entry.getKey());
			}
		}
		
		String serverId = config.getServerid();
		// 已存在旧的serverId
		if (configMap.containsKey(serverId)) {
			remove(configMap.get(serverId));
		}
		sender = new MqttSender(config);
		if (sender.isConnected()) {
			configMap.put(config.getServerid(), config);
			pool.put(config, sender);
		} else {
			sender = null;
		}
		return sender;
	}

	/**
	 * 移除服务实例
	 * 
	 * @param config
	 */
	private synchronized void remove(MqttConfig config) {
		MqttSender invalidSender = pool.get(config);
		if (invalidSender != null) {
			pool.remove(config).destory();
		}
		configMap.remove(config.getServerid());
	}

	/**
	 * 根据serverid移除服务实例
	 * 
	 * @param serverid
	 * @return
	 */
	public boolean remove(String serverid) {
		MqttConfig config = configMap.get(serverid);
		if (config != null) {
			remove(config);
			return true;
		}
		return false;
	}

}
