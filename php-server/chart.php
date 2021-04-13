<html>
	<head>
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

    <script type="text/javascript">
	      google.charts.load('current', {'packages':['corechart']});
				google.charts.setOnLoadCallback(drawChart);

				/*
					차트 그리는 함수
				*/
				function drawChart() {
						var magnitude_arr_str = "<?php echo $_GET['magnitude'];?>";
						var score_arr_str = "<?php echo $_GET['score'];?>";
						var magnitude_arr = magnitude_arr_str.split(",");
						var score_arr = score_arr_str.split(",");
						var review_count = score_arr.length;

						var data_array = [['감정의 정도', '긍정/부정의 정도']];
						console.log(review_count);
						for (var n = 0; n < review_count; n++) {
					  	data_array.push([parseFloat(magnitude_arr[n]), parseFloat(score_arr[n])]);

							console.log(n+'번째 magnitude: '+parseFloat(magnitude_arr[n]));
							console.log(n+'번째 score: '+parseFloat(score_arr[n]));
						}


						var data = google.visualization.arrayToDataTable(data_array);

		        var options = {
		              title: "각 리뷰의 '긍정/부정'값과 '감정'값 그래프",
		              hAxis: {title: '감정의 정도', minValue: 0, maxValue: 3},
		              vAxis: {title: '긍정/부정의 정도', minValue: -1, maxValue: 1},
		              legend: 'none'
		            };

		        var chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));

		        chart.draw(data, options);
        }
    </script>
  </head>
  <body>
		<div>
			<br>
			<p style="font-size:14">
				<strong style="font-size:16">◆ 리뷰분석 과정</strong><br>
				&nbsp;&nbsp; 1. 리뷰를 리뷰분석 공장에 보냄<br>
				&nbsp;&nbsp; 2. 공장에서는 리뷰의 <strong>'긍정/부정의 정도'</strong>와 <strong>'감정의 정도'</strong> 값을 알려줌<br>
				&nbsp;&nbsp; 3. 리뷰의 <strong>'긍정/부정의 정도'</strong>와 <strong>'감정의 정도'</strong> 값을 이용해 리뷰를 분석<br>
			</p>
		</div>
		<div>
			<br>
			<p style="font-size:14">
				<strong style="font-size:16">◆ 리뷰분석 자료</strong><br>
				<div id="chart_div" style="width: 450px; height: 400px;"></div><br/>
				<p style="font-size:12">※ 각 점은 리뷰의 <strong>'긍정/부정의 정도'</strong>와 <strong>'감정의 정도'</strong> 값을 표현<br><br></p>
				<p style="font-size:12">

					<strong style="font-size:14">긍정/부정의 정도?</strong><br>
					&nbsp;&nbsp;'<strong>-1.0</strong>'에 가까울수록 부정적 리뷰이고 '<strong>1.0</strong>'에 가까울수록 긍정적 리뷰<br/><br/>
				</p>
				<p style="font-size:12">
					<strong style="font-size:14">감정의 정도?</strong><br>
					&nbsp;&nbsp; 리뷰에 담겨있는 감정의 정도에 비례<br>
				</p>
			</p>
		</div><br>

		<div>
			<p style="font-size:14">
				<strong style="font-size:16">◆ 리뷰분석기준</strong><br><br>
				&nbsp;&nbsp; ☞ <strong>매우긍정</strong> : 0.6< '긍정/부정의 정도' <=1<br><br>
				&nbsp;&nbsp; ☞ <strong>긍    정</strong> : 0.1< '긍정/부정의 정도' <=0.6<br><br>
				&nbsp;&nbsp; ☞ <strong>중    립</strong> : '감정의 정도' <=0.3 <i style="font-size:12">또는</i><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				0<= '긍정/부정의 정도' <=0.1<br><br>
				&nbsp;&nbsp; ☞ <strong>긍    정</strong> : -0.6<= '긍정/부정의 정도' < 0 <br><br>
				&nbsp;&nbsp; ☞ <strong>매우부정</strong> : -1<= '긍정/부정의 정도' < -0.6 <br><br>
			</p><br>
		</div>
  </body>
</html>
