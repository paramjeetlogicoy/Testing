var miscReportCtrlr = function($scope, $http, $rootScope){

	$rootScope.rootPage = "Bar Codes";
	$scope.controlId = _luvbriteGlobalOpsId;
	
	if($scope.controlId>100) return;	
	
	$scope.alertTxt = '';
	$scope.sku = '';
	
	$scope.totalItems = 14;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 4;
	
	/**Pagination Logic**/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.sortBy = 'date';
	$scope.sdir = 'DESC'	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	/***/
	
	$scope.sortCol=function(param, e){
		//generic fn defined in main.js
		sortColFns($scope, param, e);
	};

	$scope.packets = [];
	$scope.reload=function(){
		$scope.alertTxt = '';
		
		var urlParams = "?cpage="+$scope.pg.currentPage
				+ "&sort="+$scope.sortBy
				+ "&sdir="+$scope.sdir
				+ "&allmisc=true"
				+ "&pc="+$scope.sku;
		
		$http.get('/inventory/apps/listpackets'+urlParams).success(function(data){
			if(data.success){
				$scope.packets = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
			}
		});	
	};	

	$scope.reload();
};