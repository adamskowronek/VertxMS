package VertxMS.VertxMS;

public class Chart {
	private String chart;
	private int userId;
	
	public Chart(String chart, int userId) {
		this.chart = chart;
		this.userId = userId;
	}
	
	public String getChart() {
		return chart;
	}
	public void setChart(String chart) {
		this.chart = chart;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int id) {
		this.userId = id;
	}

	@Override
	public String toString() {
		return "UiChart [chart=" + chart + ", userId=" + userId + "]";
	}
}
