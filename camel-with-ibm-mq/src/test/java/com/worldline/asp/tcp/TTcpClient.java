package com.worldline.asp.tcp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@ContextConfiguration(locations = { "classpath:META-INF/spring/test-context.xml"})
public class TTcpClient extends AbstractTestNGSpringContextTests {

    public static class SendTask implements Callable<String>{

        private String message;
        private TNettyClient tNettyClient;

        public SendTask(TNettyClient tNettyClient, String message) {
            this.tNettyClient = tNettyClient;
            this.message = message;
        }

        @Override
        public String call() throws Exception {
            long startTime = System.currentTimeMillis();
            tNettyClient.sendMessage(message);
            String response = tNettyClient.getResponses().take().getLeft();
            final long timeTaken = getElapsedMilliseconds(startTime, System.currentTimeMillis());
            LOGGER.info("Time:" + timeTaken + "ms" + System.lineSeparator());
            LOGGER.info("Req-Msg:" + message + System.lineSeparator());
            LOGGER.info("Res-Msg:" + response + System.lineSeparator());
            return response;
        }

    }

    public class Task implements Callable<Void> {

        private BlockingQueue<String> inMsgs;

        public Task() {
            super();
            inMsgs = tNettyClient.getRequests();
        }

        @Override
        public Void call() throws InterruptedException {
            while(true){
                String inMsg = inMsgs.poll(Integer.MAX_VALUE, TimeUnit.SECONDS);
                String origMsg = inMsg;
                //System.out.println("substr :" +inMsg);
                if(inMsg != null){
                    String serviceType = getServiceType(inMsg);
                    if("BSNMT190".equals(serviceType)){
                        //receive response
                        String stn = getStn(inMsg);

                        //check length and stn, discard
                        if(!requestsMap.containsKey(stn)){
                            throw new RuntimeException("unknown service trace number received:" + stn);
                        }

                        if(!checkLength(inMsg)){
                            throw new RuntimeException("response length is invalid:" + inMsg.length() + "msg:" + inMsg);
                        }

                    } else {
                        getStn(inMsg);

                        //check length
                        if(!checkLength(inMsg)){
                            throw new RuntimeException("request length is invalid:" + inMsg.length() + "msg:" + inMsg);
                        }

                        String resp = getRandomResponse(serviceType);
                        origMsg = origMsg.substring(3);
                        //System.out.println("substr :" +origMsg);
                        origMsg = "061"	+ origMsg;
                        System.out.println(origMsg);
                        tNettyClient.sendMessage(origMsg);
                    }
                }
            }
        }

    }

    //	private final static String FILE_190 = "C:/Users/A638267/Documents/190.txt";
    //	private final static String FILE_180 = "C:/Users/A638267/Documents/180.txt";
    //	private final static String FILE_010 = "C:/Users/A638267/Documents/010.txt";
    //
    // private final static String FILE_190 = "/request/190.txt";
    //	private final static String FILE_180 = "/request/180.txt";
    //	private final static String FILE_010 = "/request/010.txt";

    private final static Logger LOGGER = LoggerFactory.getLogger(TTcpClient.class);
    public static long getElapsedMilliseconds(Date start, Date end) {
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(end);

        long startTime = start.getTime();
        long endTime = end.getTime();
        return getElapsedMilliseconds(startTime, endTime);
    }
    public static long getElapsedMilliseconds(long startTime, long endTime) {
        return endTime - startTime;
    }

    public static Period getElapsedTime(Date start, Date end) {
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(end);

        long startTime = start.getTime();
        long endTime = end.getTime();
        return getElapsedTime(startTime, endTime);
    }

    public static  Period getElapsedTime(long startTime, long endTime) {
        Interval interval = new Interval(startTime, endTime);
        return interval.toPeriod();
    }

    public static Map<String, List<String>> initServiceRequest(){
        Map<String, List<String>> requests = Maps.newHashMap();

        //List<String> mockService1_RequestStr = Lists.newArrayList(); //BSNMT190
        // mockService1_RequestStr = processFile(FILE_190, mockService1_RequestStr);
        // requests.put("BSNMT190", mockService1_RequestStr);

        /*		List<String> mockService1_RequestStr180 = Lists.newArrayList();
		mockService1_RequestStr180 = processFile(FILE_180, mockService1_RequestStr180);
		requests.put("BSNPR180", mockService1_RequestStr180);


		List<String> mockService1_RequestStr010 = Lists.newArrayList();
		mockService1_RequestStr010 = processFile(FILE_010, mockService1_RequestStr010);
		requests.put("BSNPR010", mockService1_RequestStr010);*/

        return requests;
    }

