package kr.co.daehee.weather.model;

public class DataGetterSetters {
	private int hour; //시간 (hour-3) ~ hour 까지 3시간 단위를 의미
	private int day; // 0/1/2 오늘/내일/모레
	private Double temp; //현재시간 온도
	private Double tmx; //최고 기온
	private Double tmn; //최저기온
	private int sky; //하늘 상태
	private int pty; //강수 상태
	private String wfKor;
	private String wfEn;
	private int pop; //강수 확률
	private Double r12; //12시간 예상 강수량
	private Double s12; //12시간 예상 적설량
	private Double ws; //풍속
	private int reh; //습도
	
	public void setHour(int hour) {
		this.hour = hour;
	}
	
	public int getHour() {
		return hour;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	
	public String getDay() {
		if (day == 0) {
			return "오늘";
		}
		else if (day == 1) {
			return "내일";
		}
		else {
			return "모레";
		}
	}
	
	public void setTemp(Double temp) {
		this.temp = temp;
	}
	
	public Double getTemp() {
		return temp;
	}
	
	public void setTmx(Double tmx) {
		this.tmx = tmx;
	}
	
	public Double getTmx() {
		return tmx;
	}
	
	public void setTmn(Double tmn) {
		this.tmn = tmn;
	}
	
	public Double getTmn() {
		return tmn;
	}
	
	public void setSky(int Sky) {
		this.sky = sky;
	}
	
	public int getSky() {
		return sky;
	}
	
	public void setPty(int pty) {
		this.pty = pty;
	}
	
	public int getPty() {
		return pty;
	}
	
	public void setWfKor(String wfKor) {
		this.wfKor = wfKor;
	}
	
	public int getWfKor() {
		if (wfKor.equals("맑음")) {
			return 0;
		}
		else if (wfKor.equals("구름 조금")) {
			return 1;
		}
		else if (wfKor.equals("구름 많음")) {
			return 2;
		}
		else if (wfKor.equals("흐림")) {
			return 3;
		}
		else if (wfKor.equals("비")) {
			return 4;
		}
		else if (wfKor.equals("눈/비")) {
			return 5;
		}
		else if (wfKor.equals("눈")) {
			return 6;
		}
		else {
			return 0;
		}
	}
	
	public void setWfEn(String wfEn) {
		this.wfEn = wfEn;
	}
	
	public String getWfEn() {
		return wfEn;
	}
	
	public void setPop(int pop) {
		this.pop = pop;
	}
	
	public int getPop() {
		return pop;
	}
	
	public void setR12(Double r12) {
		this.r12 = r12;
	}
	
	public Double getR12() {
		return r12;
	}
	
	public void setS12(Double s12) {
		this.s12 = s12;
	}
	
	public Double getS12() {
		return s12;
	}
	
	public void setWs(Double ws) {
		this.ws = ws;
	}
	
	public Double getWs() {
		if (String.valueOf(ws).length() > 5) {
			return Double.parseDouble(String.valueOf(ws).substring(0, 4));
		}
		else {
			return ws;
		}
	}
	
	public void setReh(int reh) {
		this.reh = reh;
	}
	
	public int getReh() {
		return reh;
	}
}
