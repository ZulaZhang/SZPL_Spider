package entrance;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tool.Config;
import tool.MySQL;

public class GetDealInfo {
	private MySQL db = MySQL.getInstance();
	private String businessArea, businessCount, residenceArea, residenceCount, otherArea, otherCount, officeArea,
			officeCount, summaryArea, summaryCount;

	public GetDealInfo() {
		try {
			String responseData = getData();
			analyze(responseData);
			addToDatabase();
		} catch (Exception e) {
			System.out.println("错误");
		}
	}

	public String getData() {
		String responseData = "";
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient.Builder().build();
				Request request = new Request.Builder().url("http://ris.szpl.gov.cn/credit/showcjgs/esfcjgs.aspx")
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

	public void analyze(String responseData) {
		Document document = Jsoup.parse(responseData);

		Elements table = document.select("#ctl00_ContentPlaceHolder1_clientList1");

		Elements row1 = table.select("tr:eq(1)");
		businessArea = row1.select("td:eq(1)").text();
		businessCount = row1.select("td:eq(2)").text();

		Elements row2 = table.select("tr:eq(2)");
		residenceArea = row2.select("td:eq(1)").text();
		residenceCount = row2.select("td:eq(2)").text();

		Elements row3 = table.select("tr:eq(3)");
		otherArea = row3.select("td:eq(1)").text();
		otherCount = row3.select("td:eq(2)").text();

		Elements row4 = table.select("tr:eq(4)");
		officeArea = row4.select("td:eq(1)").text();
		officeCount = row4.select("td:eq(2)").text();

		Elements row5 = table.select("tr:eq(5)");
		summaryArea = row5.select("td:eq(1)").text();
		summaryCount = row5.select("td:eq(2)").text();
	}

	public void addToDatabase() throws SQLException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String date = df.format(System.currentTimeMillis());

//		String sql = "INSERT INTO mm_deal_info (`DATE`, `TYPE`, `AREA`, `COUNT`) VALUES ('2018-12-26','商用','"
//		+ businessArea + "','" + businessCount + "');";

//		String sql1 = String.format(
//				"INSERT INTO mm_deal_info (`DATE`, `TYPE`, `AREA`, `COUNT`) VALUES ('%s','商用' ,'%s','%s');", date,
//				businessArea, businessCount);
//		db.execute(sql1);

		String fields[] = { "DATE", "TYPE", "AREA", "COUNT" };
		String values1[] = { date, "商用", businessArea, businessCount };
		db.insert("mm_deal_info", fields, values1);

		String values2[] = { date, "住宅", residenceArea, residenceCount };
		db.insert("mm_deal_info", fields, values2);

		String values3[] = { date, "其他", otherArea, otherCount };
		db.insert("mm_deal_info", fields, values3);

		String values4[] = { date, "办公", officeArea, officeCount };
		db.insert("mm_deal_info", fields, values4);

		String values5[] = { date, "小计", summaryArea, summaryCount };
		db.insert("mm_deal_info", fields, values5);
	}

}
