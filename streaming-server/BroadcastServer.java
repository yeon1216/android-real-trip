
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * ChatServer 클래스
 */
public class BroadcastServer {

    public static void main(String[] args) {

    	System.out.println("[서버 시작]");

        try {

            ServerSocket server = new ServerSocket(5050); // ServerSocket 생성
            HashMap<String, Object> hm = new HashMap<String, Object>(); // HashMap 생성
//            HashMap<Integer,String> member_hm = new HashMap<Integer,String>();

            /**
             * 클라이언크 열결 대기
             */
            while(true) {
                Socket sock = server.accept(); // 클라이언트 접속
                new ChatThread(sock, hm).start(); // 소켓과 해쉬맵으로 챗 쓰레드를 생성
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    } // main() 메소드

} // ChatServer 클래스



/**
 * ChatThread 클래스
 */
class ChatThread extends Thread{
	private String TAG = "yeon["+getClass().getSimpleName().toString()+"]"; // 로그를 위한 태그

    private Socket sock;
    private String member_no;
    private BufferedReader br;
    private PrintWriter pw;
    private HashMap<String, Object> hm;
//    private HashMap<Integer,String> member_hm;
    private ArrayList<Integer> member_list;
    private boolean quit_check = false;

    /**
     * 생성자
     * @param sock 소켓
     * @param hm 해쉬맵
     */
    public ChatThread(Socket sock, HashMap<String,Object> hm) {
        this.sock = sock;
        this.hm = hm;
    } // 생성자

    /**
     * run() 메소드
     */
    public void run() {
        try {

        	/**
        	 * PrintWriter, BufferedReader 생성
        	 */
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));

            /**
             * while문을 돌려 계속 수신 대기
             */
            while(true){

            	String request = br.readLine();

              if(request != null){
                  String[] tokens = request.split("¡"); //  request를 통해 tokens 배열 얻기

                  /**
                  * token을 통해 각종 메소드 실행
                  */
                  if("start".equals(tokens[0])){ // 방송 시작
                	  	consoleLog(TAG,tokens[1]+" 방송 시작");
	                	doJoin(tokens[1],pw); // 방송 참여 메소드
	                	member_list = new ArrayList<>(); // array list 생성
	                	member_list.add(Integer.parseInt(tokens[1])); // 멤버 추가
                  }else if("join".equals(tokens[0])) { // 멤버 입장
                	  synchronized (hm) {

	            		  consoleLog(TAG,"join : "+tokens[1]);
	                	  String[] data_arr = tokens[1].split("ㅣ");
	                	  int temp_broadcast_member_no = Integer.parseInt(data_arr[0]);
	                	  int login_member_no = Integer.parseInt(data_arr[1]);
	                	  String login_member_nickname = data_arr[2];


	//                    	  doJoin(String.valueOf(login_member_no),pw); // 방송 참여 메소드
	                	  this.member_no = String.valueOf(login_member_no);

	                  		/**
	                  		 * 이미 writer가 있다면 제거
	                  		 */
	                  		if(hm.get(this.member_no)!=null){
	                  			consoleLog(TAG,this.member_no+" writer가 이미 있음");
	                              PrintWriter remove_pw = (PrintWriter) hm.get(this.member_no);
	                              removeWriter(remove_pw);
	                          }

	                  		/**
	                  		 * writer 추가
	                  		 */
	                		consoleLog(TAG,this.member_no+" writer 추가");
	                		hm.put(this.member_no, pw);

	                  		/**
	                  		 * 이미 writer가 있다면 제거
	                  		 */
	                  		if(hm.get(String.valueOf(temp_broadcast_member_no))==null){
	                  			consoleLog(TAG,temp_broadcast_member_no+" writer가 없음");
	                          }
                		  PrintWriter pw = (PrintWriter) hm.get(String.valueOf(temp_broadcast_member_no));
            			  pw.println("join_memberㅣ"+login_member_no+"ㅣ"+login_member_nickname);
            			  pw.flush();
                	  }
                  }
                  else if("join_member_nickname".equals(tokens[0])) { // 멤버 입장
                	  consoleLog(TAG,"join_member_nickname : "+tokens[1]);
                	  String[] data_arr = tokens[1].split("ㅣ");
                	  int login_member_no = Integer.parseInt(data_arr[0]);
                	  String login_member_nickname = data_arr[1];
                	  member_list.add(login_member_no); // 멤버 리스트 추가
                	  synchronized (hm) {
                		  for (int i = 0; i < member_list.size(); i++) {
                			  PrintWriter pw = (PrintWriter) hm.get(String.valueOf(member_list.get(i)));
                			  pw.println("join_member_nicknameㅣ"+login_member_nickname);
                			  pw.flush();
                		  }
                	  }
                  }
                  else if("message".equals(tokens[0])) { // 메시지 보내기
                	  doMessage(tokens[1]); // 방송에 채팅 보내기 메소드
                  }
                  else if("quit".equals(tokens[0])) { // 멤버 나감
	                  doQuit(pw); // 방송 나감 메소드
	                  quit_check=true;
                  }
                  else if("finish".equals(tokens[0])) { // 방송 종료
//                	  broadcast("finish");
                	  synchronized (hm) {
	                	  for (int i = 0; i < member_list.size(); i++) {
								if(hm.get(String.valueOf(member_list.get(i)))==null){
									consoleLog(TAG,this.member_no+" writer 없음");
								}else{
									PrintWriter pw = (PrintWriter) hm.get(String.valueOf(member_list.get(i)));
									pw.println("finish");
									pw.flush();
									consoleLog(TAG,this.member_no+" writer 제거");
									hm.remove(String.valueOf(member_list.get(i)));
								}
	                	  }
                      }
                  }
              }else{
                consoleLog(TAG, "request null 된 멤버 번호: "+this.member_no);
                break;
              }


            } // end while

        }catch(SocketException se){
        	if(!quit_check){
        		consoleLog(TAG, "SocketException 된 멤버 번호: "+this.member_no);
        		doQuit(pw);
        	}
        }catch (IOException e) {
        	 consoleLog(TAG, "IOException 에러 생긴 멤버 번호: "+this.member_no);
        }catch (Exception e){
          consoleLog(TAG, "Exception 생긴 멤버 번호: "+this.member_no);
          consoleLog(TAG,"e: "+e.toString());
        }finally {

            try {
                if(sock != null) {
                    sock.close();
                }
                removeWriter((PrintWriter) hm.get(this.member_no));
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    } // run() 메소드

    /**
     * 방송 입장
     */
    private void doJoin(String member_no, PrintWriter pw) {
        this.member_no = member_no;
        addWriter(pw); // 방송에 클라이언트 추가 메소드 (writer pool에 저장)
    } // doJoin() 메소드


    /**
     * 채팅방에 입장한 클라이언트에게 wirter를 주는 메소드 (writer pool에 저장)
     */
    private void addWriter(PrintWriter pw) {
		/**
		 * 소켓에 들어온 멤버와 소켓 연결
		 */
    	synchronized (hm) {

    		/**
    		 * 이미 writer가 있다면 제거
    		 */
    		if(hm.get(this.member_no)!=null){
    			consoleLog(TAG,this.member_no+" writer가 이미 있음");
                PrintWriter remove_pw = (PrintWriter) hm.get(this.member_no);
                removeWriter(remove_pw);
            }

    		/**
    		 * writer 추가
    		 */
      		consoleLog(TAG,this.member_no+" writer 추가");
      		hm.put(this.member_no, pw);
        }

    } // addWriter() 메소드


    /**
     * 퇴장 메소드
     */
    private void doQuit(PrintWriter pw) {
        // consoleLog(TAG,this.member_no+" 채팅방 나감");
        removeWriter(pw);
    } // doQuit() 메소드


    /**
     * 클라이언트가 채팅방에서 나가면서 해당 클라이언트가 가지고있는 PrintWriter를 제거해줌
     */
    private void removeWriter(PrintWriter pw) {
    	synchronized (hm) {

            if(hm.get(this.member_no)==null){
                consoleLog(TAG,this.member_no+" writer 없음");
            }else{
                consoleLog(TAG,this.member_no+" writer 제거");
                hm.remove(this.member_no);
            }

        }
    } // removeWriter() 메소드

    /**
     * 메시지 작성, 전송
     */
    private void doMessage(String data) {

        consoleLog(TAG,"[doMessage] "+data);

        synchronized (hm) {

	        String[] data_arr = data.split("ㅣ");
	        String[] chat_member_no_arr = data_arr[0].split(",");

	        Set<String> set = hm.keySet();
	        Iterator<?> iterator = set.iterator();

	        /**
	         * member_no와 printwriter를 가지고 있는 hashmap을 조회하여
	         */
	        while(iterator.hasNext()) {
	           String key = (String)iterator.next();
	           for (int i = 0; i < chat_member_no_arr.length; i++) {
	        	   if(key.equals(chat_member_no_arr[i])){

						PrintWriter pw = (PrintWriter) hm.get(chat_member_no_arr[i]);
						pw.println("messageㅣ"+data);
						pw.flush();

	               }
	           }
	        }

        }

    } // doMessage() 메소드


    /**
     * 서버에서 클라이언트에게 데이터 전송 (메시지, 클라이언트 입장, 클라이언트 퇴장 등)
     */
    private void broadcast(String data) {
    	consoleLog(TAG,"[broadcast] "+data);

	      synchronized (hm) {
	          Collection<Object> collection = hm.values(); // 해쉬맵의 value를 Collection에 반환
	          Iterator<?> iter = collection.iterator(); //Collection을 순서화시킴
	          while(iter.hasNext()) {
	              PrintWriter pw = (PrintWriter)iter.next();
	              pw.println(data);
	              pw.flush();
	          }
	      }

    } // broadcast() 메소드




    /**
     * 로그찍는 메소드
     */
    private void consoleLog(String tag, String log) {
        System.out.println(tag+" : "+log);
    } // consoleLog 메소드


} // ChatThread 클래스
