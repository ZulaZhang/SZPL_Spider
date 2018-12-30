package entrance;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tool.Config;
import tool.MySQL;
import tool.MySQL.ResultPair;

public class Entrance {
	private MySQL db = MySQL.getInstance();

	public Entrance() {
		try {
//			init();
//			getDealInfo();
//			getProjectList();
//			getProject();
			getSecondHandHouse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() throws SQLException {
		System.out.println("====== ��ʼ�� ======");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String date = df.format(System.currentTimeMillis());
		db.execute("DELETE FROM mm_deal_info WHERE DATE = '" + date + "';");
		db.execute("TRUNCATE TABLE mm_project_info;");
		db.execute("TRUNCATE TABLE mm_project_list;");
		db.execute("TRUNCATE TABLE mm_second_hand_house;");
	}

	public void getDealInfo() {
		System.out.println("====== ��ʼ�ɼ����ַ��ɽ���Ϣ ======");
		new GetDealInfo();
		System.out.println("====== ���ַ��ɽ���Ϣ�ɼ���� ======");
	}

	public void getProjectList() throws InterruptedException {
		int totalPage = getProjectListPage();
		System.out.println("====== ��ʼ�ɼ�Ԥ�ۣ�һ�֣���Դ�б� ======");
		System.out.println("�� " + totalPage + " ҳ");
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Config.THREAD_NUM);
		for (int i = 1; i <= totalPage; i++) {
			Thread t = new GetProjectList(i);
			fixedThreadPool.execute(t);
		}
		fixedThreadPool.shutdown();
		fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		System.out.println("====== Ԥ�ۣ�һ�֣���Դ�б�ɼ���� ======");
	}

	public void getProject() throws InterruptedException {
		System.out.println("====== ��ʼ�ɼ�Ԥ�ۣ�һ�֣���Դ��Ϣ ======");
		try {
			ResultPair rp = db.query("SELECT * FROM mm_project_list");
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Config.THREAD_NUM);
			while (rp.resultSet.next()) {
				String temp = rp.resultSet.getString("PROJECT_NAME");
				int projectId = rp.resultSet.getInt("PROJECT_ID");
				System.out.println(projectId + " " + temp);
				Thread t = new GetProject(projectId);
				fixedThreadPool.execute(t);
			}
			fixedThreadPool.shutdown();
			fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			rp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("====== Ԥ�ۣ�һ�֣���Դ��Ϣ�ɼ���� ======");
	}

	public void getSecondHandHouse() throws InterruptedException {
		int totalPage = getSecondHandHousePage();
		System.out.println("====== ��ʼ�ɼ����ַ�Դ��Ϣ ======");
		System.out.println("�� " + totalPage + " ҳ");
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Config.THREAD_NUM);
		for (int i = 1; i <= totalPage; i++) {
			Thread t = new GetSecondHandHouse(i);
			fixedThreadPool.execute(t);
		}
		fixedThreadPool.shutdown();
		fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		System.out.println("====== ���ַ�Դ��Ϣ�ɼ���� ======");

	}

	public int getProjectListPage() {
		String totalPage = "0";
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
						.readTimeout(20, TimeUnit.SECONDS).build();

				Request request = new Request.Builder().url("http://ris.szpl.gov.cn/bol/").build();
				Response response = client.newCall(request).execute();
				String responseData = response.body().string();

				Document document = Jsoup.parse(responseData);
				Element div = document.select(".PageInfo").first();
				totalPage = div.select("b:eq(2)").text();
				break;
			} catch (Exception e) {
				continue;
			}
		}
		return Integer.valueOf(totalPage);
	}

	public int getSecondHandHousePage() {
		int totalPage = 0;
		for (int i = 0; i < Config.MAX_TRY; i++) {
			try {
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				Request request = new Request.Builder()
						.url("http://ris.szpl.gov.cn/bol/EsSource.aspx?targetpage=1&zone=&tep_name=").build();
				Response response = client.newCall(request).execute();
				String responseData = response.body().string();

				Document document = Jsoup.parse(responseData);
				Element span = document.select(".a1").last();
				String totalMessage = span.text();
				totalMessage = totalMessage.replace("(��", "");
				totalMessage = totalMessage.replace("����¼)", "");

				totalPage = Integer.valueOf(totalMessage) / 20;
				if (Integer.valueOf(totalMessage) % 20 != 0) {
					totalPage++;
				}
				break;
			} catch (Exception e) {
				continue;
			}
		}
		return totalPage;
	}

	public static void main(String[] args) {
		new Entrance();
		// new GetSecondHandHouse(1885).start();
	}
}
