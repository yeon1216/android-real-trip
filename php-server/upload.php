<?php
/**
 * 설정 시작
 */
$upload_dir = "./uploads/";
$allow_ext = array("jpg","jpeg","png","gif");
$file = $_FILES['fileToUpload'];

/**
 * uploads 디렉토리가 없다면 생성
 */
if(!is_dir($upload_dir)){ // uploads 디렉토리가 있는지 없는지 확인
    if(!mkdir($upload_dir,0777)){ // uploads 디렉토리 생성, 생성 성공했는지 확인
        die("업로드 디렉토리 생성에 실패 했습니다."); // 디렉토리 생성 실패 (권한이 없어서??)
    }
}

/**
 * 파일이 없으면 die
 */

if(empty($file)){
    echo "업로드된 파일이 없습니다.";
    exit;
}

/**
 * 변수정리
 */
$error = $file['error'];
$name = $file['name'];

/**
 * 오류 확인
 */
if($error != UPLOAD_ERR_OK){
    switch($error){
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:
            echo "파일이 너무 큽니다. ($error)";
            break;
        case UPLOAD_ERR_PARTIAL:
            echo "파일이 부분적으로 첨부되었습니다. ($error)";
            break;
        case UPLOAD_ERR_NO_FILE:
            echo "파일이 첨부되지 않았습니다. ($error)";
            break;
        case UPLOAD_ERR_NO_TMP_DIR:
            echo "임시파일을 저장할 디렉토리가 없습니다. ($error)";
            break;
        case UPLOAD_ERR_CANT_WRITE:
            echo "임시파일을 생성할 수 없습니다 ($error)";
            break;
        case UPLOAD_ERR_EXTENSION:
            echo "업로드 불가능한 파일이 첨부 되었습니다 ($error)";
            break;
        default:
            echo "파일이 제대로 업로드되지 않았습니다 ($error)";
    }
    exit;
}

/**
 * 저장될 디렉토리 및 파일명, 첨부파일 정보, 확장자를 받음
 */
$upload_file = $upload_dir.'/'.$name;
$fileinfo = pathinfo($upload_file);
$target_file = $upload_dir . basename($file["name"]);
$ext = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));

/**
 * 파일 이름 생성
 */
$i=1;
while(is_file($upload_file)){
    $name = $fileinfo['filename']."-{$i}.".$fileinfo['extension'];
    $upload_file = $upload_dir.'/'.$name;
    $i++;
}

/**
 * 파일 확장자 확인
 */
if(!in_array($ext,$allow_ext)){
    echo "허용되지 않는 확장자입니다.";
    exit;
}

/**
 * 파일 이동
 */
if(!move_uploaded_file($file['tmp_name'],$upload_file)){
    echo "파일이 업로드 되지 않습니다";
    exit;
}else{
    // echo "파일 업로드 성공";
    // echo $upload_file;
}

?>