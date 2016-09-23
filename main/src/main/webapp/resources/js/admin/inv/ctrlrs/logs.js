var logCtrlr = function($scope, $http, $rootScope){

	$rootScope.rootPage = "Tracking Logs";
	$scope.controlId = _luvbriteGlobalOpsId;
	
	if($scope.controlId>100) return;	
	
	$scope.accessGranted = true;
	$scope.operatorSelected={operatorName:'All Operators',id:0};
	$scope.driverSelected={driverName:'All Drivers',id:0};

	$http.get('/inventory/apps/listdrivers').success(function(data){
		if(data.success){
			$scope.drivers = data.result;
			$scope.drivers.unshift($scope.driverSelected);
		}
	});

	$http.get('/inventory/apps/listops').success(function(data){
		if(data.success){
			$scope.operators = data.result;
			$scope.operators.unshift($scope.operatorSelected);
		}
	});

	$scope.actionObjs = [{value:'',label:'All Objects'},{value:'shops', label:'Shops'},
	                     {value:'packet', label:'Packets'},
	                     {value:'cat', label:'Categories'},{value:'ops', label:'Operators'},
	                     {value:'prod', label:'Products'},{value:'strain', label:'Strains'},
	                     {value:'vendor', label:'Vendors'},{value:'purchase', label:'Purchases'},
	                     {value:'clients', label:'Clients'},{value:'drivers', label:'Drivers'}]
	$scope.actionObjSelected = $scope.actionObjs[0];
	
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.sortBy = '';
	$scope.sdir = 'ASC'
	
	$scope.logs = [];		
	$scope.pageChanged=function(){
		$scope.reload();
	};
	
	$scope.sortCol=function(param, e){
		$scope.sortBy = param;
		var $currentTarget = $(e.currentTarget);
		
		/** remove sort class from all elements and later add it
		 ** just to the required one. 
		 **/
		if($currentTarget.hasClass('sdir-asc')){
			$scope.sdir='DESC';
			$('.sdir-asc,.sdir-desc').removeClass('sdir-desc sdir-asc');
			$currentTarget.addClass('sdir-desc');
		}
		else {
			$scope.sdir='ASC';
			$('.sdir-asc,.sdir-desc').removeClass('sdir-desc sdir-asc');
			$currentTarget.addClass('sdir-asc');
		}
		
		
		$scope.reload();		
	};
	
	/**
	 * Filter can be either on operator or driver. So if one is 
	 * selected, reset the other one.
	 **/
	$scope.driverFilter=function(){
		$scope.operatorSelected=$scope.operators[0];
		$scope.reload();
	};		
	
	$scope.operatorFilter=function(){
		$scope.driverSelected=$scope.drivers[0];
		$scope.reload();
	};
	
	$scope.reload=function(){
		var urlParams = "?cpage="+$scope.pg.currentPage
				+ "&op="+$scope.operatorSelected.id
				+ "&dr="+$scope.driverSelected.id
				+ "&obj="+$scope.actionObjSelected.value
				+ "&sort="+$scope.sortBy
				+ "&sdir="+$scope.sdir;
		
		$http.get('/inventory/apps/listtracker'+urlParams).success(function(data){
			if(data.success){
				$scope.logs = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
			}
		});		
	};	

	$scope.reload();
};