package se.fredsfursten.plugintools;

import org.json.simple.JSONObject;

public class PhilipsHueInfo {
	private boolean _on;
	private Long _bri;
	private Long _hue;
	private Long _sat;
	private String _effect;
	private double _x;
	private double _y;
	private Long _ct;
	private String _alert; 
	private String _colormode; 
	private boolean _reachable;


	public PhilipsHueInfo(JSONObject getResponse)
	{
		update(getResponse);
	}
	
	public void update(JSONObject getResponse)
	{
		JSONObject state = (JSONObject) getResponse.get("state");
		
		this._on = (Boolean) state.get("on");
		this._bri = (Long) state.get("bri");
		this._hue = (Long) state.get("hue");
		this._sat = (Long) state.get("sat");
		this._effect = (String) state.get("effect");
		// xy
		this._ct = (Long) state.get("ct");
		this._alert = (String) state.get("alert");
		this._colormode = (String) state.get("colormode");
		this._reachable = (Boolean) state.get("reachable");
	}
	
	public boolean isOn() { return this._on; }
	public long getBri() { return this._bri; }
	public long getHue() { return this._hue; }
	public long getSat() { return this._sat; }
}
