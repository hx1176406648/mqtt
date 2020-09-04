package com.hzdsn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hzdsn.mqtt.MqttConfig;
import com.hzdsn.mqtt.MqttFactory;
import com.hzdsn.mqtt.MqttSender;

/**
 * 测试类
 * 
 * @Title: TestController.java
 * @description:
 * @author: WQ
 * @date: 2019年9月3日
 */
@RestController
@RequestMapping("/test")
public class TestController {

	@Autowired
	MqttConfig mqttConfig;

	/**
	 * 发送测试
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/send")
	public String send(String data, String topic, String serverid) {
		if (StringUtils.isEmpty(topic)) {
			topic = mqttConfig.getDefaulttopic();
		}
		MqttConfig config = null;
		if (!StringUtils.isEmpty(serverid)) {
			config = MqttFactory.getInstance().getConfig(serverid);
		} else {
			serverid = mqttConfig.getServerid();
			config = MqttFactory.getInstance().getConfig(serverid);
			if (config == null) {
				config = mqttConfig;
			}
		}
		if (config == null) {
			return "无效的serverid，若未注册，请先注册";
		}
		try {
			// 发送MQTT消息
			if (MqttFactory.getInstance().getSender(config).send(topic, data)) {
				return "发送成功";
			} else {
				return "发送失败";
			}
		} catch (Exception e) {
			return "发送失败" + e.getMessage();
		}
	}

	/**
	 * 添加服务实例
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/register")
	public String register(@RequestBody MqttConfig config) {
		try {
			MqttSender mqttSender = MqttFactory.getInstance().register(config);
			if (mqttSender != null && mqttSender.isConnected()) {
				return " 添加服务实例成功";
			}
		} catch (Exception e) {
			return "添加服务实例失败" + e.getMessage();
		}
		return "添加服务实例失败";
	}

	/**
	 * 添加服务实例
	 * 
	 * @param data
	 * @return
	 */
	@DeleteMapping("/remove/{serverid}")
	public String remove(@PathVariable String serverid) {
		try {
			if (MqttFactory.getInstance().remove(serverid)) {
				return "移除服务实例成功";
			}
		} catch (Exception e) {
			return "移除服务实例失败" + e.getMessage();
		}
		return "移除服务实例失败";
	}
}