package entrance;

import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tool.Config;
import tool.MySQL;

public class GetProjectList extends Thread {
	private MySQL db = MySQL.getInstance();
	private String bookingNum, projectName, devEnt, district, approvalTime, projectId;
	private int page;

	public GetProjectList(int page) {
		this.page = page;
	}

	public void run() {
		try {
			System.out.println("正在采集第" + page + "页...");
			String responseData = postData(page);
			analyzeHtml(responseData);
			System.out.println("第" + page + "采集完成");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("错误");
		}
	}

	public String postData(int page) {
		String responseData = "";
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient().newBuilder()
//				.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
						.build();

				RequestBody requestBody = new FormBody.Builder().add("__EVENTTARGET", "AspNetPager1")
						.add("__EVENTARGUMENT", String.valueOf(page))
						.add("__VIEWSTATE",
								"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")
						.add("__VIEWSTATEENCRYPTED", "").build();

				Request request = new Request.Builder().url("http://ris.szpl.gov.cn/bol/index.aspx").post(requestBody)
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

	public void analyzeHtml(String responseData) throws SQLException {
		Document document = Jsoup.parse(responseData);
		Element table = document.select("#DataList1").select("table[width=100%]").last();

		Elements rows = table.select("tr[bgcolor=#F5F9FC]");

		for (Element row : rows) {
			bookingNum = row.select("td:eq(1)").text();
			projectName = row.select("td:eq(2)").text();
			devEnt = row.select("td:eq(3)").text();
			district = row.select("td:eq(4)").text();
			approvalTime = row.select("td:eq(5)").text();
			if (approvalTime.equals("")) {
				approvalTime = "0000-01-01";
			}
			projectId = row.select("td:eq(1)").select("a").attr("href");
			projectId = projectId.replace("./certdetail.aspx?id=", "");
			addToDatabase();
		}
	}

	public void addToDatabase() throws SQLException {
		String fields[] = { "PROJECT_ID", "BOOKING_NUM", "PROJECT_NAME", "DEV_ENT", "DISTRICT", "APPROVAL_TIME" };
		String values[] = { projectId, bookingNum, projectName, devEnt, district, approvalTime };

		db.insert("mm_project_list", fields, values);
	}
}
