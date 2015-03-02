package se.fredsfursten.plugintools;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class PhilipsHue {

	private static WebResource _webStateResource;
	private static WebResource _webLightResource;
	private static PhilipsHueInfo _lamp;
		
	public static void on()
	{
		JSONObject json = new JSONObject();
		json.put("on", true);
		JSONArray result = put(json);
	}

	public static void off()
	{
		JSONObject json = new JSONObject();
		json.put("on", false);
		JSONArray result = put(json);
	}

	public static void changeAll(double saturation, double hue, double brightness)
	{
		update();
		if (!_lamp.isOn()) return;
		if (saturation > 1.0) saturation = 1.0;
		if (saturation < 0) saturation = 0;
		if (hue > 1.0) hue = 1.0;
		if (hue < 0) hue = 0;
		if (brightness > 1.0) brightness = 1.0;
		if (brightness < 0) brightness = 0;
		
		int sat = (int) Math.round(saturation*255);
		int h = (int) Math.round(hue*65280);
		int bri = (int) Math.round(brightness*255);
		
		changeAll(sat, h, bri);
	}

	public static void changeAll(long saturation, long hue, long brightness)
	{
		update();
		if (!_lamp.isOn()) return;
		if (saturation > 255) saturation = 255;
		if (saturation < 0) saturation = 0;
		if (brightness > 255) brightness = 255;
		if (brightness < 0) brightness = 0;
		if (hue > 65280) hue = 65280;
		if (hue < 0) hue = 0;
		
		JSONObject json = new JSONObject();
		json.put("sat", saturation);
		json.put("hue", hue);
		json.put("bri", brightness);
		JSONArray result = put(json);
	}

	public static void changeBrightness(double brightness)
	{
		update();
		if (!_lamp.isOn()) return;
		if (brightness > 1.0) brightness = 1.0;
		if (brightness < 0) brightness = 0;
		
		int bri = (int) Math.round(brightness*255);
		JSONObject json = new JSONObject();
		json.put("sat", _lamp.getSat());
		json.put("hue", _lamp.getHue());
		json.put("bri", bri);
		JSONArray result = put(json);
	}

	public static void dark()
	{
		update();
		if (!_lamp.isOn()) return;
		JSONObject json = new JSONObject();
		json.put("bri", 0);
		JSONArray result = put(json);
	}
	
	public static String getData()
	{
		return get().toString();
	}

	private static JSONArray put(JSONObject in)
	{
		JSONArray out = null;
		try {
			String input = in.toString();
			System.out.println("Input to Server .... \n" + input);

			ClientResponse response = getStateWebResource().accept("application/json")
					.put(ClientResponse.class, input);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatus());
			}

			String output = response.getEntity(String.class);

			System.out.println("Output from Server .... \n");
			System.out.println(output);

			JSONParser parser = new JSONParser();
			out = (JSONArray) parser.parse(output);
			
		} catch (Exception e) {

			e.printStackTrace();
		}
		return out;
	}

	private static JSONObject get()
	{
		JSONObject out = null;
		try {
			ClientResponse response = getLightWebResource().accept("application/json")
					.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatus());
			}

			String output = response.getEntity(String.class);

			System.out.println("Output from Server .... \n");
			System.out.println(output);

			JSONParser parser = new JSONParser();
			out = (JSONObject) parser.parse(output);
			
		} catch (Exception e) {

			e.printStackTrace();
		}
		return out;
	}

	private static WebResource getStateWebResource()
	{
		if (_webStateResource == null) {
			Client client = Client.create();

			_webStateResource = client
					.resource("http://192.168.1.91/api/minecrafthueplugin/lights/3/state");			
		}

		return _webStateResource;
	}

	private static WebResource getLightWebResource()
	{
		if (_webLightResource == null) {
			Client client = Client.create();

			_webLightResource = client
					.resource("http://192.168.1.91/api/minecrafthueplugin/lights/3");			
		}

		return _webLightResource;
	}

	private static void update()
	{
		JSONObject getResponse = get();
		if (_lamp == null) {
			_lamp = new PhilipsHueInfo(getResponse);	
		} else {
			_lamp.update(getResponse);
		}
	}

	public static PhilipsHueInfo getLamp() {
		JSONObject getResponse = get();
		return new PhilipsHueInfo(getResponse);
	}
}
