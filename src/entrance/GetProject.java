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

public class GetProject extends Thread {
	private MySQL db = MySQL.getInstance();
	private String projectName, groundDate, groundPosition, transferDate, location, origin, approvalAuthority,
			contractNo, useLife, landUsePermit, houseUse, landUse, managementCost;
	private int idNum;

	public GetProject(int idNum) {
		this.idNum = idNum;
	}

	public void run() {
		try {
			String responseData = getData();
			analyzeHtml(responseData);
			addToDatabase();
			System.out.println(idNum + " ץȡ���");

//			System.out.println();
//			System.out.println("��Ŀ���ƣ�" + projectName + "    " + "�ڵغţ�" + groundDate);
//			System.out.println("�ڵ�λ�ã�" + groundPosition);
//			System.out.println("�������ڣ�" + transferDate + "    " + "��������" + location);
//			System.out.println("Ȩ����Դ��" + origin + "    " + "��׼���أ�" + approvalAuthority);
//			System.out.println("��ͬ���ֺţ�" + contractNo + "    " + "ʹ�����ޣ�" + useLife);
//			System.out.println("�õع滮���֤��" + landUsePermit);
//			System.out.println("������;��" + houseUse);
//			System.out.println("������;��" + landUse);
//			System.out.println("����ѣ�" + managementCost);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(idNum + "����");
		}
	}

	public String getData() {
		String responseData = "";
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder().url("http://ris.szpl.gov.cn/bol/projectdetail.aspx?id=" + idNum)
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

	public void analyzeHtml(String responseData) {
		Document document = Jsoup.parse(responseData);
		Element table = document.select("table[width=100%]").first();
		// Elements table = document.select("#Form1");
		// Element table = document.getElementById("loginForm")
		// System.out.println(table);

		Elements row1 = table.select("tr:eq(1)");
		projectName = row1.select("td:eq(1)").text();
		groundDate = row1.select("td:eq(3)").text();

		Elements row2 = table.select("tr:eq(2)");
		groundPosition = row2.select("td:eq(1)").text();

		Element row3 = table.select("tr:eq(3)").first();
		transferDate = row3.select("td:eq(1)").text();
		if (transferDate.equals(""))
			transferDate = "0000-01-01";
		location = row3.select("td:eq(3)").text();

		Elements row4 = table.select("tr:eq(4)");
		origin = row4.select("td:eq(1)").text();
		approvalAuthority = row4.select("td:eq(3)").text();

		Element row5 = table.select("tr:eq(5)").first();
		contractNo = row5.select("td:eq(1)").text();
		useLife = row5.select("td:eq(3)").text();
		if (useLife.equals("��"))
			useLife = "0";
		if (useLife.contains("��"))
			useLife = useLife.split("��")[0];

		Element row7 = table.select("tr:eq(7)").first();
		landUsePermit = row7.select("td:eq(1)").text();

		Elements row8 = table.select("tr:eq(8)");
		houseUse = row8.select("td:eq(1)").text();

		Element row9 = table.select("tr:eq(9)").first();
		landUse = row9.select("td:eq(1)").text();

		Element smallTable = document.select("#DataList1").first();
		if (smallTable == null) {
			smallTable = document.select("#lblDesc").first();
		}
		Element row17 = smallTable.parent().parent().previousElementSibling().previousElementSibling();
		managementCost = row17.select("td:eq(3)").text();
		managementCost = managementCost.replace("Ԫ/ƽ��", "");
		if (managementCost.equals("")) {
			managementCost = "0";
		}
	}

	public void addToDatabase() throws SQLException {
		String fields[] = { "PROJECT_NAME", "GROUND_DATE", "GROUND_POSITION", "TRANSFER_DATE", "LOCATION", "ORIGIN",
				"APPROVAL_AUTHORITY", "CONTRACT_NO", "USE_LIFE", "LAND_USE_PERMIT", "HOUSE_USE", "LAND_USE",
				"MANAGEMENT_COST" };
		String values[] = { projectName, groundDate, groundPosition, transferDate, location, origin, approvalAuthority,
				contractNo, useLife, landUsePermit, houseUse, landUse, managementCost };

		db.insert("mm_project_info", fields, values);
	}
}
