//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var crApp = angular.module(
	'controlRecordApp', [
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter){
	
	$scope.pageLevelError = '';
	$scope.records = [];
	
	$scope.getRecords = function(){
		
		$http.get('/admin/ctrls/json/all')
		.then(function(resp){			
			$scope.records = resp.data;
		});	
		
	};	
	$scope.getRecords();
	
	$scope.reloadConfig = function(){
		
		$http.post('/admin/ctrls/reloadconfig')
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Config Reloaded');
			}
			else{
				alert(data.message);
			}
		});
	};
	
	
	$scope.saveCtrlRecord = function(){
		$http.post('/admin/ctrls/saverecord', this.r)
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Record Saved');
				$scope.getRecords();
			}
			else{
				alert(data.message);
			}
		});
	};
    
};

crApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr);
