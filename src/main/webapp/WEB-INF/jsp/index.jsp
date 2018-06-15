<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE HTML>
<html>
	<head>
	<style>
		div{
			margin:10px;
		}
		html,body{
			height:100%;
		}
	</style>
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <title>eDownload</title>
	<link rel="stylesheet" type="text/css" href="/css/bootstrap/3.3.6/bootstrap.min.css">
	<script src="/js/bootstrap.min.js"></script>
	<script>
	
		 var websocket = null;

	    //判断当前浏览器是否支持WebSocket
	    if('WebSocket' in window){
	        websocket = new WebSocket("ws://localhost:80/websocket");
	    }
	    else{
	        alert('Not support websocket')
	    }

	    //连接发生错误的回调方法
	    websocket.onerror = function(){
	        setMessageInnerHTML("error");
	    };

	    //连接成功建立的回调方法
	    websocket.onopen = function(event){
	        
	    }

	    //接收到消息的回调方法
	    websocket.onmessage = function(event){
	        setMessageInnerHTML(event.data);
	    }

	    //连接关闭的回调方法
	    websocket.onclose = function(){
	        //setMessageInnerHTML("close");
	        setMessageInnerHTML("连接已断开！请刷新浏览器！");
	    }

	    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
	    window.onbeforeunload = function(){
	        websocket.close();
	    }

	    //将消息显示在网页上
	    function setMessageInnerHTML(msg){
	    	show = $("#show_message").val();
	    	var scrollTop = $("#show_message")[0].scrollHeight;  
            $("#show_message").scrollTop(scrollTop);  
	    	if(msg=='complete\n'){
	    		$('#download').removeAttr("hidden");
	    	}
			$("#show_message").val(show + msg);
	    }

	    //关闭连接
	    function closeWebSocket(){
	        websocket.close();
	    }

	    //发送消息
	    function send(){
	    	var url = $('#url').val();
			var begin = $('#begin').val();
			var end = $('#end').val();
			if(url && begin && end){
				$("#show_message").val("");
				var msg = url + ',' + begin + ',' + end;
				websocket.send(msg);
			}else{
				$("#show_message").val("输入不完整！");
			}
	    }
	
	


		
		
	
	
	
	</script>
	
	</head>
	<body>
		<div style='width:800px;margin:0 auto;height:100%;text-align:center'>
			<div class="page-header">
  				<h1>下载王<small>说明：结束为0表示全部</small></h1>
			</div>
			<div class="input-group">
				  <span class="input-group-addon" id="basic-addon1">地址</span>
				  <input id="url" type="text" class="form-control" placeholder="https://..." aria-describedby="basic-addon1">
			</div>
				 
				<div class="input-group">
					<span class="input-group-addon" id="basic-addon2">起始</span>
				  <input type="text" id="begin" class="form-control" value='1' aria-describedby="basic-addon2">
				  </div>
				  <div class="input-group">
					<span class="input-group-addon" id="basic-addon2">结束</span>
				  <input type="text" id="end" class="form-control" value='0' aria-describedby="basic-addon2">
				  
			</div>
			
			<div>
				<button onclick="send()" class="btn btn-primary">开始</button>
			</div>
			<div hidden id="download">
				<a href='/download'><button   class="btn btn-danger" >下载</button></a>
			</div>
			<div style="height:100%">
				<textarea id="show_message" class="form-control" readonly="readonly" style="background-color:transparent;height:50%;resize:none"></textarea>
			</div>
		</div>
		
	
	</body>
</html>