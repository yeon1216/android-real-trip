<?php
// 데이터베이스 정보입력
$db_host="localhost";
$db_user="root";
$db_password="";
$db_name="trip";

// 데이터베이스 연결
$con=mysqli_connect($db_host,$db_user,$db_password,$db_name);

// 연결에 실패할 경우 예외처리
if(!$con){ die("연결 실패 : ".mysqli_connect_error()); }

/**
 * 어떤 요청인지 확인
 * - jsonObject 또는 jsonArray인 경우에는 디코드해주기
 */
$data = '';
$data_arr = '';
if(substr(file_get_contents("php://input"),0,1)=='['){ // jsonArray
    // echo file_get_contents("php://input");
    $data_arr = json_decode(file_get_contents("php://input"));
}else{ //jsonObject
    // echo file_get_contents("php://input");
    $data = json_decode(file_get_contents("php://input"));
}

/**
 * 로그인 요청
 */
if(($_POST['mode'])=='login_action'){

	$id = $_POST['id'];
    $pw = $_POST['pw'];

	$sql = "SELECT * FROM member WHERE member_email='$id'";
	$result = mysqli_query($con,$sql);
    $login_member = mysqli_fetch_assoc($result);
    $login_member_no = $login_member['member_no'];
    if(!$login_member['member_email'] || !($pw === $login_member['member_pw'])){ // 로그인 실패
        echo -1;
    }else{ // 로그인 성공
        echo $login_member_no;
	}
}

/**
 * 회원가입 요청
 */
