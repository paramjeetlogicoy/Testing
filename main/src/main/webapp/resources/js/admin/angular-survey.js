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
		
		$http.get('/admin/surveys/json/')
		.then(function(resp){
			var data = resp.data;
			
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

		}, function(){
			$scope.pageLevelError = "There was some error getting the list."
					+" Please contact G";
		});	
		
	};
	
	$scope.getSurveys();
},

surveyDetailsCtrl = function($scope, $http, $routeParams, ChartService){
	
	$scope.surveyId = $routeParams.id;
	
	$scope.pageLevelError = '';
	$scope.surveyResponse = [];
	$scope.activateChart = false;
	$scope.totalSurveys = 0;
	
	var surveyQns = [],
	surveyAggregate = [],
	
	formatSurveyData = function(){
		
		if(surveyQns && surveyQns.length){
			
			$scope.surveyResponse = [];
			
			for(var i=0; i<surveyQns.length; i++){
				 
				var qn = surveyQns[i],
				qnResp = [['Factor', 'Count']];
				
				for(var j=0; j<surveyAggregate.length; j++){
					
					var currResp = surveyAggregate[j];
					if(currResp && 
							currResp._id && 
							currResp._id.qid == qn._id){
						
						qnResp.push([currResp._id.resp, currResp.sum]);
					}
				}
				
				
				$scope.surveyResponse.push({
					questionId : qn._id,
					question : qn.question,
					response : qnResp
				})
			}
			
			//console.log($scope.surveyResponse);
			
			//load Google Visualization
			loadGoogleVis();
		}
		
	},
	
	loadGoogleVis = function(){
	
		var loadGoogle = ChartService.loadGoogleVisualization();
	    
	    // If the Google Loader request was made with no errors, 
	    // register a callback, and construct the chart data
	    // model within the callback function
	    if (loadGoogle) {
	
	        google.charts.setOnLoadCallback(function() {
	            try {
	            	$scope.$apply(function(){
	            		$scope.activateChart = true;    
	            	});
	            } catch(e) {}
	        });  
	    }
	};
	
	$scope.getSurveryResponses = function(){
		
		$http.get('/admin/surveys/aggregate/' + $scope.surveyId)
		.then(function(resp){
			var data = resp.data;
			
			if(data.success){
				surveyAggregate = data.results;
				$scope.totalSurveys = parseInt(data.message);
				
				formatSurveyData();
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the survey response."
					+" Please contact G";
			}

		}, function(){
			$scope.pageLevelError = "There was some error getting the survey response."
					+" Please contact G";
		});
	};
	
	$scope.getSurveyQuestions = function(){
		
		$http.get('/admin/surveys/questions/' + $scope.surveyId)
		.then(function(resp){
			var data = resp.data;
			
			if(data.success){
				surveyQns = data.results;
				
				$scope.getSurveryResponses();
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the questions list."
					+" Please contact G";
			}

		}, function(){
			$scope.pageLevelError = "There was some error getting the questions list."
					+" Please contact G";
		});
	};
	
	$scope.getSurveyQuestions();
};

surveyApp

//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js

.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
	    when('/all', {
	         templateUrl: 'allSurveys',
	         controller: 'defaultRouteCtrl'
	    }).
	    when('/details/:id', {
	         templateUrl: 'surveyDetails',
	         controller: 'surveyDetailsCtrl'
	    }).
	    otherwise({ redirectTo: "/all" });
}])

.factory('ChartService', function() {
    return {
        
        /**
         * Loads the visualization module from the Google Charts API 
         * if available
         * @returns {boolean} - Returns true is successful, or false 
         * if not available
         */
        loadGoogleVisualization: function() {
        	
            try {
            	google.charts.load('current', {packages: ['corechart', 'bar']});
                return true;
            
            } catch(e) {
                console.log('Could not load Google lib', e);
                return false;  
            }

        }
    };
})

.directive("googleChart",function(){  
    return{
        restrict : "A",
        link: function($scope, $elem, $attr){
            var model;

            // Function to run when the trigger is activated
            var initChart = function() {

            	model = $scope.$eval($attr.ngModel);

            	// If the model is defined on the scope,
            	// grab the dataTable that was set up
            	// during the Google Loader callback
            	// function, and draw the chart
            	if (model) {
//          		var dt = model.dataTable,
//          		options = {},
//          		chartType = $attr.googleChart;

//          		if (model.title) {
//          		options.title = model.title;
//          		}

            		var data = google.visualization.arrayToDataTable(model.response),
            		chartType = $attr.googleChart,
            		options = {
            			title: model.question,
            	        chartArea: {width: '95%'}
            		},

            		chart = new google.visualization[chartType]($elem[0]);

            		chart.draw(data, options);
            	}
            };

            // Watch the scope value placed on the trigger attribute
            // if it ever flips to true, activate the chart
            $scope.$watch($attr.trigger, function(val){
                if (val === true) {
                    initChart(); 
                }
            });
            
        }
    }
})

.controller('defaultRouteCtrl', defaultCtrlr)
.controller('surveyDetailsCtrl', surveyDetailsCtrl);

