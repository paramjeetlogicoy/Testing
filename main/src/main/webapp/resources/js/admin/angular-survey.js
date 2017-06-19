//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var surveyApp = angular.module(
	'surveyApp', [
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
	$scope.surveys = [];
	$scope.getSurveys = function(){
		
		$http.get('/admin/surveys/json/').success(function(data){
			if(data.success){
				$scope.surveys = data.results;
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
	
	$scope.getSurveys();
};

surveyApp

//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr);