    public static Map<String, List<String>> initServiceResponse(){
        Map<String, List<String>> mockResponse = Maps.newHashMap();
        List<String> mockService1_ResponseStr = Lists.newArrayList(); //702000
        List<String> mockService2_ResponseStr = Lists.newArrayList(); //702001
        List<String> mockService3_ResponseStr = Lists.newArrayList(); //702002
        List<String> mockService4_ResponseStr = Lists.newArrayList(); //703004
        List<String> mockService5_ResponseStr = Lists.newArrayList(); //703007
        List<String> mockService6_ResponseStr = Lists.newArrayList(); //703008
        List<String> mockService7_ResponseStr = Lists.newArrayList(); //703011
        List<String> mockService8_ResponseStr = Lists.newArrayList(); //703012
        List<String> mockService9_ResponseStr = Lists.newArrayList(); //703013

        mockService1_ResponseStr.add("060012345678901234567897030041234561234561234561234561234567890112345678901234567890060012345678901234567897030041234561234561234561234561234567890112345678901234567890");
        mockService2_ResponseStr.add("061012345678901234567897020011234561234561234561234561234567890112345678901234567890");
        mockService3_ResponseStr.add("061012345678901234567897020021234561234561234561234561234567890112345678901234567890");
        mockService4_ResponseStr.add("061012345678901234567897030041234561234561234561234561234567890112345678901234567890060012345678901234567897030041234561234561234561234561234567890112345678901234567890");
        mockService5_ResponseStr.add("061012345678901234567897030071234561234561234561234561234567890112345678901234567890");
        mockService6_ResponseStr.add("061012345678901234567897030081234561234561234561234561234567890112345678901234567890");
        mockService7_ResponseStr.add("061012345678901234567897030111234561234561234561234561234567890112345678901234567890");
        mockService8_ResponseStr.add("061000000000000000000007030110000001630341705031100151624471001516244710015162447100151624471001516244710015162447100151624471001516244710015162447");
        mockService9_ResponseStr.add("061012345678901234567897030131234561234561234561234561234567890112345678901234567890");
        mockResponse.put("702000", mockService1_ResponseStr);
        mockResponse.put("702001", mockService2_ResponseStr);
        mockResponse.put("702002", mockService3_ResponseStr);
        mockResponse.put("703004", mockService4_ResponseStr);
        mockResponse.put("703007", mockService6_ResponseStr);
        mockResponse.put("703008", mockService7_ResponseStr);
        mockResponse.put("703011", mockService8_ResponseStr);
        mockResponse.put("703012", mockService5_ResponseStr);
        mockResponse.put("703013", mockService9_ResponseStr);


        return mockResponse;
    }

    public static String testMessage(){
        int i = 0;
        int end = RandomUtils.nextInt(10, 100);
        String randomContent = "";
        while(i < end){
            randomContent = randomContent + i;
            i++;
        }
        return "BIZFUSE_" + UUID.randomUUID().toString() + randomContent;
    }

    private ExecutorService executors;

    private Map<String, List<String>> mockRequest;

    private Map<String, List<String>> mockResponse;

    private Map<String, String> requestsMap;

    @Autowired
    private TNettyClient tNettyClient;

    private boolean checkLength(String msg){
        //		return true;
    	if(true)
    		return true;
        String serviceType = getServiceType(msg);
        if("702000".equals(serviceType)){ //response
            LOGGER.info("702000 current message length:" + msg.length());
            return msg.length() == 360;
        } else if("702001".equals(serviceType)){ //request
            LOGGER.info("702001 current message length:" + msg.length());
            return msg.length() == 506;
        } else if("702002".equals(serviceType)){ //request
            LOGGER.info("702002 current message length:" + msg.length());
            return msg.length() == 1143;
        } else if("703004".equals(serviceType)){ //request
            LOGGER.info("703004 current message length:" + msg.length());
            return msg.length() == 172;
        } else if("703007".equals(serviceType)){ //request
            LOGGER.info("703007 current message length:" + msg.length());
            return msg.length() == 412;
        } else if("703008".equals(serviceType)){ //request
            LOGGER.info("703008 current message length:" + msg.length());
            return msg.length() == 436;
        }else if("703011".equals(serviceType)){ //request
            LOGGER.info("703011 current message length:" + msg.length());
            return msg.length() == 313;
        } else {
            throw new RuntimeException("unknown service type:" + serviceType);
        }
    }

