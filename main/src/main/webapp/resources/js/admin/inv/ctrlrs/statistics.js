var statsCtrlr = function($scope, $http, $filter, $uibModal, $rootScope){

	$rootScope.rootPage = "Statistics";
	
	/**Hide the edit controls as per user access*/
	$scope.controlId = $rootScope.globals.currentUser?$rootScope.globals.currentUser.ctrlid:0;	
	$scope.accessGranted = false;
	
	if($scope.controlId>100) return;	
	
	$scope.accessGranted = true;
	$scope.stats = [];
	$scope.sTotalAmount = 0;
	$scope.sortField = 'amount';
	$scope.sortReverse = true;

	$http.get('/inventory/apps/getbasestat').success(function(data){
		if(data.success){
			$scope.stats = data.result;
		}
	});
	
	$scope.driverSelected={driverName:'All Drivers',id:0};
	$http.get('/inventory/apps/listdrivers').success(function(data){
		if(data.success){
			$scope.drivers = data.result;
			$scope.drivers.unshift($scope.driverSelected);
		}
	});
	
	$scope.startDate = "";
	$scope.endDate = "";
	/** Date Save Logic **/
	$scope.saveDate = function(key){
		if(key=='stsDate')
			sessionStorage.setItem(key, $scope.startDate.getTime());
		
		else
			sessionStorage.setItem(key, $scope.endDate.getTime());
	};
	$scope.setDates = function(){
		var startDate = sessionStorage.getItem('stsDate'),
			endDate = sessionStorage.getItem('steDate');
		
		if(startDate){
			$scope.startDate = new Date(parseInt(startDate));
		}
		else {
			var dn = new Date();
			$scope.startDate = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());
		}
		
		if(endDate){ 
			$scope.endDate = new Date(parseInt(endDate));
		}
	};	
	$scope.setDates();
	/** Date Save Logic Ends **/	

	$scope.das = [];
	$scope.dStats = [];
	$scope.dasTipTotal = 0;
	$scope.dasCommTotal = 0;
	$scope.getDriverStat = function(){
		
		$http.get('/inventory/apps/getstat',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy'),
				d:$scope.driverSelected.id
			}
		})
		.success(function(data){
			$scope.alertTxt = "";
			$scope.das = [];
			$scope.dStats = [];
			
			if(data.success){
				if($scope.driverSelected.id==0){
					$scope.das = data.result;
					$scope.getTotal();
					if($scope.das.length==0)
						$scope.alertTxt = "No data found";
				}
				else{
					$scope.dStats = data.result;
					if($scope.dStats.length==0)
						$scope.alertTxt = "No data found";
				}
			}
			else{
				alert(data.message);
			}
		});
	};	
	$scope.getTotal = function(){
		
		$scope.sTotalAmount = 0;
		$scope.dasTipTotal = 0;
		$scope.dasCommTotal = 0;
		
		for(var i = 0; i < $scope.das.length; i++){
	        $scope.sTotalAmount+= $scope.das[i].amount;		
			$scope.dasCommTotal+= $scope.das[i].commission;
			$scope.dasTipTotal+= $scope.das[i].tip;
	    }
	};

	$scope.orStats = [];
	$scope.getOrderStat = function(){
		$http.get('/inventory/apps/getorderstat',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		})
		.success(function(data){
			$scope.alertTxt = "";			
			if(data.success){
				$scope.orStats = data.result;
				if($scope.orStats.length==0)
					$scope.alertTxt = "No data found";
				
				$scope.orderStatSummary();
			}
			else{
				alert(data.message);
			}
		});
	};	
	$scope.orderStatSummary = function(){
		for(var i = 0; i < $scope.orStats.length; i++){
			var originalTotal = 0,
				tipTotal = 0,
				ostats = $scope.orStats[i].ostat;
			
			for(var j = 0; j < ostats.length; j++){
				originalTotal+= ostats[j].originalTotal;
				tipTotal+= ostats[j].tip;
		    }
			
			$scope.orStats[i].originalAmountTotal = originalTotal;
			$scope.orStats[i].tipTotal = tipTotal;
	    }
	};
	
	$scope.pStats = [];
	$scope.getProdStat = function(){
		$http.get('/inventory/apps/getprodstat',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		})
		.success(function(data){
			$scope.alertTxt = "";			
			if(data.success){
				$scope.pStats = data.result;
				if($scope.pStats.length==0)
					$scope.alertTxt = "No data found";
				
				else{
					$scope.pStats.forEach(function(e){
						e.mode = parseInt(e.mode);
					});
				}

			}
			else{
				alert(data.message);
			}
		});		
	};
	
	$scope.cStats = [];
	$scope.getCustStat = function(){
		$http.get('/inventory/apps/getcuststat',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		})
		.success(function(data){
			$scope.alertTxt = "";			
			if(data.success){
				$scope.cStats = data.result;
				if($scope.cStats.length==0)
					$scope.alertTxt = "No data found";

			}
			else{
				alert(data.message);
			}
		});		
	};
	
	$scope.psStats = [];
	$scope.getProfitSalesStat = function(){
		$http.get('/inventory/apps/getprofitsales',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		})
		.success(function(data){
			$scope.alertTxt = "";			
			if(data.success){
				$scope.psStats = data.salesprofit;
				$scope.getPsStatTotal();
				
				if($scope.psStats.length==0)
					$scope.alertTxt = "No data found";

			}
			else{
				alert(data.message);
			}
		});		
	};
	
	$scope.activeTab = 0;
	$scope.tabChange = function(){
		
		if($scope.das.length==0 && $scope.dStats.length==0){
			$scope.getDriverStat();			
		}

		if($scope.orStats.length==0){
			$scope.getOrderStat();			
		}
		
		if($scope.pStats.length==0){
			$scope.getProdStat();
		}
		
		if($scope.cStats.length==0){
			$scope.getCustStat();
		}
		
		if($scope.psStats.length==0){
			$scope.getProfitSalesStat();
		}
	};
	
	$scope.getStat = function(){
		$scope.getDriverStat();
		$scope.getOrderStat();	
		$scope.getProdStat();	
		$scope.getCustStat();
		$scope.getProfitSalesStat();
		
		if($scope.activeTab==6){
			$scope.loadHeatMap();
		}
	};
	
	$scope.psStatTotal = 0;
	$scope.psStatSTotal = 0;
	$scope.psStatPTotal = 0;
	$scope.getPsStatTotal = function(){
		$scope.psStatTotal = 0;
		$scope.psStatSTotal = 0;
		$scope.psStatPTotal = 0;
		for(var i = 0; i < $scope.psStats.length; i++){
	        $scope.psStatTotal+= $scope.psStats[i].total;
	        $scope.psStatSTotal+= $scope.psStats[i].sellingTotal;
	        $scope.psStatPTotal+= $scope.psStats[i].purchaseTotal;
	    }
	};
	
	$scope.closeOpenBatch=function(mode){
		
		var currentOstat = this.orStat.ostat;
		if(!currentOstat) return;
		
		var nextPrevDispatches = "";
		currentOstat.forEach(function(x){
			nextPrevDispatches+="," + x.dispatchId;
		});
		
		//Remove leading comma
		if(nextPrevDispatches.substr(0,1) == ',')
			nextPrevDispatches = nextPrevDispatches.substr(1);
		
		$http.get('/inventory/apps/updatedispatch',{ 
			params : { 'mode' : mode, 
				'sids' : nextPrevDispatches, 
				id:10, //no need for id parameter, we just need something nonzero 
				opsid:_luvbriteGlobalOpsId} 
		})
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				var status = 'open';
				if(mode == 'closesales'){
					status = 'locked'
				}
				
				currentOstat.forEach(function(x){
					x.status = status;
				});			
			}
			else{
				alert(data.message);
			}
		});
	};
	
	$scope.getSalesDetails=function(){
		
		var currentSalesId = this.ostat? this.ostat.dispatchId : this.orStat.ostat[0].dispatchId;

		if(!currentSalesId) return;
		
		var nextPrevDispatches = [];
		$scope.orStats.forEach(function(y){
			y.ostat.forEach(function(x){
				nextPrevDispatches.push(x.dispatchId);
			});
		});
		
		$http.get('/inventory/apps/listdispatches?id='+currentSalesId).success(function(data){
			if(data.success){
				var dispatches = data.result[0];
				
			    var modalInstance = $uibModal.open({
			      templateUrl: '/resources/ng-templates/admin/inv/modals/sales-info.html'+versionCtrl,
			      controller: 'ModalSalesInfoCtrl',
			      backdrop : 'static',
			      size: 'lg',
			      resolve: {
			    	  modalScope: function () {	    		  
			    		  return {
			    			  salesId:currentSalesId,
			    			  dateFinished:dispatches.dateFinished,
			    			  dateArrived:dispatches.dateArrived,
			    			  clientName:dispatches.clientName,
			    			  paymentMode:dispatches.paymentMode,
			    			  additionalInfo:dispatches.additionalInfo,
			    			  driverName:dispatches.driverName,
			    			  tip:dispatches.tip,
			    			  splitAmount:dispatches.splitAmount,
			    			  commissionPercent:dispatches.commissionPercent,
			    			  source:'report',
			    			  nextPrevDispatches:nextPrevDispatches,
			    			  distInMiles:dispatches.distInMiles,
			    			  status:dispatches.status
			    		  };
			    	  }
			      }
			    });
			
			    modalInstance.result.then(
			    	function (){},
			    	function (){}
			    );
			}
		});		
	};
	
	$scope.chartLoaded = false;
	$scope.showChart = false;
	$scope.toggleChartView = function() {
		$scope.showChart = !$scope.showChart;
		
		if(!$scope.chartLoaded){
			$scope.loadChart();
		}
	};
	
	$scope.loadChart = function(){
		
		$http.get('/inventory/apps/getdailysales',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		})
		.success(function(data){
			
			$scope.chartLoaded = true;
			
			var jsonData = data.dt;
			
			//format the date value into JS date object
			jsonData.rows.forEach(function(e){
				var dateFormat = e.c[0].v;
				if(dateFormat.length==10){
					var splits = dateFormat.split('/');
					e.c[0].v = new Date(splits[0], parseInt(splits[1])-1, splits[2]);
				}
				else{
					e.c[0].v = null;
				}
			 });
			
			 var data = new google.visualization.DataTable(jsonData);
	         
	         var dashboard = new google.visualization.Dashboard(
	        	        document.getElementById('chartRangeFilter_dashboard_div'));

    	    var controlSales = new google.visualization.ControlWrapper({
    	      'controlType': 'ChartRangeFilter',
    	      'containerId': 'div_sales_control',
    	      'options': {
    	        // Filter by the date axis.
    	        'filterColumnIndex': 0,
    	        'ui': {
    	          'chartType': 'LineChart',
    	          'chartOptions': {
    	            'chartArea': {'width': '80%', 'height' : '100%'},
                    'colors' : ['grey'],
    	            'hAxis': {'baselineColor': 'none'}
    	          },
    	          // 1 day in milliseconds = 24 * 60 * 60 * 1000 = 86,400,000
    	          'minRangeSize': 7 * 86400000
    	        }
    	      },
    	      
    	      //Date range CurrDate - 10 and CurrDate
    	      'state' : {'range' : {'start' : new Date(new Date().getTime() - (15*86400000)), 'end' : new Date()}}
    	    });

    	    var chartSales = new google.visualization.ChartWrapper({
    	      'chartType': 'AreaChart',
    	      'chartArea': {'height': '80%', 'width': '90%'},
    	      'containerId': 'div_sales_chart',
    	      'options': {
         		 vAxis: {title : "Amount in $"},
                 hAxis: {title : "Date"},
                 title: 'Daily Sales',
                 colors : ['green'],
                 legend: {position: 'none'}
    	      },
    	      'view': {
      	        'columns': [0,1]
      	      }
    	    }),
    	    
    	    chartCount = new google.visualization.ChartWrapper({
    	      'chartType': 'AreaChart',
    	      'chartArea': {'height': '80%', 'width': '90%'},
    	      'containerId': 'div_count_chart',
    	      'options': {
         		 vAxis: {title : "Count"},
                 hAxis: {title : "Date"},
                 title: 'Order Count',
                 legend: {position: 'none'}
    	      },
    	      'view': {
    	        'columns': [0,2]
    	      }
    	    });

    	    dashboard.bind(controlSales, [chartSales, chartCount]);
    	    dashboard.draw(data);
    	    
    	    
    	    
    	    /* Draw Monthly Sales Data*/
    	    var mData = new google.visualization.DataTable(jsonData);
    	    var mSalesView = google.visualization.data.group(mData, [{
    	        		column: 0,
    	        		modifier: function(value) {
    	        			return new Date(value.getFullYear(), value.getMonth());
    	        		},
    	        		type: 'date'
    	        		}], 
    	        		[{column: 1, aggregation: google.visualization.data.sum, type: 'number'}]);
    	    
    	    var mCountView = google.visualization.data.group(mData, [{
    	        		column: 0,
    	        		modifier: function(value) {
    	        			return new Date(value.getFullYear(), value.getMonth());
    	        		},
    	        		type: 'date'
    	    			}], 
    	    			[{column: 2, aggregation: google.visualization.data.sum, type: 'number'}]);
    	    
    	    var monthlySales = new google.visualization.ColumnChart(document.getElementById('div_msales_chart'));
    	    var mOptions1 = {
    	 		 vAxis: {title : "Amount in $"},
    	         hAxis: {title : "Date"},
    	         title: 'Monthly Sales',
    	         colors : ['green'],
    	         legend: {position: 'none'}
    	      };

    	    monthlySales.draw(mSalesView, mOptions1);
    	    
    	    var monthlyCounts = new google.visualization.ColumnChart(document.getElementById('div_mcount_chart'));
    	    var mOptions2 = {
    	    		 vAxis: {title : "Count"},
    	            hAxis: {title : "Date"},
    	            title: 'Monthly Order Count',
    	            legend: {position: 'none'}
    	         };
    	    monthlyCounts.draw(mCountView, mOptions2);
	              
		});		
	};

	try{google.charts.load('current', {'packages':['corechart', 'controls']});}catch(e){}
	//google.charts.setOnLoadCallback($scope.loadChart);
	
	
	/* Load Heatmap */
	$scope.hm = {};
	$scope.hm.avgDistance = 0;
    var map1, map2, heatmap, mapPoints = [new google.maps.LatLng(37.782551, -122.445368)];
    $scope.loadHeatMap = function() {
    	$scope.alertTxt = "";
    	$http.get('/inventory/apps/plotsalesmap',{
			params:{
				sd:$filter('date')($scope.startDate, 'MM/dd/yyyy'),
				ed:$filter('date')($scope.endDate, 'MM/dd/yyyy')
			}
		}).then(function(resp){
			var data = resp.data;	
			if(data.success){	
				mapPoints = [];
				var totalDistance = 0;
				if(data.result && data.result.length>0){
					data.result.forEach(function(x){
						totalDistance+= x.distInMiles;
						
						mapPoints.push(new google.maps.LatLng(x.lat, x.lng));
					});	
					
					/* Find avg distance */ 
					$scope.hm.avgDistance = totalDistance/data.result.length;
					
					/* Plot the Heat Map */
					var myLatLng = {lat: 34.034745, lng: -118.45228}; 
					map1 = new google.maps.Map(document.getElementById('heatMapWrapper'), {
					  zoom: 11,
					  center: myLatLng
					});
					
					heatmap = new google.maps.visualization.HeatmapLayer({
					  data: mapPoints,
					  map: map1
					});	
					
					/* Plot the Marker Map*/
					map2 = new google.maps.Map(document.getElementById('markerMapWrapper'), {
					  zoom: 11,
					  center: myLatLng
					});
					
					data.result.forEach(function(x){
						var marker = new google.maps.Marker({
							position: {lat: x.lat, lng: x.lng},
							map: map2,
							icon: {
								path: google.maps.SymbolPath.CIRCLE,
								fillColor:'#ff6666',
								fillOpacity:1,
								scale: 4,
								strokeWeight:2
							}
						});
					});
				}
				else{
					$scope.alertTxt = "No data found";
				}
			}			
		});
    };
	
};