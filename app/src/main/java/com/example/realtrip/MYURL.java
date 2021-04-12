package com.example.realtrip;

public class MYURL {
    /**
     * 서버 url
     */
//    public static String MYURL = "http://18.221.242.79/query.php"; // aws 서버
    public static String URL = "http://35.224.156.8/query.php"; // gcp 서버


    /**
     * 여행지 공공 데이터 url
     */
    // 서비스키
    public static String SERVICE_KEY = "7GNSLWSaaHEWI0xpeLzSLH%2BKcYzU1IavfKzPBWIfdrkXbACQ0ncI1knbnqbWjsNYjGDPyByfbIp4Sa8AXVV8cw%3D%3D";

    // 지역기반 url (AREABASED)
    public static String TOUR_URL_AREABASED = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?" +
            "contentTypeId=&sigunguCode=&cat1=&cat2=&cat3=&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=P&numOfRows=50&pageNo=1&_type=json" +
            "&ServiceKey="+SERVICE_KEY+
            "&areaCode=";

    // 지역기반 url (AREABASED) 챗봇용
    public static String TOUR_URL_AREABASED_CHAT_BOT = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?" +
            "contentTypeId=&sigunguCode=&cat1=&cat2=&cat3=&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=P&numOfRows=5&pageNo=1&_type=json" +
            "&ServiceKey="+SERVICE_KEY+
            "&areaCode=";

    // 키워드검색 url (SEARCH_KEYWORD)
    public static String TOUR_URL_SEARCH_KEYWORD = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchKeyword?MobileOS=ETC&MobileApp=AppTest&_type=json&arrange=P&numOfRows=50&pageNo=1" +
            "&ServiceKey="+SERVICE_KEY+
            "&keyword=";

    public static String which_streaming_str = "7";

} // MYURL 클래스
