//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var seoApp = angular.module(
	'seoApp', [
         'ngRoute',
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $sanitize){
	
	$scope.pageLevelError = '';
	$scope.elements = [];
	$scope.seoSearch = { url : ''};	
	
	$scope.getElements = function(){
		
		$http.get('/admin/seo/json/').success(function(data){
			if(data.success){
				$scope.elements = data.results;
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the list."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting the list."
					+" Please contact G";
		});	
		
	};
	
	$scope.getElements();
},

elemCtrlrs = function($scope, $http, $filter, $routeParams, $location, $sanitize){
	
	$scope.params = $routeParams;
	$scope.errorMsg = '';
	$scope.elem = {};
	$scope.elemId = $scope.params.elemId;
	
	$scope.getElemDetails = function($event){
		
		$http.get('/admin/seo/json/' + $scope.elemId)
		.success(function(data){
			$scope.elem = data;			
			$('#seoelem-editor').show();
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the SEO element info. Please try later';
			$('#seoelem-editor').show();
		});		
	};	
	
	$scope.closeModal = function(){
		$('#seoelem-editor').hide();
		$scope.initalize();
	};
	
	$scope.saveElem = function(cb){	
			
		$http.post('/admin/seo/json/save', $scope.elem)
		.success(function(resp){
			if(cb)
				cb(resp);
		})
		.error(function(){
			$scope.errorMsg  = "There was some error saving the element details. "
					+"Please contact G.";
		});	
	};
	
	
	$scope.initalize = function(){
		$scope.errorMsg = '';
		$scope.p = {};	
    	$location.path('');	
	};
	
	
	$scope.getElemDetails();
};

seoApp.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
	    when('/details/:elemId', {
	         templateUrl: '/resources/ng-templates/admin/seo-element.html'+versionCtrl,
	         controller: 'elemControllers'
	    }).
	    
	    when('', {
	         templateUrl: '/resources/ng-templates/empty.html',
	         controller: 'defaultRouteCtrl'
	    });
}])



//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)
.controller('elemControllers', elemCtrlrs);