if(($_POST['mode'])=='join_action'){
    $email = $_POST['email'];
    $nickname = $_POST['nickname'];
    $password = $_POST['password'];

    if (!mysqli_query($con, "INSERT INTO member (member_email, member_pw, member_nickname)
                                VALUES ('$email','$password','$nickname');")) {
        echo "회원가입 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo "join_success";
    }
}

/**
 * 닉네임 중복 검사
 */
if(($_POST['mode']=='nickname_duplicate_check')){
    $nickname = $_POST['nickname'];
    $sql = "SELECT count(*) c FROM member WHERE member_nickname='$nickname';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);
    if($row['c']==0){ // 사용가능한 닉네임
        echo 0;
    }else{ // 이미 사용중인 닉네임
        echo -1;
    }
}

/**
 * 이메일 중복 검사, 인증번호 생성
 * (이건 회원가입 화면에서 보내는 요청을 받는 부분)
 */
if(($_POST['mode']=='email_check')){
    // echo '11111    ';
    $email = $_POST['email'];
    $sql = "SELECT count(*) c FROM member WHERE member_email='$email';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);

    if($row['c']==0){ // 사용가능한 이메일
        // echo '22222    ';
        /**
         * email_certification 테이블에 요청받은 이메일이 있는지 없는지 여부 확인
         */
        $sql = "SELECT count(*) c FROM email_certification WHERE email='$email';";
        $result = mysqli_query($con,$sql);
        $row = mysqli_fetch_assoc($result);

        /**
         * 인증번호 생성
         */
        $certification_no='';
        for($i=0;$i<4;$i++){
            $certification_no = $certification_no.rand(0,9);
        }

        /**
         * email_certification 테이블에 인증번호 INSERT 또는 UPDATE
         */
        if($row['c']==0){ // email_certification 테이블에 해당 메일이 없으므로 INSERT
            if (!mysqli_query($con, "INSERT INTO email_certification (email, certification_no)
                                        VALUES ('$email',$certification_no);")) { // 인증번호 디비에 저장 실패
                echo "인증번호 생성 실패 (Error: ".mysqli_error($con).")"; // 에러 응답
            }else{ // 인증번호 디비에 저장 성공, 메일 전송
                include_once('function.php'); // mailer함수가 있는 function.php 파일 include
                mailer('REAL TRIP','qwse8770@naver.com',$email,'[REAL TRIP] 인증번호를 입력해주세요','인증번호: '.$certification_no); // 메일 전송
                echo 0; // 0 응답
            }
        }else{ // email_certification 테이블에 해당 메일이 있으므로 UPDATE
            if (!mysqli_query($con, "UPDATE email_certification
                                        SET certification_no='$certification_no' WHERE email='$email'")) { // 인증번호 디비에 수정 실패
                echo "인증번호 생성 실패 (Error: ".mysqli_error($con).")";
            }else{ // 인증번호 디비에 수정 성공, 메일 전송
                include_once('function.php'); // mailer함수가 있는 function.php 파일 include
                mailer('REAL TRIP','qwse8770@naver.com',$email,'[REAL TRIP] 인증번호를 입력해주세요','인증번호: '.$certification_no); // 메일 전송
                echo 0; // 0 응답
            }
        }

    }else{ // 이미 사용중인 이메일
        echo -1; // -1 응답
    }
}

/**
 * 이메일이 있는지 없는지 여부, 인증번호 생성
 * (이건 비밀번호 수정 화면에서 보내는 요청을 받는 부분)
 */
if($_POST['mode']=='is_email'){



    /**
     * 요청 인자 받기
     */
    $email = $_POST['email'];
    // echo "email: ".$email;

    /**
     * 인증번호 생성
     */
    $certification_no='';
    for($i=0;$i<4;$i++){
        $certification_no = $certification_no.rand(0,9);
    }

    /**
     * member 테이블에 요청받은 이메일이 있는지 없는지 판단하기위한 sql query
     */
    $sql = "SELECT count(*) c FROM member WHERE member_email='$email';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);

    /**
     * member 테이블에 이메일이 있는지 없는지 여부에 따라 응답
     */
    if($row['c']==0){ // 가입되어있지 않은 이메일 입력

        echo -1; // -1 응답

    }else{ // 가입된 이메일 입력 -->> 인증번호 생성

        /**
         * email_certification 테이블에 인증번호 수정
         */
        if (!mysqli_query($con, "UPDATE email_certification
                                    SET certification_no='$certification_no' WHERE email='$email'")) { // 디비의 email_certification 테이블에 인증번호 수정 실패

            echo "인증번호 생성 실패 (Error: ".mysqli_error($con).")"; // 에러 응답

        }else{ // 디비의 email_certification 테이블에 인증번호 수정 성공

            include_once('function.php'); // mailer함수가 있는 function.php 파일 include
            mailer('REAL TRIP','qwse8770@naver.com',$email,'[REAL TRIP] 인증번호를 입력해주세요','인증번호: '.$certification_no); // 메일 전송
            echo 0; // 0 응답

        }

    }

}

/**
 * 인증번호 확인 요청
 */
if($_POST['mode']=='certification_no_check'){
    $email = $_POST['email'];
    $certification_no=$_POST['certification_no'];

    $sql = "SELECT count(*) c FROM email_certification WHERE email='$email' AND certification_no='$certification_no'";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);
    if($row['c']==0){ // 올바르지 않은 인증번호 입력
        echo -1;
    }else{ // 올바른 인증번호 입력
        echo 0;
        mysqli_query($con, "UPDATE email_certification SET certification_no=0 WHERE email='$email'");
    }
}

/**
 * 비밀번호 수정 요청
 */
if($_POST['mode']=='update_pass'){
    $email = $_POST['email'];
    $password=$_POST['password'];

    if (!mysqli_query($con, "UPDATE member
                                SET member_pw='$password' WHERE member_email='$email'")) {
        echo "비밀번호 수정 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }
}

/**
 * 닉네임 수정 요청
 */
if($_POST['mode']=='update_nickname'){
    $email = $_POST['email'];
    $nickname=$_POST['nickname'];

    if (!mysqli_query($con, "UPDATE member
                                SET member_nickname='$nickname' WHERE member_email='$email'")) {
        echo "닉네임 수정 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }
}

/**
 * 기본 프로필 적용 요청
 */
if($_POST['mode']=='apply_default_profile_img'){
    $email = $_POST['email'];

    if (!mysqli_query($con, "UPDATE member
                                SET member_profile_img='default' WHERE member_email='$email'")) {
        echo "기본 프로필 적용 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }
}

/**
 * 로그인 멤버의 정보를 얻는 요청 (JSONObject)
 */
if($data->mode=='find_login_member'){
    $login_member_no = $data->login_member_no;

    $sql = "SELECT * FROM member WHERE member_no='$login_member_no';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);

    $member_no = $row['member_no']; // 멤버 번호
    $member_email = $row['member_email']; // 멤버 이메일
    $member_nickname = $row['member_nickname']; // 멤버 닉네임
    $member_profile_img = $row['member_profile_img']; // 멤버 프로필 이미지

    $member = ['member_no'=>$member_no,'member_email'=>$member_email,'member_nickname'=>$member_nickname, 'member_profile_img'=>$member_profile_img];
    // header('Content-type: application/json;charset=utf-8');
    echo json_encode($member);
}

/**
 * 게시글 리스트 받는 요청 (JSONArray)
 */
if($data_arr[0]->mode=='get_board_list'){
    $boards=[];
    $sql ="SELECT * FROM board;";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)>0){
        while($board = mysqli_fetch_array($result)){
            /**
             * 게시글 정보 가지고 오기
             */
            $board_no = $board['board_no'];
            $board_write_member_no = $board['board_write_member_no'];
            $board_title = $board['board_title'];
            $board_content = $board['board_content'];
            $board_write_time = $board['board_write_time'];

            $temp_board = ['board_no'=>$board_no, 'board_write_member_no'=>$board_write_member_no,
                            'board_content'=>$board_content, 'board_write_time'=>$board_write_time];

            array_push($boards,$temp_board);


            /**
             * 게시글 이미지 가지고 오기
             */
            $find_board_img_sql = "SELECT * FROM board_img WHERE board_no='$board_no';";
            $find_board_img_result = mysqli_query($con,$find_board_img_sql);
            $find_board_img_row = mysqli_fetch_assoc($find_board_img_result);

            /**
             * 게시글 태그 가지고 오기
             */


             /**
              * 게시글 좋아요 갯수 가지고 오기
              */

              /**
               * 게시글 댓글 가지고 오기
               */
        } // end while
    }else{

    }

    echo json_encode($boards);
}

/**
 * 로그인 멤버의 정보를 얻는 요청 (JSONObject)
 */
if($data->mode=='find_member'){
    /**
     * 요청 인자 받기
     */
    $member_no = $data->member_no;

    /**
     * SQL문 시작
     */
    $sql = "SELECT * FROM member WHERE member_no='$member_no';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);

    $member_no = $row['member_no']; // 멤버 번호
    $member_email = $row['member_email']; // 멤버 이메일
    $member_nickname = $row['member_nickname']; // 멤버 닉네임
    $member_profile_img = $row['member_profile_img']; // 멤버 프로필 이미지

    $member = ['member_no'=>$member_no,'member_email'=>$member_email,'member_nickname'=>$member_nickname, 'member_profile_img'=>$member_profile_img];
    // header('Content-type: application/json;charset=utf-8');
    echo json_encode($member);
}

/**
 * 채팅 상대를 찾기위한 요청
 */
if($data->mode=='find_member_by_member_nickname'){
    /**
     * 요청 인자 받기
     */
    $member_nickname = $data->member_nickname;


    /**
     * SQL문 시작
     */
     $sql = "SELECT count(*) c FROM member WHERE member_nickname='$member_nickname';";
     $result = mysqli_query($con,$sql);
     $row = mysqli_fetch_assoc($result);
     if($row['c']==0){ // 해당 닉네임을 가진 멤버가 없음
         $response = ['member_email'=>'no'];
         echo json_encode($response);
     }else{ // 해당 닉네임을 가진 멤버가 있음
       $sql = "SELECT * FROM member WHERE member_nickname='$member_nickname';";
       $result = mysqli_query($con,$sql);
       $row = mysqli_fetch_assoc($result);

       $member_no = $row['member_no']; // 멤버 번호
       $member_email = $row['member_email']; // 멤버 이메일
       $member_nickname = $row['member_nickname']; // 멤버 닉네임
       $member_profile_img = $row['member_profile_img']; // 멤버 프로필 이미지

       $member = ['member_no'=>$member_no,'member_email'=>$member_email,'member_nickname'=>$member_nickname, 'member_profile_img'=>$member_profile_img];
       // header('Content-type: application/json;charset=utf-8');
       echo json_encode($member);
     }


}

/**
 * 게시글 작성 요청 (StringRequest))
 */
if($_POST['mode']=='write_board'){
    /**
     * 요청 인자 받기
     */
    $board_content = $_POST['board_content'];
    $board_write_member_no = $_POST['board_write_member_no'];

    if (!mysqli_query($con, "INSERT INTO board (board_write_member_no, board_content)
                                 VALUES ('$board_write_member_no','$board_content');")) {
        echo "게시글 작성 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }
}

/**
 * 프로필 사진 수정
 */
if($_POST['mode']=='update_profile_img'){

    include_once('upload.php');
    $login_member_no = $_POST['login_member_no'];

    $upload_file = explode('/',$upload_file);

    $upload_file = $upload_file[3];
    if (!mysqli_query($con, "UPDATE member SET member_profile_img='$upload_file' WHERE member_no=$login_member_no")) { // 프로필 사진 수정 실패
        echo "-1/프로필 사진 수정 실패 (Error: ".mysqli_error($con).")";
    }else{ // 프로필 사진 수정 성공
        echo 0;
        echo '/';
        echo $upload_file;
    }

}

/**
 * 리뷰 작성 요청 (StringRequest)
 */
if($_POST['mode']=='write_review'){
    /**
     * 요청 인자 받기
     */
    $content_id = $_POST['content_id'];
    $review_content = $_POST['review_content'];
    $review_score = $_POST['review_score'];
    $review_magnitude = $_POST['review_magnitude'];
    $review_write_member_no = $_POST['review_write_member_no'];

    /**
     * 이미지 업로드
     */
    $img_file_name_arr = $_POST['img_file_name_arr'];
    $img_file_arr = $_POST['img_file_arr'];

    $img_file_name_arr = explode('|',$img_file_name_arr);
    $img_file_arr = explode('|',$img_file_arr);
    $img_count = count($img_file_arr);

    $review_img = '';

    for($i=0;$i<$img_count;$i++){
        $temp_img_file = $img_file_arr[$i];
        $temp_img_file_name = $img_file_name_arr[$i];
        $decoded_imgage = base64_decode("$temp_img_file");
        $file_upload_result = file_put_contents("./uploads/".$temp_img_file_name.".jpg",$decoded_imgage);
        if($file_upload_result === false){
            echo "리뷰 작성 실패 (Error: 파일업로드 실패)";
            exit;
        }

        if($img_count-1==$i){
            $review_img = $review_img.$img_file_name_arr[$i].'.jpg';
        }else{
            $review_img = $review_img.$img_file_name_arr[$i].'.jpg|';
        }

    }

    if($_POST['img_file_name_arr']==''){
        $review_img = '';
    }

    if (!mysqli_query($con, "INSERT INTO review (review_write_member_no, review_content, content_id, review_score, review_magnitude, review_img)
                                    VALUES ('$review_write_member_no','$review_content','$content_id','$review_score','$review_magnitude', '$review_img');")) {
        echo "리뷰 작성 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }

}

/**
 * 리뷰 리스트 받는 요청 (JSONArray)
 */
if($data_arr[0]->mode=='get_review_list'){

    $content_id = $data_arr[0]->content_id;

    $reviews=[];
    $sql ="SELECT * FROM review WHERE content_id='$content_id';";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)>0){
        while($review = mysqli_fetch_array($result)){

            /**
             * 리뷰 정보 가지고 오기
             */
            $review_no = $review['review_no'];
            $review_write_member_no = $review['review_write_member_no'];
            $review_content = $review['review_content'];
            $review_write_time = $review['review_write_time'];
            $review_img = $review['review_img'];
            $review_score = $review['review_score'];
            $review_magnitude = $review['review_magnitude'];

            $content_id = $review['content_id'];

            /**
             * review_write_member_no로 멤버의 정보 찾기
             */
            $find_member_sql = "SELECT * FROM member WHERE member_no='$review_write_member_no';";
            $find_member_result = mysqli_query($con,$find_member_sql);
            $find_member_row = mysqli_fetch_assoc($find_member_result);

            $member_nickname = $find_member_row['member_nickname'];
            $member_profile_img = $find_member_row['member_profile_img'];

            $temp_review_item = ['review_no'=>$review_no, 'content_id'=>$content_id, 'review_write_member_no'=>$review_write_member_no,
                            'review_content'=>$review_content, 'review_write_time'=>$review_write_time, 'review_img'=>$review_img,
                            'review_score'=>$review_score, 'review_magnitude'=>$review_magnitude,
                            'member_nickname'=>$member_nickname, 'member_profile_img'=>$member_profile_img];

            array_push($reviews,$temp_review_item);

        } // end while
    }else{

    }

    echo json_encode($reviews);
}

/**
 * 리뷰 삭제 요청 (StringRequest)
 */
if($_POST['mode']=='remove_review'){

    $review_no = $_POST['review_no'];

    if (!mysqli_query($con, "DELETE FROM review WHERE review_no='$review_no';")) {
        echo "리뷰 삭제 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }

}

/**
 * 리뷰 수정 요청 (StringRequest)
 */
if($_POST['mode']=='update_review'){

    /**
     * 요청 인자 받기
     */
    $review_no = $_POST['review_no'];
    $content_id = $_POST['content_id'];
    $review_content = $_POST['review_content'];
    $review_score = $_POST['review_score'];
    $review_magnitude = $_POST['review_magnitude'];
    $review_write_member_no = $_POST['review_write_member_no'];

    /**
     * 이미지 관련 요청 인자 받기
     */
    $img_file_name_arr = $_POST['img_file_name_arr'];
    $img_file_arr = $_POST['img_file_arr'];

    /**
     * 이미지 문자열 나누기
     */
    $img_file_name_arr = explode('|',$img_file_name_arr);
    $img_file_arr = explode('|',$img_file_arr);

    $review_img = '';

    /**
     * 이미지 업로드
     */
    for($i=0;$i<count($img_file_arr);$i++){
        $temp_img_file = $img_file_arr[$i];
        if($temp_img_file == 'no'){ // 서버에 이미 업로드 된 이미지
            if(count($img_file_arr)-1==$i){
                $review_img = $review_img.$img_file_name_arr[$i];
            }else{
                $review_img = $review_img.$img_file_name_arr[$i].'|';
            }
        }else{ // 서버에 업로드 해야하는 이미지
            $decoded_imgage = base64_decode("$temp_img_file");
            $file_upload_result = file_put_contents("./uploads/".$img_file_name_arr[$i].".jpg",$decoded_imgage);
            if($file_upload_result === false){
                echo "리뷰 작성 실패 (Error: 파일업로드 실패)";
                exit;
            }

            if(count($img_file_arr)-1==$i){
                $review_img = $review_img.$img_file_name_arr[$i].'.jpg';
                // $review_img = $review_img.$img_file_name_arr[$i].'하하하하핳하ㅏㅎ';
            }else{
                $review_img = $review_img.$img_file_name_arr[$i].'.jpg|';
            }
        }

    }

    if($_POST['img_file_name_arr']==''){
        $review_img = '';
    }

    if (!mysqli_query($con, "UPDATE review SET review_content='$review_content', review_score='$review_score',
                                review_magnitude='$review_magnitude', review_img='$review_img'
                                WHERE review_no='$review_no';")) {
        echo "리뷰 작성 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }

}

/**
 * chat_insert (StringRequest)
 * 채팅 업로드 (+ 채팅방이 없을 경우에는 채팅방 생성)
 */
if($_POST['mode']=='chat_insert'){
    $chat_room_name = $_POST['chat_room_name'];
    $chat_member_no = $_POST['chat_member_no'];
    $chat_content = $_POST['chat_content'];
    $chat_time = $_POST['chat_time'];

    /**
     * 채팅방이 있는지 없는지 확인
     */
    $sql = "SELECT count(*) c FROM chat_room WHERE chat_member_no_arr='$chat_room_name';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);
    if($row['c']==0){ // 현재 채팅방이 없으므로 채팅방을 만들어야함
        if (!mysqli_query($con, "INSERT INTO chat_room (chat_member_no_arr, last_chat_content, last_chat_time)
                                                      VALUES('$chat_room_name','$chat_content','$chat_time');")) {
            echo "채팅방 생성 실패 (Error: ".mysqli_error($con).")";
            exit;
        }
    }

    if (!mysqli_query($con, "INSERT INTO chat (chat_room_name, chat_member_no, chat_content, chat_time)
                                    VALUES ('$chat_room_name','$chat_member_no','$chat_content','$chat_time');")) {
        echo "채팅 업로드 실패 (Error: ".mysqli_error($con).")";
    }else{
        if (!mysqli_query($con, "UPDATE chat_room
                                    SET last_chat_content='$chat_content', last_chat_time='$chat_time' WHERE chat_member_no_arr='$chat_room_name';")) {
            echo "채팅방 마지막 말/시간 수정 실패 (Error: ".mysqli_error($con).")";
        }else{
            echo 0;
        }
    }

}


/**
 * 채팅방의 채팅을 받는 메소드 (JSONArray)
 */
if($data_arr[0]->mode=='get_chat_this_chat_room'){

    $this_chat_room_name = $data_arr[0]->this_chat_room_name;
    $login_member_no = $data_arr[0]->login_member_no;
    $login_member_nickname = $data_arr[0]->login_member_nickname;
    $login_member_profile_img = $data_arr[0]->login_member_profile_img;
    $chat_member_nickname = $data_arr[0]->chat_member_nickname;
    $chat_member_profile_img = $data_arr[0]->chat_member_profile_img;



    $chats=[];
    $sql ="SELECT * FROM chat WHERE chat_room_name ='$this_chat_room_name' ORDER BY chat_no;";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)>0){
        while($chat = mysqli_fetch_array($result)){

            /**
             * 채팅 정보 가지고 오기
             */
            $chat_no = $chat['chat_no'];
            $chat_room_name = $chat['chat_room_name'];
            $chat_member_no = $chat['chat_member_no'];
            $chat_content = $chat['chat_content'];
            $chat_time = $chat['chat_time'];

            if($login_member_no==$chat_member_no){ // 로그인한 멤버의 채팅인 경우
                $temp_chat = ['chat_no'=>$chat_no, 'chat_room_name'=>$chat_room_name, 'chat_member_no'=>$chat_member_no, 'chat_content'=>$chat_content, 'chat_time'=>$chat_time,
                                                    'member_nickname'=>$login_member_nickname, 'member_profile_img'=>$login_member_profile_img];
                array_push($chats,$temp_chat);
            }else{
                $temp_chat = ['chat_no'=>$chat_no, 'chat_room_name'=>$chat_room_name, 'chat_member_no'=>$chat_member_no, 'chat_content'=>$chat_content, 'chat_time'=>$chat_time,
                                                    'member_nickname'=>$chat_member_nickname, 'member_profile_img'=>$chat_member_profile_img];
                array_push($chats,$temp_chat);
            }

        } // end while
    }else{

    }
    echo json_encode($chats);
}


/**
 * 채팅방을 받는 메소드 (JSONArray)
 */
if($data_arr[0]->mode=='get_chat_room'){

    $login_member_no = $data_arr[0]->login_member_no;

    $chatRooms=[];
    $sql ="SELECT * FROM chat_room;";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)>0){
        while($chat_room = mysqli_fetch_array($result)){
            /**
             * 채팅 정보 가지고 오기
             */
            $chat_room_no = $chat_room['chat_room_no'];
            $chat_room_name = $chat_room['chat_member_no_arr'];
            $last_chat_content = $chat_room['last_chat_content'];
            $last_chat_time = $chat_room['last_chat_time'];

            $chat_member_no_arr = explode(',',$chat_room_name);

            $chat_member_no = null;

            if($login_member_no==$chat_member_no_arr[0]){
                $chat_member_no = $chat_member_no_arr[1];
            }else if($login_member_no==$chat_member_no_arr[1]){
                $chat_member_no = $chat_member_no_arr[0];
            }

            if($chat_member_no != null){
                $find_member_sql = "SELECT * FROM member WHERE member_no = $chat_member_no;";
                $find_member_result = mysqli_query($con,$find_member_sql);
                $member = mysqli_fetch_assoc($find_member_result);
                $member_nickname = $member['member_nickname'];
                $member_profile_img = $member['member_profile_img'];

                $temp_chat_room = ['chat_room_no'=>$chat_room_no, 'chat_room_name'=>$chat_room_name, 'last_chat_content'=>$last_chat_content, 'last_chat_time'=>$last_chat_time,
                                    'member_nickname'=>$member_nickname, 'member_profile_img'=>$member_profile_img];

                array_push($chatRooms,$temp_chat_room);
            }

        } // end while
    }else{

    }
    echo json_encode($chatRooms);
}

/**
 * add_broadcast_room (StringRequest)
 * 방송 방 추가
 */
if($_POST['mode']=='add_broadcast_room'){

    $broadcast_room_title = $_POST['broadcast_room_title'];
    $broadcast_member_no = $_POST['broadcast_member_no'];
    // $broadcast_member_nickname = $_POST['broadcast_member_nickname'];
    // $broadcast_member_profile_img = $_POST['broadcast_member_profile_img'];

    if (!mysqli_query($con, "INSERT INTO broadcast_room (broadcast_room_title, broadcast_member_no)
                                                  VALUES('$broadcast_room_title','$broadcast_member_no');")) {
        echo "방송방 생성 실패 (Error: ".mysqli_error($con).")";
    }else{
      echo 0;
    }

}

/**
 * exit_broadcast (StringRequest)
 * 방송 종료
 */
if($_POST['mode']=='exit_broadcast'){

    $broadcast_member_no = $_POST['broadcast_member_no'];

    if (!mysqli_query($con, "UPDATE broadcast_room
                                SET is_live='exit' WHERE broadcast_member_no='$broadcast_member_no';")) {
        echo "방송종료 실패 (Error: ".mysqli_error($con).")";
    }else{
        echo 0;
    }

}

/**
 * 방송리스트를 받는 메소드 (JSONArray)
 */
if($data_arr[0]->mode=='get_broadcast_list'){

    $broadcastRooms=[];

    $sql ="SELECT * FROM broadcast_room;";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)>0){
        while($broadcast_room = mysqli_fetch_array($result)){
            /**
             * 방송 정보 가지고 오기
             */
            $broadcast_room_no = $broadcast_room['broadcast_room_no'];
            $broadcast_room_title = $broadcast_room['broadcast_room_title'];
            $broadcast_member_no = $broadcast_room['broadcast_member_no'];
            $is_live = $broadcast_room['is_live'];

            /**
             * review_write_member_no로 멤버의 정보 찾기
             */
            $find_member_sql = "SELECT * FROM member WHERE member_no='$broadcast_member_no';";
            $find_member_result = mysqli_query($con,$find_member_sql);
            $find_member_row = mysqli_fetch_assoc($find_member_result);

            $broadcast_member_nickname = $find_member_row['member_nickname'];
            $broadcast_member_profile_img = $find_member_row['member_profile_img'];

            $temp_broadcast_room = ['broadcast_room_no'=>$broadcast_room_no, 'broadcast_room_title'=>$broadcast_room_title, 'broadcast_member_no'=>$broadcast_member_no,
                                'broadcast_member_nickname'=>$broadcast_member_nickname,'broadcast_member_profile_img'=>$broadcast_member_profile_img, 'is_live'=>$is_live];

            array_push($broadcastRooms,$temp_broadcast_room);

        } // end while
    }else{

    }
    echo json_encode($broadcastRooms);
}


/**
 * 테스트용 (get)
 */
if($_GET['mode']=='test'){
    echo 1;
    $sql = "SELECT * FROM member WHERE member_no=7;";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);
    echo $row['member_profile_img'];
}

/**
 * 테스트용 (post)
 */
if($_POST['mode']=='test'){
    $date = $_POST['date'];
    $menu = $_POST['menu'];
    $day = $_POST['day'];
    $lunch = ['date'=>$date, 'menu'=>$menu, 'day'=>$day];
    echo json_encode($lunch);
}

mysqli_close($con); // 데이터베이스 접속 종료

?>
