package entrance;

import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tool.Config;
import tool.MySQL;

public class GetSecondHandHouse extends Thread {

	private MySQL db = MySQL.getInstance();
	private String projectName, contractNo, district, area, use, floor, houseCoding, agent, releaseDate;
	private int page;

	public GetSecondHandHouse(int i) {
		page = i;
	}

	public void run() {
		try {
			System.out.println("正在采集第" + page + "页...");
			String responseData = getData(page);
			handleData(responseData);
			System.out.println("第" + page + "采集完成");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(page + "错误");
		}
	}

	public String getData(int page) {
		String responseData = "";
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				Request request = new Request.Builder()
						.url("http://ris.szpl.gov.cn/bol/EsSource.aspx?targetpage=" + page + "&zone=&tep_name=")
						.build();
				Response response = client.newCall(request).execute();
				responseData = response.body().string();
				break;
			} catch (Exception e) {
				continue;
			}
		}
		return responseData;
	}

	public void handleData(String responseData) throws SQLException {
		Document document = Jsoup.parse(responseData);
		Element table = document.select("#DataGrid1").last();
		Elements rows = table.select("tr[align=center]").next();

		for (Element row : rows) {
			projectName = row.select("td:eq(0)").text();
			contractNo = row.select("td:eq(1)").text();
			district = row.select("td:eq(2)").text();
			area = row.select("td:eq(3)").text();
			use = row.select("td:eq(4)").text();

			floor = row.select("td:eq(5)").text();
			if (floor.contains("-")) {
				floor = "0";
			}
			if (floor.equals("")) {
				floor = "0";
			}
			houseCoding = row.select("td:eq(6)").text();
			agent = row.select("td:eq(7)").text();
			releaseDate = row.select("td:eq(8)").text();
			addToDatabase();
		}

	}

	public void addToDatabase() throws SQLException {
		String fields[] = { "PROJECT_NAME", "CONTRACT_NO", "DISTRICT", "AREA", "USE", "FLOOR", "HOUSE_CODING", "AGENT",
				"RELEASE_DATE" };
		String values[] = { projectName, contractNo, district, area, use, floor, houseCoding, agent, releaseDate };

		db.insert("mm_second_hand_house", fields, values);
	}

}
