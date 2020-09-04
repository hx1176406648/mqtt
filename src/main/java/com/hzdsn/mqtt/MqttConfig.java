package com.hzdsn.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 
 * @Title: MqttConfig.java
 * @description: MQTT配置类
 * @author: WQ
 * @date: 2019年9月3日
 */
@Component
@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttConfig {

	/** 用户名 */
	private String username;

	/** 密码 */
	private String password;

	/** 地址 */
	private String url;
	
	/** 客户端ID前缀 */
	private String prefix = "message";

	/** 默认主题 */
	private String defaulttopic = "defaultTopic";

	/** 连接池数量 */
	private String maxpool;
	
	/** 服务实例ID */
	private String serverid = "default";
	
}
