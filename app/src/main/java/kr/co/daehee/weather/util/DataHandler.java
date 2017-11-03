package kr.co.daehee.weather.util;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import kr.co.daehee.weather.model.DataGetterSetters;

public class DataHandler extends DefaultHandler {
	private String elementValue = null;
	private boolean elementOn = false;
	private ArrayList<DataGetterSetters> dataList = new ArrayList<DataGetterSetters>();
	private DataGetterSetters data = null;
	String lastUpdate = null;
	String myLocation = null;

	public ArrayList<DataGetterSetters> getData() {
		return dataList;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementOn =  true;
		if (localName.equalsIgnoreCase("data")) {
			data = new DataGetterSetters();
			Log.e("시작", "엘리먼트 <data>");
		}
	}
	
	public String strTokenize (String lastUpdate) {
		String str = lastUpdate.substring(0, 4) + "년 " + lastUpdate.substring(4, 6) + "월 " + lastUpdate.substring(6, 8) + "일 " +
				lastUpdate.substring(8, 10) + "시 " + lastUpdate.substring(10, lastUpdate.length()) + "분";
		Log.e("last updated", str);
		return str;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		elementOn = false;
		if (localName.equalsIgnoreCase("category")) {
			Log.e("category", elementValue);
			myLocation = elementValue;
		}
		else if (localName.equalsIgnoreCase("tm")) {
			Log.e("tm", elementValue);
			lastUpdate = strTokenize(elementValue);
		}
		else if (localName.equalsIgnoreCase("hour")) {
			Log.e("hour", elementValue);
			data.setHour(Integer.parseInt(elementValue));
		}
		else if (localName.equalsIgnoreCase("tm")) {
			Log.e("tm", elementValue);
			data.setHour(Integer.parseInt(elementValue));
		}
		else if (localName.equalsIgnoreCase("day")) {
			Log.e("day", elementValue);
			data.setDay(Integer.parseInt(elementValue));
		}
		else if (localName.equalsIgnoreCase("temp")) {
			Log.e("temp", elementValue);
			data.setTemp(Double.parseDouble(elementValue));
		}
		else if (localName.equalsIgnoreCase("tmx")) {
			Log.e("tmx", elementValue);
			data.setTmx(Double.parseDouble(elementValue));
		}
		else if (localName.equalsIgnoreCase("tmn")) {
			Log.e("tmn", elementValue);
			data.setTmn(Double.parseDouble(elementValue));
		}		
		else if (localName.equalsIgnoreCase("sky")) {
			Log.e("sky", elementValue);
			data.setSky(Integer.parseInt(elementValue));
		}		
		else if (localName.equalsIgnoreCase("pty")) {
			Log.e("pty", elementValue);
			data.setPty(Integer.parseInt(elementValue));
		}		
		else if (localName.equalsIgnoreCase("wfKor")) {
			Log.e("wfKor", elementValue);
			data.setWfKor(elementValue);
		}		
		else if (localName.equalsIgnoreCase("wfEn")) {
			Log.e("wfEn", elementValue);
			data.setWfEn(elementValue);
		}		
		else if (localName.equalsIgnoreCase("pop")) {
			Log.e("pop", elementValue);
			data.setPop(Integer.parseInt(elementValue));
		}		
		else if (localName.equalsIgnoreCase("r12")) {
			Log.e("r12", elementValue);
			data.setR12(Double.parseDouble(elementValue));
		}		
		else if (localName.equalsIgnoreCase("s12")) {
			Log.e("s12", elementValue);
			data.setS12(Double.parseDouble(elementValue));
		}		
		else if (localName.equalsIgnoreCase("ws")) {
			Log.e("ws", elementValue);
			data.setWs(Double.parseDouble(elementValue));
		}		
		else if (localName.equalsIgnoreCase("reh")) {
			Log.e("reh", elementValue);
			data.setReh(Integer.parseInt(elementValue));
		}		
		else if (localName.equalsIgnoreCase("data")) {
			dataList.add(data);
			data = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (elementOn) {
			elementValue = new String(ch, start, length);
			elementOn = false;
		}
	}
}
