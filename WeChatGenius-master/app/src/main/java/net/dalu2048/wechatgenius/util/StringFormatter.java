package net.dalu2048.wechatgenius.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class StringFormatter {

	/**
	 * json串排版
	 *
	 * @param uglyJSONString
	 * @return
	 */
	public static String jsonFormatter(String uglyJSONString) {
		String prettyJsonStr2 = uglyJSONString;
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(uglyJSONString);
			prettyJsonStr2 = gson.toJson(je);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prettyJsonStr2;
	}
}
