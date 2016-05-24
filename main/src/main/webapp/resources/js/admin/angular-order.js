//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var ordApp = angular.module(
	'orderApp', [
         'ui.bootstrap',
         'ngSanitize',
         'ordDtlsModule'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $rootScope, $sanitize, ordDtlService){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	
	$scope.sortType = 'orderNumber';
	$scope.sortReverse = true;
	$scope.orderSearch = '';
	$scope.orders = '';
	
	$scope.getOrders = function(){
		
		$http.get('/admin/orders/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.orderSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){
				$scope.orders = data.respData;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the orders."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting the orders."
					+" Please contact G";
		});	
		
	};
	
	$scope.pageChanged = function() {
		$scope.getOrders();
	};
	
	$scope.getOrders();
	
	/**
	 * Order Details Fns
	 **/
	
	$scope.showDetails = function(){
		ordDtlService.orderNumber = this.o.orderNumber;
		
		ordDtlService.showGallery($rootScope);
	};
};

ordApp

//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)

//This is defined in angular-orderdetails-service.js
.controller('ordDtlsCtrlr', ordDtlsCtrlr);

