var homeCtrlr = function($scope, $http, $rootScope){
	
	$scope.fiveGramSpecials = [];
	$scope.successMsg = '';
	$scope.errorMsg = '';
	
	$scope.get5gms = function(){
		
		$http.get(_lbUrls.getprd + 'get5gspecials', {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(resp){
			$scope.fiveGramSpecials = resp.data;	
		});	
	};
	
	$scope.get5gms();
};