    public String getRandomRequest(String serviceType){
        int index = RandomUtils.nextInt(0,mockRequest.get(serviceType).size());
        return mockRequest.get(serviceType).get(index);

    }

    public String getRandomResponse(String serviceType){
        //		int index = RandomUtils.nextInt(0, 29);
        int index = RandomUtils.nextInt(0,mockResponse.get(serviceType).size());
        return mockResponse.get(serviceType).get(index);

    }

    private String getServiceType(String reqResp){
        String serviceType = reqResp.substring(23, 29);
        return serviceType;
    }

    private String getStn(String reqResp){
        String stn = reqResp.substring(52, 59);
        return stn;
    }

    /*@Test*/
    public void infiniteSend() throws InterruptedException{
        while(true){
            tNettyClient.sendMessage("ECHO");
            //log.info(tNettyClient.getResponses().take());
        }
    }

    @BeforeClass
    public void init(){
        mockResponse = initServiceResponse();
        mockRequest = initServiceRequest();
        requestsMap = Maps.newConcurrentMap();

        executors = Executors.newFixedThreadPool(1000);
        tNettyClient.initAndConnect();
    }

    @Test
    public void mockAsccend() throws InterruptedException {
        List<Future<Void>> futures = Lists.newArrayList();
        //i = concurrent listener
       // for (int i = 0 ; i < 50 ; i ++ ){
            futures.add(executors.submit(new Task()));
       // }

        futures.forEach(f -> {
            try{
                f.get();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Test(invocationCount = 2)
    public void send() throws InterruptedException{
        List<String> serviceTypeList = Lists.newArrayList("BSNMT190");
        ConcurrentMap<String, Long> requestMap = Maps.newConcurrentMap();

        for(String serviceType : serviceTypeList){
            for(int i = 0 ; i < 10 ; i++){
                executors.execute(() -> {
                    for (int j = 0; j < 5; j++) {
                        String stn = RandomStringUtils.randomAlphanumeric(25);
                        requestMap.put(stn, new Long(System.currentTimeMillis()));
                        sendRandomRequest(stn, serviceType);
                    }
                });
            }

            for (int i = 0; i < 50; i++) {
                Pair<String, Long> response = tNettyClient.getResponses().take();
                String respStn = getStn(response.getLeft());
                Long timeTaken = response.getRight() - requestMap.get(respStn);
                System.out.print(String.format("Time Taken for request with stn:%s, %sms, exceed 30s?%s%n", respStn, timeTaken, timeTaken/1000 > 30 ? true : false));
            }
        }
    }

    //	@Test
    public void sendRandomRequest(String stn, String serviceType){
        //		int count = 0;
        //		int index = RandomUtils.nextInt(1, 3);

        //		while(true){
        String request = getRandomRequest(serviceType);
        //String stn = getStn(request);
        //replace stn
        String newRequest = request.substring(0, 52) + stn + request.substring(77, request.length());
        // requestsMap.put(stn, newRequest);
        LOGGER.info("Sending Request:" + request + "| stn:" + stn);
        tNettyClient.sendMessage("060012345678901234567897030041234561234561234561234561234567890112345678901234567890060012345678901234567897030041234561234561234561234561234567890112345678901234567890");
        //discard response

        //			count++;
        //		}
    }

    //	@Test
    public void sendTcpToAsp() throws ExecutionException, InterruptedException{
        int i = 0;
        int j = 0;
        int count = 10;
        int concurrent_count = 200;
        while(i < count){

            List<SendTask> tasks = Lists.newArrayList();
            while(j < concurrent_count){
                tasks.add(new SendTask(tNettyClient, testMessage()));
                j++;
            }
            List<Future<String>> futures = executors.invokeAll(tasks);
            futures.forEach(f -> {
                try {
                    //log.info(f.get() + System.lineSeparator());
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            });

            i++;
            j = 0;
        }
    }
}
