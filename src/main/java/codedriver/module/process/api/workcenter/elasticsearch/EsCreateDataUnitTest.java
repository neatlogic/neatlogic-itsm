package codedriver.module.process.api.workcenter.elasticsearch;

// import org.junit.Test;

public class EsCreateDataUnitTest {
    /*private MultiAttrsObjectPool workcenterObjectPool;
    private CountDownLatch latch = new CountDownLatch(10);
    List<String> arrayList = Arrays.asList("sucess", "pending", "running", "failded", "hanged", "accepted");
    List<String> userList = new ArrayList<String>();
    {
        MultiAttrsSearchConfig config = new MultiAttrsSearchConfig();
        config.setPoolName("test");
        config.addCluster("my-cluster", "192.168.1.122,192.168.1.123,192.168.1.124:9200");
        workcenterObjectPool = MultiAttrsSearch.getObjectPool(config);

        for (int j = 1; j <= 1000; j++) {
            userList.add("user#" + j);
        }

    }

    // @Test
    public void test() {
        MyThread thread1 = new MyThread(1);
        thread1.start();
        MyThread thread2 = new MyThread(100001);
        thread2.start();
        MyThread thread3 = new MyThread(200001);
        thread3.start();
        MyThread thread4 = new MyThread(300001);
        thread4.start();
        MyThread thread5 = new MyThread(400001);
        thread5.start();
        MyThread thread6 = new MyThread(500001);
        thread6.start();
        MyThread thread7 = new MyThread(600001);
        thread7.start();
        MyThread thread8 = new MyThread(700001);
        thread8.start();
        MyThread thread9 = new MyThread(800001);
        thread9.start();
        MyThread thread10 = new MyThread(900001);
        thread10.start();
        try {
            latch.await(); // 主线程等待
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class MyThread extends Thread {
        private Integer index;

        public MyThread(Integer index) {
            super();
            this.index = index;
        }

        public void run() {
            for (int i = index; i < index + 100000; i++) {
                JSONArray stepArray = new JSONArray();
                JSONObject stepJson = new JSONObject();
                stepJson.put("isactive", new Double(Math.random() * 10 / 2).intValue());
                stepJson.put("name", "通用节点" + new Double(Math.random() * 10 / 2).intValue());
                stepJson.put("status", arrayList.get(new Double(Math.random() * 10 / 2).intValue()));
                JSONArray usertypelist = new JSONArray();
                JSONObject userJson = new JSONObject();
                JSONArray userlist = new JSONArray();
                userlist.add(userList.get(new Double(Math.random() * 1000).intValue()));
                userJson.put("userlist", userlist);
                userJson.put("usertype", "major");
                userJson.put("usertypename", "处理人");
                usertypelist.add(userJson);
                stepJson.put("usertypelist", usertypelist);
                stepArray.add(stepJson);
                JSONObject stepJson1 = new JSONObject();
                stepJson1.put("isactive", new Double(Math.random() * 10 / 2).intValue());
                stepJson1.put("name", "通用节点" + new Double(Math.random() * 10 / 2).intValue());
                stepJson1.put("status", arrayList.get(new Double(Math.random() * 10 / 2).intValue()));
                JSONArray usertypelist1 = new JSONArray();
                JSONObject userJson1 = new JSONObject();
                JSONArray userlist1 = new JSONArray();
                userlist1.add(userList.get(new Double(Math.random() * 10 / 2).intValue()));
                userJson1.put("userlist", userlist1);
                userJson1.put("usertype", "agent");
                userJson1.put("usertypename", "代办人");
                usertypelist1.add(userJson1);
                stepJson1.put("usertypelist", usertypelist1);
                stepArray.add(stepJson1);
                String id = String.valueOf(i);
                workcenterObjectPool.checkout("local-dev");
                MultiAttrsObjectPatch patch = workcenterObjectPool.save(id);
                JSONObject WorkcenterFieldJson = new JSONObject();
                WorkcenterFieldJson.put("title", "标题" + id);
                WorkcenterFieldJson.put("status", "sucess");
                WorkcenterFieldJson.put("priority", "bff9423cd3024de79c28086f39fe8595");
                WorkcenterFieldJson.put("catalog", "52c6957150b84b5b8079b630e2a96812");
                WorkcenterFieldJson.put("channel", "8ab35196edee400f975268d82ec0a45e");
                WorkcenterFieldJson.put("channeltype", "ee41e71c6f344b929f341e6ac8cc7a21");
                WorkcenterFieldJson.put("content", "这是内容" + id);
                WorkcenterFieldJson.put("starttime", "2020-04-17 17:16:22");
                WorkcenterFieldJson.put("endtime", "");
                WorkcenterFieldJson.put("owner", "user#zsjk");
                WorkcenterFieldJson.put("reporter", "");
                WorkcenterFieldJson.put("step", stepArray);
                WorkcenterFieldJson.put("transferfromuser", Arrays.asList("user#zsjk", "user#chenqw"));
                WorkcenterFieldJson.put("worktime", "9c9a3d02ec624d179c5a541066514f7c");
                WorkcenterFieldJson.put("expiredtime", "2020-04-13 17:13:22");
                patch.set("form", new JSONObject());
                patch.set("common", WorkcenterFieldJson);
                patch.commit();
            }
            latch.countDown(); // 执行完毕，计数器减1
        }

    }*/
}
