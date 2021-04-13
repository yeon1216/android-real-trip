<?php

include_once('./PHPMailer/PHPMailerAutoload.php');

function mailer($fname, $fmail, $to, $subject, $content)
{
    
    // echo $fname.'<br>';
    // echo $fmail.'<br>';
    // echo $to.'<br>';
    // echo $subject.'<br>';
    // echo $content.'<br>';

    // echo "<script>console.log('mailer() 함수 실행')</script>";
    try{
        // echo "<script>console.log('try문 실행')</script>";

        if(empty($mail)){
            // echo 'mail empty true<br>';
        }else{
            // echo 'mail empty false<br>';
        }

        $mail = new PHPMailer();

        if(empty($mail)){
            // echo 'mail empty true<br>';
        }else{
            // echo 'mail empty false<br>';
        }

        $mail->IsSMTP();

        $mail->SMTPSecure="ssl";
        $mail->SMTPAuth=true;

        $mail->Host = "smtp.naver.com";
        $mail->Port=465;
        $mail->Username="qwse8770@naver.com";
        $mail->Password="abcd1234";

        $mail->CharSet='UTF-8';
        $mail->From=$fmail;
        $mail->FromName=$fname;
        $mail->Subject=$subject;
        $mail->msgHTML($content);
        $mail->addAddress($to);
        
        if($mail->send()){
            // echo "<script>console.log('메일전송 성공')</script>";
        }else{
            // echo "<script>console.log('메일전송 실패')</script>";
            // echo "메일 전송 실패 error : ".$mail->ErrorInfo;
        }
    }catch(Exception $e){
        // echo "<script>console.log('catch문 실행')</script>";
        $e = $e->getMessage() . '(오류코드: '.$e->getCode().')';
        // echo "<script>console.log('error : '+$e)</script>";    
    }
}

?>