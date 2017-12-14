<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="zh-CN">
<head>
<title>在线预览</title>
<script type="text/javascript" src="../static/FlexPaper/js/jquery.js"></script>
<script type="text/javascript" src="../static/FlexPaper/js/flexpaper_flash.js"></script>
<script type="text/javascript" src="../static/FlexPaper/js/flexpaper_flash_debug.js"></script>

</head>
<body>
<center>
	<div style="width: 80%;height: 100%">
		<!-- 设置显示位置 -->
		<%-- 指定flexPaper的宽度和高度  --%>
		<a id="viewerPlaceHolder"
			style="width: 100%; height: 100%; display: block"></a>
		<script type="text/javascript"> 
                var fp = new FlexPaperViewer(    
                         '../static/FlexPaper/swfFiles/FlexPaperViewer',
                         'viewerPlaceHolder',    
                         { config : {
                         SwfFile : decodeURI('${swfPath}'),
							Scale : 1.2, /*初始化缩放比例  */
							ZoomTransition : 'easeOut', /*Flexpaper中缩放样式  */
							ZoomTime : 0.5, /*缩放比例需要花费的时间  */
							ZoomInterval : 0.2, /* 缩放滑块-移动的缩放基础[工具栏] */
							FitPageOnLoad : false, /*自适应页面  */
							FitWidthOnLoad : false, /*自适应宽度  */
							PrintEnabled : true,
							FullScreenAsMaxWindow : false, /*全屏按钮-新页面全屏[工具栏]  */
							ProgressiveLoading : true, /*分割加载   */
							MinZoomSize : 0.2, /*最小缩放 */
							MaxZoomSize : 5, /*最大缩放  */
							SearchMatchAll : true, /*符合条件的地方高亮显示  */
							InitViewMode : 'Portrait', /* 初始显示模式(SinglePage,TwoPage,Portrait)  */

							ViewModeToolsVisible : true, /*显示模式工具栏是否显示  */
							ZoomToolsVisible : true, /* 缩放工具栏是否显示  */
							NavToolsVisible : true, /*跳页工具栏   */
							CursorToolsVisible : true, /* 工具栏上是否显示光标工具 */
							SearchToolsVisible : true, /*工具栏上是否显示搜索  */
							localeChain : 'zh_CN'
						}
					});
		</script>
	</div>
</center>
</body>
</html>